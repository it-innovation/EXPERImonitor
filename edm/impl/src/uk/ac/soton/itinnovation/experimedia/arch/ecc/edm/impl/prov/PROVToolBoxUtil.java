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
      
      EDMProvFactory factory = EDMProvFactory.getInstance();
      factory.clear();
      factory.loadReport(eccPROVReport);
      
      createPTBElements( factory.getAllElements(), ptdb );
      createPTBStatements( factory.getAllElements(), ptdb );
      
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
    
    private static void createPTBElements( HashMap<String, EDMProvBaseElement> hashMap,
                                           PROVTBDataManager ptdb )
    {
      for ( EDMProvBaseElement e : hashMap.values() )
      {
        try
        {
          switch ( e.getProvType() )
          {
            case ePROV_ENTITY   : ptdb.createPTBEntity( e ); break;
            case ePROV_AGENT    : ptdb.createPTBAgent( e ); break;
            case ePROV_ACTIVITY : ptdb.createPTBActivity( e ); break;
          }
        }
        catch ( Exception ex )
        { ptuLogger.error( "Could not create PROVToolBox element:" + ex.getMessage() ); }
      }
    }
    
    private static void createPTBStatements( HashMap<String, EDMProvBaseElement> hashMap,
                                             PROVTBDataManager ptdb )
    {
      for ( EDMProvBaseElement el : hashMap.values() )
      {
          switch ( el.getProvType() )
          {
            case ePROV_ENTITY :
            {
              for ( EDMTriple triple : el.getTriples().values() )
                try
                { 
                  ptdb.createEntityRelation( triple ); 
                }
                catch (Exception ex) { ptuLogger.warn( "Could not create relation: " + ex.getMessage() ); }
              
            } break;

            case ePROV_AGENT :
            {
              for ( EDMTriple triple : el.getTriples().values() )
                try
                {
                  ptdb.createAgentRelation( triple );
                }
                catch (Exception ex) { ptuLogger.warn( "Could not create relation: " + ex.getMessage() );
            
              }
            } break;

            case ePROV_ACTIVITY :
            {
              for ( EDMTriple triple : el.getTriples().values() )
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

