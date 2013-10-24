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
//      Created Date :          23-Oct-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.*;

import org.openprovenance.prov.model.*;
import org.openprovenance.prov.xml.ProvFactory;

import java.util.*;




/**
 * PROVTBDataManager is an internal data manager for PROVToolBox data - it should not 
 * be used for client side development. This container assists in the data inter-operation
 * with ECC PROV data.
 * 
 * @author Simon Crowle
 */
class PROVTBDataManager
{ 
  private ProvFactory               ptFactory;
  private HashMap<String, Entity>   ptEntities;
  private HashMap<String, Agent>    ptAgents;
  private HashMap<String, Activity> ptActivities;
  private HashSet<Statement>        ptStatements;
  
  
  public PROVTBDataManager()
  {
    ptFactory    = new ProvFactory();
    ptEntities   = new HashMap<String, Entity>();
    ptAgents     = new HashMap<String, Agent>();
    ptActivities = new HashMap<String, Activity>();
    ptStatements = new HashSet<Statement>();
  }
  
  public Entity createPTBEntity( EDMProvBaseElement el ) throws Exception
  {
    // Safety first
    if ( el == null ) throw new Exception( "Could not create PTB Entity: input param is null" ); 
    if ( ptEntities.containsKey(el.getIri()) ) throw new Exception ( "Could not create PTB Entity: it already exists" );
    
    String iri = el.getIri();
    Entity entity = ptFactory.newEntity( iri, iri );
    ptEntities.put( iri, entity );
    
    return entity;
  }
  
  public Agent createPTBAgent( EDMProvBaseElement el ) throws Exception
  {
    // Safety first
    if ( el == null ) throw new Exception( "Could not create PTB Agent: input param is null" );
    if ( ptAgents.containsKey(el.getIri()) ) throw new Exception ( "Could not create PTB Agent: it already exists" );
    
    String iri = el.getIri();
    Agent agent = ptFactory.newAgent( iri, iri );
    ptAgents.put( iri, agent );
    
    return agent;
  }
  
  public Activity createPTBActivity( EDMProvBaseElement el ) throws Exception
  {
    // Safety first
    if ( el == null ) throw new Exception( "Could not create PTB Activity: input param is null" );
    if ( ptActivities.containsKey(el.getIri()) ) throw new Exception ( "Could not create PTB Activity: it already exists" );
    
    String iri = el.getIri();
    Activity activity = ptFactory.newActivity( iri, iri );
    ptActivities.put( iri, activity );
    
    return activity;
  }
  
  public Statement createEntityRelation( EDMProvTriple triple ) throws Exception
  {
    Statement statement = null;
    
    // Safety first
    if ( triple == null ) throw new Exception( "Could not create Entity statement: input triple is null" );
   
    // Supported relations
    if ( triple.hasPredicate("prov:wasQuotedFrom") )
    {
      // TODO: PROVToolBox support?
    }
    else if ( triple.hasPredicate("prov:hadPrimarySource") )
    {
      // TODO: PROVToolBox support?
    }
    else if ( triple.hasPredicate("prov:wasRevisionOf") )
    {
      // TODO: PROVToolBox support?
    }
    else if ( triple.hasPredicate("prov:wasGeneratedBy") )
    {
      Entity   lhs = ptEntities.get( triple.getSubject() );
      Activity rhs = ptActivities.get( triple.getObject() );
      
      if ( lhs != null && rhs != null )
        statement = ptFactory.newWasGeneratedBy( lhs, "UNKNONW ROLE", rhs );
    }
    else if ( triple.hasPredicate("prov:generatedAtTime") )
    {
      // TODO: PROVToolBox support?
    }
    else if ( triple.hasPredicate("prov:wasDerivedFrom") )
    {
      Entity lhs = ptEntities.get( triple.getSubject() );
      Entity rhs = ptEntities.get( triple.getObject() );
      
      if ( lhs != null && rhs != null )
        statement = ptFactory.newWasDerivedFrom( lhs, rhs );
    }
    else if ( triple.hasPredicate("prov:wasInvalidatedBy") )
    {
      // TODO: PROVToolBox support?
    }
    else if ( triple.hasPredicate("prov:invalidatedAtTime") )
    {
      // TODO: PROVToolBox support?
    }
    
    if ( statement != null )
      ptStatements.add( statement );
    else
      throw new Exception( "Currently unsupported Entity relation: " + triple.getPredicate() );

    return statement;
  }
  
