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
//      Created By :            Stefanie Wiegand
//      Created Date :          2014-01-20
//      Created for Project :   Experimedia
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.edmprov.owlim.common.NoSuchRepositoryException;
import uk.ac.soton.itinnovation.edmprov.owlim.common.RepositoryExistsException;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.db.EDMProvStoreWrapper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvDataStore;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvElementReader;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvRelationReader;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvWriter;

public final class EDMProvDataStoreImpl implements IEDMProvDataStore {
    
    private IEDMProvWriter provWriter;
    private IEDMProvElementReader provElementReader;
    private IEDMProvRelationReader provRelationReader;
    
    private EDMProvStoreWrapper edmProvStoreWrapper;
    private Properties props;
    private final Logger logger;
    
    public EDMProvDataStoreImpl() {
		
		logger = Logger.getLogger(EDMProvDataStoreImpl.class);
		logger.setLevel(Level.INFO);	//TODO: remove
		
		try {
			logger.debug("Loading properties file");
			this.props.load(EDMProvDataStoreImpl.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			logger.error("Error loading properties file", e);
		}
		
		init();
    }
	
	public EDMProvDataStoreImpl(Properties props) {

	    logger = Logger.getLogger(EDMProvDataStoreImpl.class);
		this.props = props;

		this.init();
	}
    
    private void init() {

        provWriter = new EDMProvWriterImpl(props);
        provElementReader = new EDMProvElementReaderImpl(props);
        provRelationReader = new EDMProvRelationReaderImpl(props);
		
        connect();
		
		createRepository(props.getProperty("owlim.repositoryID"), props.getProperty("owlim.repositoryName"));
    }

	@Override
	public void connect() {
		try {
            edmProvStoreWrapper = new EDMProvStoreWrapper(props);
        } catch (Exception e) {
			logger.error("Error connecting to sesame server at " + props.getProperty("owlim.sesameServerURL"), e);
        }
	}

	@Override
	public void createRepository(String repositoryID, String repositoryName) {
		try {
			logger.debug("Creating owlim repository");
			edmProvStoreWrapper.createNewRepository(repositoryID, repositoryName);
		} catch (RepositoryExistsException e) {
			logger.info("Repository " + repositoryID + " already existed - fine, will continue...");
			//TODO: clear?
		} catch (Exception e) {
			logger.error("Error creating repository \"" + repositoryID + "\" on sesame server", e);
		}
	}

	@Override
	public void deleteRepository(String repositoryID) {
		try {
			logger.info("Deleting repository");
			edmProvStoreWrapper.deleteRepository(repositoryID);
		} catch (NoSuchRepositoryException e) {
			logger.debug("Repository doesn't exist");
		} catch (Exception e) {
			logger.error("Error deleting repository " + repositoryID, e);
		}
	}
	
	@Override
	public void disconnect() {
		if ((edmProvStoreWrapper != null) && edmProvStoreWrapper.isConnected()) {
			logger.warn("EDMProvStoreWrapper has still got an open connection - disconnecting now");
			edmProvStoreWrapper.disconnect();
		}
		provWriter.disconnect();
		provElementReader.disconnect();
		provRelationReader.disconnect();
	}
	
	public void importOntology(String ontologypath, String baseURI, String prefix, Class resourcepathclass) {

		provWriter.importOntology(ontologypath, baseURI, prefix, resourcepathclass);
		
//		if (((EDMProvWriterImpl)provWriter).getEDMProvStoreWrapper().getRepositoryNamespaces()!=null &&
//			((EDMProvWriterImpl)provWriter).getEDMProvStoreWrapper().getRepositoryNamespaces().containsKey(props.getProperty("owlim.repositoryID"))) {
///			System.out.println("prefixes after import:");
//			for (Map.Entry<String, String> e: ((EDMProvWriterImpl)provWriter).getEDMProvStoreWrapper().getRepositoryNamespaces().get(props.getProperty("owlim.repositoryID")).entrySet()) {
//				System.out.println( -   e.getKey() + ": " + e.getValue());
//			}
//		}
		logger.debug("Sharing prefixes between prov store access classes");
		((EDMProvElementReaderImpl)provElementReader).getEDMProvStoreWrapper().setRepositoryNamespaces(
				((EDMProvWriterImpl)provWriter).getEDMProvStoreWrapper().getRepositoryNamespaces());
		((EDMProvRelationReaderImpl)provRelationReader).getEDMProvStoreWrapper().setRepositoryNamespaces(
				((EDMProvWriterImpl)provWriter).getEDMProvStoreWrapper().getRepositoryNamespaces());
		edmProvStoreWrapper.setRepositoryNamespaces(
				((EDMProvWriterImpl)provWriter).getEDMProvStoreWrapper().getRepositoryNamespaces());
	}
	
	public LinkedList<HashMap<String, String>> query(String sparql) {
		return this.edmProvStoreWrapper.query(sparql);
	}

	public IEDMProvWriter getProvWriter() {
		return provWriter;
	}

	public IEDMProvElementReader getProvElementReader() {
		return provElementReader;
	}

	public IEDMProvRelationReader getProvRelationReader() {
		return provRelationReader;
	}

	public EDMProvStoreWrapper getEDMProvStoreWrapper() {
		return edmProvStoreWrapper;
	}
	
}
