/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
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
//      Created Date :          17-Apr-2014
//      Created for Project :   EccService
//
/////////////////////////////////////////////////////////////////////////

package uk.co.soton.itinnovation.ecc.service.process;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvWriter;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.dao.EDMProvWriterImpl;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.db.EDMProvStoreWrapper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.*;

import org.slf4j.*;
import java.util.*;




public class LivePROVConsumer
{
    private final Logger lpcLog = LoggerFactory.getLogger(LivePROVConsumer.class);
    
    private boolean        repoInitialised;
    private String         repoID;
    private IEDMProvWriter provWriter;
    
    private HashMap<String,String> nsBaseURIMap; // name x baseURI
    private HashMap<String,String> nsPrefixMap;  // name x prefix
    
    public LivePROVConsumer()
    {
        initialiseNamespaces();
    }
    
    public void createExperimentRepository(UUID expID, String expTitle, Properties repoProps) throws Exception
    {
        // Safety first
        if ( expID == null || expTitle == null || repoProps == null ) throw new Exception( "Could not create experiment repository - parameter(s) null" );
        if ( repoInitialised ) throw new Exception( "Could not create experiment repository - repository already initialised" );
        
        // TO DO: Validate properties
        
        try
        {
            provWriter = null;   
            String expIDVal = expID.toString();
            
            lpcLog.info( "Attempting to create PROV repository for experiment: " + expTitle + ": " + expIDVal );
            
            repoProps.setProperty( "owlim.repositoryID", expIDVal );
            repoProps.setProperty( "owlim.repositoryName", expTitle );
            
            // Create impls for verification/creation of repository
            EDMProvStoreWrapper psw = new EDMProvStoreWrapper( repoProps );
            if ( psw.repositoryExists(expIDVal) )
                throw new Exception( "Could not create repository: it already exists" );
            
            psw.disconnect(); // We're finished with this one
            
            // Repository doesn't exist, so create a new one
            EDMProvWriterImpl provWriterImpl = new EDMProvWriterImpl( repoProps );
            repoID = expIDVal;
            
            // All seems well
            provWriter = provWriterImpl;
            createDefaultNamespaces(expTitle);
            
            repoInitialised = true;
            lpcLog.info( "Repository created OK" );
        }
        catch ( Exception ex )
        {
            String msg = "Could not create experiment repository: " + ex.getMessage();
            lpcLog.error( msg );
            
            throw new Exception( msg, ex );
        }
    }
    
    public void closeCurrentExperimentRepository() throws Exception
    {
        // Safety first
        if ( !repoInitialised ) throw new Exception( "Could not close repository - it has not been created" );
        
        try
        {
            lpcLog.info( "Trying to close current experiment repository" );
            
            provWriter.disconnect();
            
            lpcLog.info( "Repository closed" );
        }
        catch ( Exception ex )
        {
            String msg = "Could not close current repository: " + ex.getMessage();
            lpcLog.error( msg );
            
            throw new Exception( msg, ex );
        }
    }
    
    public void addPROVReport( EDMProvReport report ) throws Exception
    {
        if ( !repoInitialised ) throw new Exception( "Could not add PROV report to repository - repository has not been created");
        if ( report == null ) throw new Exception( "Could not add PROV report to repository - Report is null" );
        
        Collection<EDMTriple> triples = report.getTriples().values();
        if ( triples == null ) throw new Exception( "Could not add PROV report to repository - Triple set is null" );
        
        // Add triples, if there are some
        if ( !triples.isEmpty() )
        {
            provWriter.storeReport( report );
        }
        else
            lpcLog.warn( "PROV report contained no triples - droppped" );
    }
    
    // Private methods ---------------------------------------------------------
    private void initialiseNamespaces()
    {
        nsPrefixMap  = new HashMap<String,String>();
        nsBaseURIMap = new HashMap<String, String>();
        
        nsBaseURIMap.put( "foaf", "http://xmlns.com/foaf/0.1/" );
        nsBaseURIMap.put( "sioc", "http://rdfs.org/sioc/ns#" );
        nsBaseURIMap.put( "prov", "http://www.w3.org/ns/prov-o#/" );
        nsBaseURIMap.put( "experimedia", "experimedia.rdf" );
        
        nsPrefixMap.put( "foaf", "http://xmlns.com/foaf/0.1/" );
        nsPrefixMap.put( "sioc", "http://rdfs.org/sioc/ns#" );
        nsPrefixMap.put( "prov", "http://www.w3.org/ns/prov#" );
        nsPrefixMap.put( "experimedia", "http://it-innovation.soton.ac.uk/ontologies/experimedia#" );
    }
    
    private void createDefaultNamespaces(String expTitle) throws Exception
    {
        EDMProvWriterImpl writerImpl = (EDMProvWriterImpl) provWriter;
        EDMProvStoreWrapper psw = writerImpl.getEDMProvStoreWrapper();
        
        if ( psw == null )        throw new Exception( "Cannot create default namespaces: ProvStoreWrapper is null" );
        if ( !psw.isConnected() ) throw new Exception( "Cannot create default namespaces: ProvStoreWrapper is not conencted" );
		
		psw.createNewRepository(repoID, expTitle);
        
        try
        {
            // FOAF
            psw.importOntologyToKnowledgeBase( nsBaseURIMap.get("foaf"),
                                               nsPrefixMap.get("foaf"), 
                                               "foaf", LivePROVConsumer.class );

            // SIOC
            psw.importOntologyToKnowledgeBase( nsBaseURIMap.get("sioc"),
                                               nsPrefixMap.get("sioc"),
                                               "sioc", LivePROVConsumer.class );

            // PROV
            psw.importOntologyToKnowledgeBase( nsBaseURIMap.get("prov"),
                                               nsPrefixMap.get("prov"),
                                               "prov", LivePROVConsumer.class );

            // EXPERIMEDIA
            psw.importOntologyToKnowledgeBase( nsBaseURIMap.get("experimedia"),
                                               nsPrefixMap.get("experimedia"),
                                               "experimedia", LivePROVConsumer.class );
        }
        catch ( Exception ex )
        {
            String msg = "Could not create default namespaces: " + ex.getMessage();
            lpcLog.error( msg );
            
            throw new Exception( msg, ex );
        }
    }
    
}
