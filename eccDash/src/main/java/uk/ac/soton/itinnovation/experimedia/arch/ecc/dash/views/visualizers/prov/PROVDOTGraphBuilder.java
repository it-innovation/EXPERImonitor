/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
//
// Copyright in this software belongs to University of Southampton
// IT Innovation Centre of Gamma House, Enterprise Road,
// Chilworth Science Park, Southampton, SO16 7NS, UK.
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//      Created By :            Simon Crowle
//      Created Date :          30-Oct-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.visualizers.prov;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.*;

import java.text.SimpleDateFormat;
import java.util.*;



public class PROVDOTGraphBuilder
{
  private final static String agentNodeStyle    = "style=\"filled\",shape=\"house\",fillcolor=\"#FDB266\"";
  private final static String entityNodeStyle   = "style=\"filled\",color=\"#808080\",fillcolor=\"#FFFC87\"";
  private final static String activityNodeStyle = "style=\"filled\",color=\"#0000FF\",shape=\"polygon\",fillcolor=\"#9FB1FC\",sides=\"4\"";

  private HashMap<UUID, EDMProvBaseElement> agents;
  private HashMap<UUID, EDMProvBaseElement> activities;
  private HashMap<UUID, EDMProvBaseElement> entities;


  public PROVDOTGraphBuilder()
  {
    agents     = new HashMap<UUID, EDMProvBaseElement>();
    activities = new HashMap<UUID, EDMProvBaseElement>();
    entities   = new HashMap<UUID, EDMProvBaseElement>();
  }

  public String createDOT( EDMProvReport report ) throws Exception
  {
    agents.clear();
    activities.clear();
    entities.clear();

    // Safety first
    if ( report == null ) throw new Exception( "Could not create DOT data - PROV report is null" );

    String dotData = null;

    // All private method calls potentially throw exceptions - pass these up
    parseProvReport( report );

    // Header
    dotData = writeDOTHeader( report );

    // Timeline
    dotData += writeTimeLine();

    // Write nodes first
    dotData += writeNodes();

    dotData += writeArcs();

    // Write footer here
    dotData += "\n}";

    return dotData;
  }

  // Private methods -----------------------------------------------------------
  private void parseProvReport( EDMProvReport report ) throws Exception
  {
    HashMap<String, EDMProvBaseElement> provElements = report.getProvElements();

    if ( provElements.isEmpty() ) throw new Exception( "Could not parse Prov report - no PROV elements are found" );

    for ( EDMProvBaseElement el : provElements.values() )
    {
      UUID id = el.getInstanceID();

      switch ( el.getProvType() )
      {
        case ePROV_ENTITY   : entities.put( id, el ); break;
        case ePROV_AGENT    : agents.put( id, el ); break;
        case ePROV_ACTIVITY : activities.put( id, el ); break;
      }
    }
  }

  private String writeDOTHeader( EDMProvReport report )
  {
    return "digraph \"" + report.getID().toString() + "\" \n{\n";
  }

  private String makeLabel(String input) {
//      return ",label=\"" + input.replaceFirst("experimedia:", "").replaceFirst("_", "\n") + "\"";
      int start = input.indexOf(":");
      int end = input.indexOf("_");
      return ",label=\"" + input.substring(start + 1, end) + "\"";
  }

  private String writeNodes() throws Exception
  {
    String provOUT = "";

    // Agents
    for ( EDMProvBaseElement agent : agents.values() )
    {
      provOUT += "\"" + agent.getIri() + "\"";
      provOUT += " [" + agentNodeStyle + makeLabel(agent.getIri()) + "]\n";
    }

    // Entities
    for ( EDMProvBaseElement entity : entities.values() )
    {
      provOUT += "\"" + entity.getIri() + "\"";
      provOUT += " [" + entityNodeStyle + makeLabel(entity.getIri()) + "]\n";
    }

    // Activities
    for ( EDMProvBaseElement activity : activities.values() )
    {
      provOUT += "\"" + activity.getIri() + "\"";
      provOUT += " [" + activityNodeStyle + makeLabel(activity.getIri()) + "]\n";
    }

    provOUT += "\n";

    return provOUT;
  }

