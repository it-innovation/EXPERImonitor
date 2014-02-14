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
//      Created By :            Stefanie Wiegand
//      Created Date :          12-Nov-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.edm.test.prov;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMActivity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMEntity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvBaseElement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvDataContainer;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMProvPersistenceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.dao.EDMProvDataStoreImpl;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.dao.SPARQLProvTranslator;


public class KnowledgeBaseTest {

	private EDMProvFactory factory;
	
	private EDMProvPersistenceFactory persistenceFactory;
	private EDMProvDataStoreImpl store;
	
	private static final Properties props = new Properties();
	private static final Logger logger = Logger.getLogger(KnowledgeBaseTest.class);

	private KnowledgeBaseTest() {

		logger.info("Starting Prov Test");
		
		try {
			logger.info("Loading properties file");
			props.load(KnowledgeBaseTest.class.getClassLoader().getResourceAsStream("prov.properties"));
		} catch (IOException e) {
			logger.error("Error loading properties file", e);
		}

		try {
			logger.info("Filling factory with example data");
			fillFactory();

			logger.info("Connecting to Sesame server");
			persistenceFactory = EDMProvPersistenceFactory.getInstance(props);
			store = persistenceFactory.getStore();

			logger.info("Deleting repository");
			store.deleteRepository(props.getProperty("owlim.repositoryID"));
			
			logger.info("Creating owlim repository");
			store.createRepository(props.getProperty("owlim.repositoryID"), props.getProperty("owlim.repositoryName"));

			logger.info("Adding ontologies");
			//done via store wrapper method to share namespaces between store access objects!
			store.importOntology("experimedia.rdf",
				"http://it-innovation.soton.ac.uk/ontologies/experimedia#", "experimedia", KnowledgeBaseTest.class);
			store.importOntology("http://www.w3.org/ns/prov-o#/",
				"http://www.w3.org/ns/prov#", "prov", KnowledgeBaseTest.class);
			store.importOntology("http://xmlns.com/foaf/0.1/",
				"http://xmlns.com/foaf/0.1/", "foaf", KnowledgeBaseTest.class);
			store.importOntology("http://rdfs.org/sioc/ns#",
				"http://rdfs.org/sioc/ns#", "sioc", KnowledgeBaseTest.class);
			store.importOntology("skiing.rdf",
				"http://www.semanticweb.org/sw/ontologies/skiing#", "ski", KnowledgeBaseTest.class);

			logger.info("Adding EDMProvFactory contents");
			store.getProvWriter().storeReport(factory.createProvReport());
			
			logger.info("Getting prov elements back from KB");
			EDMProvDataContainer result = store.getProvElementReader().getElements(null, null);
			logger.info(result.toString());
		
		} catch (Exception e) {
			logger.error("Exception caught: ", e);
		} finally {
			if (store !=null) {
				store.disconnect();
			}
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		KnowledgeBaseTest kbtest = new KnowledgeBaseTest();
	}

	private void fillFactory() {
		try {
			logger.debug("getting factory...");
			//init
			factory = EDMProvFactory.getInstance();
			factory.addOntology("foaf", "http://xmlns.com/foaf/0.1/");
			factory.addOntology("sioc", "http://rdfs.org/sioc/ns#");
		
			//add skiing ontology
			factory.addOntology("ski", "http://www.semanticweb.org/sw/ontologies/skiing#");
			
			//This is Bob.
			EDMAgent bob = factory.createAgent("facebook_154543445", "Bob");
			bob.addOwlClass(factory.getNamespaceForPrefix("foaf") + "Person");

			EDMEntity skilift1 = factory.createEntity("skilift1-dfkjhdsjf", "Skilift 1");
			skilift1.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");

			EDMEntity skilift2 = factory.createEntity("skilift2-dfkdfgdjf", "Skilift 2");
			skilift2.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");

			EDMActivity usingSkilift1 = bob.startActivity("usingskilift1-dskfhskdfh", "Using skilift 1", "1280512800");
			usingSkilift1.addOwlClass(factory.getNamespaceForPrefix("ski") + "UsingSkiliftActivity");
			usingSkilift1.useEntity(skilift1);
			bob.stopActivity(usingSkilift1, "1280513800");
			
			EDMActivity skiing1 = bob.startActivity("skiing1-hdskjdshfjsd", "Skiing 1", "1280513800");
			skiing1.addOwlClass(factory.getNamespaceForPrefix("ski") + "SkiingActivity");
			
			EDMActivity usingSkilift2 = bob.startActivity("usingskilift2-dsdsfdsffh", "Using skilift 1", "1280515800");
			usingSkilift2.addOwlClass(factory.getNamespaceForPrefix("ski") + "UsingSkiliftActivity");
			usingSkilift2.useEntity(skilift2);
			bob.stopActivity(usingSkilift2, "1280516800");
			
			EDMActivity skiing2 = bob.startActivity("skiing2-hhffghfghfjsd", "Skiing 2", "1280516800");
			skiing2.addOwlClass(factory.getNamespaceForPrefix("ski") + "SkiingActivity");
			
			EDMActivity finish = bob.doDiscreteActivity("finish-dskfhkjsdhf", "Finish skiing", "1280519800");
			finish.addOwlClass(factory.getNamespaceForPrefix("ski") + "FinishActivity");
			/*
			
			//This is a video about Schladming.
			EDMEntity video = factory.getEntity("facebook_1545879879", "reallyCoolFacebookVideo");

			//Bob starts to watch the video and pauses it when he sees something interesting.
			EDMActivity watchVideo = bob.startActivity("activity123", "WatchVideo");
			watchVideo.useEntity(video);
			EDMActivity pauseVideo = bob.doDiscreteActivity("activity234", "PauseVideo");
			pauseVideo.useEntity(video);
			
			//Bob logs in to his FB account and posts something
			EDMActivity writePost = bob.startActivity("activity345", "WritePost");
			writePost.generateEntity("facebook_98763242347", "BobsFacebookPost", "1280512800");
			bob.stopActivity(writePost);
			
			//Bob goes back to watch the rest of the video.
			EDMAgent copyOfBob = factory.getAgent("facebook_154543445", null);
			EDMActivity resumeVideo = copyOfBob.doDiscreteActivity("activity456", "ResumeVideo");
			resumeVideo.useEntity(video);
			bob.stopActivity(watchVideo);
			
			*/
			
			//example data to check whether inferring statements works
			EDMEntity ea = factory.createEntity("InheritanceTest", "Should inherit classes prov:Agent, " +
			"dcterms:Agent, experimedia:ValuePartition, foaf:Document and sioc:Item");
			ea.addOwlClass(factory.getNamespaceForPrefix("prov") + "Person");
			ea.addOwlClass(factory.getNamespaceForPrefix("foaf") + "Person");
			ea.addOwlClass(factory.getNamespaceForPrefix("experimedia") + "Unit");
			ea.addOwlClass(factory.getNamespaceForPrefix("sioc") + "Post");
			ea.addOwlClass("http://some-mysterious-URI.com/ontology/ont#unknownclass");
			ea.addOwlClass("http://some-mysterious-URI.com/ontology/ont#unknownclass");
		
		} catch (Exception e) {
			logger.error("Error filling EDMProvFactory with test data", e);
		}
	}

}
