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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.edmprov.owlim.common.NoSuchRepositoryException;
import uk.ac.soton.itinnovation.edmprov.owlim.common.OntologyDetails;
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
    
    private EDMProvStoreWrapper sCon;
    private final Properties props;
    private final Logger logger;
    
    public EDMProvDataStoreImpl() {
        
        props = new Properties();
        logger = Logger.getLogger(EDMProvDataStoreImpl.class);
        
        provWriter = createIEDMProvWriter(props);
        provElementReader = createIEDMProvElementReader(props);
        provRelationReader = createIEDMProvRelationReader(props);
        
        init();  
    }
    
    private void init() {
        
        try {
            logger.debug("Loading properties file");
            props.load(EDMProvDataStoreImpl.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            logger.error("Error loading properties file", e);
        }
        
        connect();
		
		createRepository(props.getProperty("owlim.repositoryID"), props.getProperty("owlim.repositoryName"));
    }

    @Override
    public IEDMProvWriter createIEDMProvWriter(Properties props) {
        return new EDMProvWriterImpl(props);
    }

    @Override
    public IEDMProvElementReader createIEDMProvElementReader(Properties props) {
        return new EDMProvElementReaderImpl(props);
    }

    @Override
    public IEDMProvRelationReader createIEDMProvRelationReader(Properties props) {
        return new EDMProvRelationReaderImpl(props);
    }

	@Override
	public void connect() {
		try {
            sCon = new EDMProvStoreWrapper(props);
        } catch (Exception e) {
			logger.error("Error connecting to sesame server at " + props.getProperty("owlim.sesameServerURL"), e);
        }
	}

	@Override
	public void createRepository(String repositoryID, String repositoryName) {
		try {
			logger.debug("Creating owlim repository");
			sCon.createNewRepository(repositoryID, repositoryName);
		} catch (RepositoryExistsException e) {
			logger.info("Repository " + repositoryID + " already existed - fine, will continue...");
			//TODO: clear?
		} catch (Exception e) {
			logger.error("Error creating repository \"" + repositoryID + "\" on sesame server", e);
		}
	}

	@Override
	public void importOntology(String ontologypath, String baseURI, String prefix) {
		logger.info("Importing ontology " + prefix + " (" + baseURI + ")");
		OntologyDetails od = new OntologyDetails();
		File ontfile = new File(ontologypath);
		if (!ontfile.exists()) {
			//try URL
			if (ontologypath.startsWith("http://")) {
				try {
					URL remoteOntology = new URL(ontologypath);
					od.setURL(remoteOntology);       
				} catch (MalformedURLException e) {
					logger.error("Error loading ontology from URL " + ontologypath, e);
				}
			//try file from classpath
			} else {
				String resourcepath = EDMProvDataStoreImpl.class.getClassLoader().getResource(ontologypath).getPath();
				try {
					ontfile = new File(resourcepath);
					od.setURL(ontfile.toURI().toURL());
				} catch (MalformedURLException e) {
					logger.error("Error getting ontology from resource path", e);
				}
			}
			
		} else {
			try {
				od.setURL(ontfile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.error("Ontology path invalid", e);
			}
		}
		od.setBaseURI(baseURI);
		od.setPrefix(prefix);
		try {
			sCon.addOntology(props.getProperty("owlim.repositoryID"), od);
		} catch (Exception e) {
			logger.error("Error importing ontology", e);
		}
	}

	@Override
	public void clearRepository(String repositoryID) {
		String sparql = "";	//TODO
		sCon.query(sparql);
	}

	@Override
	public void deleteRepository(String repositoryID) {
		try {
			logger.info("Deleting repository");
			sCon.deleteRepository(repositoryID);
		} catch (NoSuchRepositoryException e) {
			logger.debug("Repository doesn't exist");
		} catch (Exception e) {
			logger.error("Error deleting repository " + repositoryID, e);
		}
	}

}