  private String writeArcs() throws Exception
  {
    String provOUT = "";

    // Agent relations
    for ( EDMProvBaseElement agent : agents.values() )
      for ( EDMProvTriple triple : getProvRelations(agent) )
        provOUT += writeRelation( triple);

    // Entity relations
    for ( EDMProvBaseElement entity : entities.values() )
      for ( EDMProvTriple triple : getProvRelations(entity) )
        provOUT += writeRelation( triple);


    // Activity relations
    for ( EDMProvBaseElement activity : activities.values() )
      for ( EDMProvTriple triple : getProvRelations(activity) )
        provOUT += writeRelation( triple);

    provOUT += "\n";

    return provOUT;
  }

  private String writeTimeLine() throws Exception
  {
    String provOUT = "";
    String timelineErrors = "";

    String timelineMiniGraph = "past ";
    String timelineLayout    = "node [shape=box];\n";

    // Activity relations
    SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    ArrayList<Date> timeStamps = new ArrayList<Date>();
    for ( EDMProvBaseElement activity : activities.values() )
    {
      String activityID = activity.getIri();

      LinkedList<EDMProvTriple> triples = activity.getTriplesWithPredicate( "prov:startedAtTime" );

      // Make a note of any problems we find as we go along
      if ( triples.isEmpty() )
        timelineErrors += "Visualisation problem: Could not find start time for Activity " + activityID + " ";

      if ( triples.size() > 1 )
        timelineErrors += "Visualisation problem: Activity " + activityID + " has more than one start time!";

      // If no problems, then write out some timeline
      if ( timelineErrors.isEmpty() )
      {
        String timeStamp = triples.get( 0 ).getObject();
        if (timeStamp != null) {
            timeStamp = timeStamp.replaceAll("\\^\\^xsd:dateTime", "");
//            timeStamp = timeStamp.replaceFirst("T", "\n");
        }
        timeStamps.add(ISO8601DATEFORMAT.parse(timeStamp));
        timelineLayout += "{ rank = same; \"" + timeStamp.replaceAll("T", "\n").replaceAll("Z", "") + "\"; \"" + activityID + "\"; }\n";
      }
    }

      Collections.sort(timeStamps, new Comparator<Date>() {
          @Override
          public int compare(Date o1, Date o2) {
              return o1.compareTo(o2);
          }
      });

      for (Date timeStamp : timeStamps) {
        timelineMiniGraph += "-> \"" + ISO8601DATEFORMAT.format(timeStamp).replaceAll("T", "\n").replaceAll("Z", "") + "\" ";
      }

    // Throw a bit of a wobbler if we found errors
    if ( !timelineErrors.isEmpty() )
      throw new Exception( timelineErrors );

    // Finish up minigraph
    timelineMiniGraph += "-> future;\n";

    // Write out timeline graph data
    provOUT += "{\nnode[shape=plaintext, fontsize=16];\n";
    provOUT += timelineMiniGraph;
    provOUT += "}\n";

    // Write out timeline layout
    provOUT += timelineLayout;

    provOUT += "\n";

    return provOUT;
  }

  // Private methods -----------------------------------------------------------
  // This blunt method just returns anything that looks vaguely like a PROV relation
  private Set<EDMProvTriple> getProvRelations( EDMProvBaseElement el )
  {
    HashSet<EDMProvTriple> triples = new HashSet<EDMProvTriple>();

    if ( el != null )
    {
      for ( EDMProvTriple triple : el.getTriples() )
      {
        String pred = triple.getPredicate();

        if ( pred.contains("prov:was") || pred.contains("prov:used") ||
             pred.contains("prov:acted") || pred.contains( "prov:had") )
        {
          triples.add( triple );
        }
      }
    }

    return triples;
  }

  private String writeRelation( EDMProvTriple triple )
  {
    String label = triple.getPredicate();
    label = label.replaceFirst("prov:", "");
    return "\"" + triple.getSubject() + "\"" + " -> " +
           "\"" + triple.getObject()  + "\"" +  " [label=\"" +
                  label + "\"];\n";
  }
}
