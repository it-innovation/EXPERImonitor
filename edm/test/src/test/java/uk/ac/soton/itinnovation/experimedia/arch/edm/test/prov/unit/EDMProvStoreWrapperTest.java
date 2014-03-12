/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
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
//      Created Date :          2014-03-05
//      Created for Project :   Experimedia
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.prov.unit;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.edmprov.owlim.common.RelationshipType;
import uk.ac.soton.itinnovation.edmprov.owlim.common.RepositoryExistsException;
import uk.ac.soton.itinnovation.edmprov.owlim.common.SesameException;
import uk.ac.soton.itinnovation.edmprov.owlim.common.Triple;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.db.EDMProvStoreWrapper;

/**
 * This test class tests only the plain mechanics of the EDMProv store - data consistency checks
 * are only included in the more specific test classes.
 * 
 * @author Stefanie Wiegand
 */
@RunWith(JUnit4.class)
public class EDMProvStoreWrapperTest extends TestCase {
    private EDMProvStoreWrapper store;
    private static Logger logger;
	private static Properties props;
	private static final String repoID = "testrepo";
	private static final String repoName = "This is a test repository";

    @BeforeClass
    public static void beforeClass() {
		
        logger = Logger.getLogger(EDMProvStoreWrapperTest.class);
        logger.info("EDMProvStoreWrapper tests executing...");
    }
	
	@Before
	public void before() {
		
		try {
            props = getProperties();
			//overwrite repo id and name for test
			props.setProperty("owlim.repositoryID", repoID);
			props.setProperty("owlim.repositoryName", repoName);
			
        } catch (Exception ex) {
            logger.error("Failed to get properites.", ex);
        }
		
		if ((store == null)) {
			try {
				store = new EDMProvStoreWrapper(props);
			} catch (Exception e) {
				logger.error("Error connecting to store", e);
			}
        }
		
		try {
			store.createNewRepository(repoID, repoName);
		} catch (RepositoryExistsException e) {
			logger.debug("Repository already exists - clearing contents");
			try {
				store.clearRepository(repoID);
			} catch (Exception ex) {
				logger.error("Error clearing repository", ex);
			}
		} catch (Exception e) {
			logger.error("Error creating repository", e);
		}
	
	}
	
	@After
	public void after() {
		
		logger.debug("Shutting down store after test");

		if (store!=null && store.isConnected()) {

			try {
				if (store.repositoryExists(repoID)) {
					store.deleteRepository(repoID);	
				}
			} catch (Exception ex) {
				logger.error("Error deleting repository", ex);
			}

			store.disconnect();
			store = null;
		}
	}

	@Test
	public void testClearRepository() {
		try {
			store.clearRepository(repoID);
		} catch (Exception e) {
			logger.error("Error clearing repository", e);
			fail("Error clearing repository");
		}
		//no sanity check needed, clearRepository(); throws Exception if deletion fails
	}
	
	@Test
	public void testDeleteRepository() {
		try {
			store.deleteRepository(repoID);
		} catch (Exception e) {
			logger.error("Error deleting repository", e);
			fail("Error deleting repository");
		}
		try {
			if (store.repositoryExists(repoID)) {
				fail("Repository still exists");
			}
		} catch (SesameException e) {
			fail("Error connecting to store to verify repository has been deleted");
			logger.error("Error connecting to store to verify repository has been deleted", e);
		}
	}
	
	@Test
	public void testCreateRepository() {
		String testrepo = "test" + UUID.randomUUID().toString();
		try {
			store.createNewRepository(testrepo, "this is a test repo");
		} catch (Exception e) {
			logger.error("Error creating repository", e);
			fail("Error creating repository");
		}
		try {
			if (!store.repositoryExists(testrepo)) {
				fail("Repository was not created");
			}
		} catch (SesameException e) {
			fail("Error connecting to store to verify repository has been created");
			logger.error("Error connecting to store to verify repository has been created", e);
		}
		try {
			store.deleteRepository(testrepo);
		} catch (Exception ex) {
			logger.error("Error deleting test repository", ex);
		}
	}
	
