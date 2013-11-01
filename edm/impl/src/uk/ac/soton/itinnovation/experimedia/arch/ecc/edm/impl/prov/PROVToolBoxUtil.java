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
//      Created Date :          17-Oct-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.*;

import org.openprovenance.prov.model.*;
import org.openprovenance.prov.dot.ProvToDot;

import java.util.*;
import java.io.File;




public class PROVToolBoxUtil
{
    private static IECCLogger ptuLogger = Logger.getLogger( PROVToolBoxUtil.class );
    
  
    public static EDMProvReport aggregateReport( EDMProvReport lhs, EDMProvReport rhs ) throws Exception
    {
      // Safety first
      if ( lhs == null || rhs == null ) throw new Exception( "Could not aggregate PROV report - lhs/rhs report is null" );

      // Create indexed LHS & RHS elements
      HashMap<UUID, EDMProvBaseElement> lhsElements = new HashMap<UUID, EDMProvBaseElement>();
      HashMap<UUID, EDMProvBaseElement> rhsElements = new HashMap<UUID, EDMProvBaseElement>();
      
      for ( EDMProvBaseElement be : lhs.getProvElements().values() )
        lhsElements.put( be.getInstanceID(), be );
      
      for ( EDMProvBaseElement be : rhs.getProvElements().values() )
        rhsElements.put( be.getInstanceID(), be );
      
      // Create target report
      EDMProvReport aggReport = new EDMProvReport();
      HashMap<String, EDMProvBaseElement> aggElements = aggReport.getProvElements();
      
      // Create a set of unique elements from the LHS
      HashSet<UUID> lhsIDs = new HashSet<UUID>();
      for ( EDMProvBaseElement be : lhsElements.values() )
      {
        UUID lhsID = be.getInstanceID();
        if ( !lhsIDs.contains(lhsID) )
        {
          aggElements.put( be.getIri(), be );
          lhsIDs.add( lhsID );
        }
      }
      
      // Update lhs elements from updates in RHS
      for ( EDMProvBaseElement be : rhsElements.values() )
      {
        UUID elID = be.getInstanceID();
        if ( lhsIDs.contains(elID) )
        {
          EDMProvBaseElement lhsEl = lhsElements.get( elID );
          EDMProvBaseElement rhsEl = rhsElements.get( elID );
          
          try { integratePROVElement( lhsEl, rhsEl ); }
          catch ( Exception ex ) { throw ex; } // throw this problem upwards
        }
      }
      
      // And new elements from RHS
      for ( EDMProvBaseElement be : rhsElements.values() )
      {
        UUID rhsID = be.getInstanceID();
        if ( !lhsIDs.contains(rhsID) )
        {
          aggElements.put( be.getIri(), be );
          lhsIDs.add( rhsID );
        }
      }
      
      return aggReport;
    }
    
    /**
     * Creates a PROV ToolBox Document using the ECC PROV Model classes
     * 
     * @param eccPROVReport - PROV report
     * @return              - PROV Toolbox Document (empty if eccPROVReport is erroneous)
     */
    public static Document createPTBDocument( EDMProvReport eccPROVReport ) throws Exception
    {
      // Safety first
      if ( eccPROVReport == null ) throw new Exception( "Could not create PROVToolBox Document: ECC PROV report is null" );
      
      PROVTBDataManager ptdb = new PROVTBDataManager();
      
      HashMap<String, EDMProvBaseElement> provElements = eccPROVReport.getProvElements();
      
      createPTBElements( provElements, ptdb );
      createPTBStatements( provElements, ptdb );
      
      return ptdb.createPTDocument();
    }
        
    /**
     * Creates a DOT document based on a PROV ToolBox document.
     * 
     * @param target      - PROV ToolBox document
     * @param fileTarget  - Path and filename of expected PDF document
     * @throws Exception  - Throws on target data, path or file writing problems
     */
    public static void createVizDOTFile( Document target, String fileTarget ) throws Exception
    {
      // Safety first
      if ( target == null || fileTarget == null ) throw new Exception( "Could not create PROV DOT file because input param(s) are null" );
      
      ProvToDot ptd = new ProvToDot( true );
      try
      {
        File outFile = new File( fileTarget + ".dot" );
        
        ptd.convert( target, outFile, "PROV Visualisation" );
      }
      catch( Exception ex )
      { throw ex; } // Throw exception upwards
    }
    
    // Private methods --------------------------------------------------------- 
    private static void integratePROVElement( EDMProvBaseElement lhsOUT, EDMProvBaseElement rhs ) throws Exception
    {
      // Safety first
      if ( lhsOUT == null || rhs == null ) throw new Exception( "Could not integrate PROV elements: lhs/rhs is null" );
      
      if ( !lhsOUT.getIri().equals( rhs.getIri()) ) throw new Exception( "Could not integrate PROV elements: IRIs are not identical" );
      
      LinkedList<EDMProvTriple> newTriples = new LinkedList<EDMProvTriple>();
      LinkedList<EDMProvTriple> rhsTriples = rhs.getTriples();
      
      // Create a list of new triples for this element
      for ( EDMProvTriple triple : rhsTriples )
        if ( !lhsOUT.contains(triple) ) newTriples.add( triple );
      
      // Add new triples into OUT element
      for ( EDMProvTriple triple : newTriples )
        lhsOUT.addTriple( triple.getSubject(),
                            triple.getPredicate(),
                            triple.getObject() );
        
    }
    
    private static void createPTBElements( HashMap<String, EDMProvBaseElement> provElements,
                                           PROVTBDataManager ptdb )
    {
      for ( EDMProvBaseElement el : provElements.values() )
      {
        try
        {
          switch ( el.getProvType() )
          {
            case ePROV_ENTITY   : ptdb.createPTBEntity( el ); break;
            case ePROV_AGENT    : ptdb.createPTBAgent( el ); break;
            case ePROV_ACTIVITY : ptdb.createPTBActivity( el ); break;
          }
        }
        catch ( Exception ex )
        { ptuLogger.error( "Could not create PROVToolBox element:" + ex.getMessage() ); }
      }
    }
    
    private static void createPTBStatements( HashMap<String, EDMProvBaseElement> provElements,
                                             PROVTBDataManager ptdb )
    {
      for ( EDMProvBaseElement el : provElements.values() )
      {
          switch ( el.getProvType() )
          {
            case ePROV_ENTITY :
            {
              for ( EDMProvTriple triple : el.getTriples() )
                try
                { 
                  ptdb.createEntityRelation( triple ); 
                }
                catch (Exception ex) { ptuLogger.warn( "Could not create relation: " + ex.getMessage() ); }
              
            } break;

            case ePROV_AGENT :
            {
              for ( EDMProvTriple triple : el.getTriples() )
                try
                {
                  ptdb.createAgentRelation( triple );
                }
                catch (Exception ex) { ptuLogger.warn( "Could not create relation: " + ex.getMessage() );
            
              }
            } break;

            case ePROV_ACTIVITY :
            {
              for ( EDMProvTriple triple : el.getTriples() )
                try
                {
                  ptdb.createActivityRelation( triple );
                }
                catch (Exception ex) { ptuLogger.warn( "Could not create relation: " + ex.getMessage() );

            } break;
          }
        }
      }
    }
}