  public Statement createAgentRelation( EDMProvTriple triple ) throws Exception
  {
    Statement statement = null;
    
    // Safety first
    if ( triple == null ) throw new Exception( "Could not create Agent relation: input triple is null" );
    
    // Supported relations
    if ( triple.hasPredicate("prov:wasAssociatedWith") )
    {
      Activity lhs = ptActivities.get( triple.getSubject() );
      Agent    rhs = ptAgents.get( triple.getObject() );
      
      if ( lhs != null && rhs != null )
        statement = ptFactory.newWasAssociatedWith( "UNKNOWN ID", lhs, rhs );
    }
    
    if ( statement != null )
      ptStatements.add( statement );
    else
      throw new Exception( "Currently unsupported Agent relation: " + triple.getPredicate() );
    
    return statement;
  }
  
  public Statement createActivityRelation( EDMProvTriple triple ) throws Exception
  {
    Statement statement = null;
    
    // Safety first
    if ( triple == null ) throw new Exception( "Could not create Activity relation: input triple is null" );
    
    // Supported relations
    if ( triple.hasPredicate("prov:wasStartedBy") )
    {
      // TODO: PROVToolBox support?
    }
    else if ( triple.hasPredicate("prov:wasEndedBy") )
    {
      // TODO: PROVToolBox support?
    }
    else if ( triple.hasPredicate("prov:used") )
    {
      Activity lhs = ptActivities.get( triple.getSubject() );
      Entity   rhs = ptEntities.get( triple.getObject() );
      
      if ( lhs != null && rhs != null )
        statement = ptFactory.newUsed( lhs, "UNKNOWN ROLE", rhs );
    }
    else if ( triple.hasPredicate("prov:wasInformedBy") )
    {
      Activity lhs = ptActivities.get( triple.getSubject() );
      Activity rhs = ptActivities.get( triple.getObject() );
      
      if ( lhs != null && rhs != null )
        statement = ptFactory.newWasInformedBy( lhs, rhs );
    }
    else if ( triple.hasPredicate("prov:wasInfluencedBy") )
    {
      // TODO: PROVToolBox support?
    }
    else if ( triple.hasPredicate("prov:actedOnBehalfOf") )
    {
      // TODO: PROVToolBox support?
    }
    
    if ( statement != null )
      ptStatements.add( statement );
    else
      throw new Exception( "Currently unsupported Activity relation: " + triple.getPredicate() );

    return statement;
  }
  
  public Entity getEntity( String iri ) throws Exception
  { return ptEntities.get( iri ); }
  
  public Agent getAgent( String iri ) throws Exception
  { return ptAgents.get( iri ); }
  
  public Activity getActivity( String iri ) throws Exception
  { return ptActivities.get( iri ); }

  public Document createPTDocument()
  {
    // Create PROV ToolBox arrayed data
    Entity[]    ptDocEntities   = new Entity[ ptEntities.size() ];
    Agent[]     ptDocAgents     = new Agent[ ptAgents.size() ];
    Activity[]  ptDocActivities = new Activity[ ptActivities.size() ];
    Statement[] ptDocStatements = new Statement[ ptStatements.size() ];

    // Populate
    ptEntities.values().toArray( ptDocEntities );
    ptAgents.values().toArray( ptDocAgents );
    ptActivities.values().toArray( ptDocActivities );
    ptStatements.toArray( ptDocStatements );

    // Finally create the document
    Document doc = ptFactory.newDocument( ptDocActivities,
                                          ptDocEntities,
                                          ptDocAgents,
                                          ptDocStatements );

    return doc;
  }
}