	@Test
	public void testImportOntology() {

		//from file
		try {
			store.importOntologyToKnowledgeBase("experimedia.rdf",
				"http://it-innovation.soton.ac.uk/ontologies/experimedia#", "experimedia", EDMProvStoreWrapperTest.class);
		} catch (Exception e) {
			logger.error("Error importing ontology from file", e);
			fail("Error importing ontology from file");
		}
		
		//check if import was successful
		if (!store.getNamespacesForRepository(repoID).get("experimedia").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#")) {
			fail("Ontology was not imported properly from file");
		}
		
		
		//from url
		try {
			store.importOntologyToKnowledgeBase("http://xmlns.com/foaf/0.1/",
				"http://xmlns.com/foaf/0.1/", "foaf", EDMProvStoreWrapperTest.class);
		} catch (Exception e) {
			logger.error("Error importing ontology from URL", e);
			fail("Error importing ontology from URL");
		}
		
		//check if import was successful
		if (!store.getNamespacesForRepository(repoID).get("foaf").equals("http://xmlns.com/foaf/0.1/")) {
			fail("Ontology was not imported properly from file");
		}

	}
		
	@Test
	public void testStoreTriples() {
		try {
			storeTestTriples();
		} catch (Exception e) {
			logger.error("Error storing triples", e);
			fail("Error storing triples");
		}
		//no sanity check needed, storeTestTriples(); throws Exception if insertion fails
	}
	
	@Test
	public void testLoadPrefixes() {
		try {
			store.loadPrefixes();
		} catch (Exception e) {
			logger.error("Error loading prefixes from store", e);
			fail("Error loading prefixes from store");
		}
		if (store.getPrefixes()==null || store.getPrefixes().equals("") || !store.getPrefixes().contains("PREFIX")) {
			fail("No prefixes loaded");
		}
	}
	
	@Test
	public void testQuery() {
		LinkedList<HashMap<String, String>> result = null;
		try {
			storeTestTriples();
			String sparql = "SELECT * WHERE { "
					+ "owl:Test ?p ?o . "
					+ "}";
			 result = store.query(sparql);
		} catch (Exception e) {
			logger.error("Error querying repository", e);
			fail("Error querying repository");
		}
		
		//sanity check: should at least contain the three test triples:
		/*
		!	[p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/2002/07/owl#Class]
			[p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/2000/01/rdf-schema#Class]
		!	[p=http://www.w3.org/2000/01/rdf-schema#subClassOf, o=http://www.w3.org/2002/07/owl#Thing]
			[p=http://www.w3.org/2000/01/rdf-schema#subClassOf, o=http://www.w3.org/2002/07/owl#Test]
		!	[p=http://www.w3.org/2000/01/rdf-schema#comment, o="owl:Thing"^^<http://www.w3.org/2001/XMLSchema#String>]
		*/
		int hits = 0;
		for (HashMap<String, String> row: result) {
			if (row.get("p").equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") &&
					row.get("o").equals("http://www.w3.org/2002/07/owl#Class")) {
				hits++;
			}
			if (row.get("p").equals("http://www.w3.org/2000/01/rdf-schema#subClassOf") &&
					row.get("o").equals("http://www.w3.org/2002/07/owl#Thing")) {
				hits++;
			}
			if (row.get("p").equals("http://www.w3.org/2000/01/rdf-schema#comment") &&
					row.get("o").equals("\"owl:Thing\"^^<http://www.w3.org/2001/XMLSchema#String>")) {
				hits++;
			}
		}
		if (hits < 3) {
			fail("Unexpected query results");
		}
	}
	
	@Test
	public void testDisconnect() {
		try {
			store.disconnect();
		} catch (Exception e) {
			logger.error("Error disconnecting from store", e);
			fail("Error disconnecting from store");
		}
		if (store.isConnected()) {
			fail("Store is still connected");
		}
	}

	//helper methods
	private void storeTestTriples() throws Exception {
		//adding triples of different types in "standard" namespaces
		List<Triple> triples = new LinkedList<Triple>();
		triples.add(new Triple("owl:Test", "rdf:type", "owl:Class", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple("owl:Test", "rdfs:subClassOf", "owl:Thing", RelationshipType.OBJECT_PROPERTY));
		triples.add(new Triple("owl:Test", "rdfs:comment", "\"owl:Thing\"^^xsd:String", RelationshipType.ANNOTATION_PROPERTY));
		store.addTriples(repoID, triples);
	}

	public Properties getProperties() {
        Properties properties = new Properties();
        
        try {
            properties.load(EDMProvStoreWrapperTest.class.getClassLoader().getResourceAsStream(EDMProvTestSuite.getPropertiesFile()));
        } catch (IOException ex) {
            logger.error("Error with loading properties file", ex);
            return null;
        }
        
        return properties;
    }
		
}
