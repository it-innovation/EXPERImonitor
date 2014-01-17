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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.test.prov;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import uk.ac.soton.itinnovation.edmprov.owlim.common.NoSuchRepositoryException;
import uk.ac.soton.itinnovation.edmprov.owlim.common.OntologyDetails;
import uk.ac.soton.itinnovation.edmprov.owlim.common.RelationshipType;
import uk.ac.soton.itinnovation.edmprov.owlim.common.RepositoryExistsException;
import uk.ac.soton.itinnovation.edmprov.owlim.common.Triple;
import uk.ac.soton.itinnovation.edmprov.sesame.ASesameConnector;
import uk.ac.soton.itinnovation.edmprov.sesame.RemoteSesameConnector;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMActivity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMEntity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvDisplayFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple;

//test comment for git

public class KnowledgeBaseTest {

	private EDMProvFactory factory;
	private EDMProvDisplayFactory factory2;
	private EDMProvReport report;
	private ASesameConnector sCon;
	
	private static Properties props = new Properties();
	
	//ontology config
	private static String ontPrefix = "experimedia";
	private static String ontBaseURI = "http://it-innovation.soton.ac.uk/ontologies/experimedia#";
	
	//triple store config
	private static String sesameServerURL = "http://localhost:8080/openrdf-sesame";
	private static String repositoryID = "experimedia";
	private static String repositoryName = "EXPERIMEDIA provenance store";
	
	private static Logger logger = Logger.getLogger(KnowledgeBaseTest.class);

	private KnowledgeBaseTest() {
		
		logger.info("Starting Prov Test");
		
		logger.info("Loading properties file");
		try {
			props.load(KnowledgeBaseTest.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (Exception e) {
			logger.error("Error loading properties file", e);
		}
		
		try
		{
			logger.info("Initialising EDMProvFactory");
			init();
			
			logger.info("Filling factory with example data");
			fillFactory();
			
			logger.info("Creating provenance report");
			report = factory.createProvReport();

			logger.info("Connecting to Sesame server");
			sCon = new RemoteSesameConnector(props, sesameServerURL);
			
			//logger.info("Clearing factory, reconstructing from triples");
			//clearAndReloadFactory();
			
			logger.info("Deleting repository");
			try {
				sCon.deleteRepository(repositoryID);
			} catch (NoSuchRepositoryException ex) { }
			
			logger.info("Creating owlim repository");
			try {
				sCon.createNewRepository(repositoryID, repositoryName);
			} catch (RepositoryExistsException ex) {
				logger.info("Repository already existed - fine, will continue...");
			}
			
			logger.info("Adding ontologies");
			importOntologyToKnowledgeBase("experimedia.rdf",
				"http://it-innovation.soton.ac.uk/ontologies/experimedia#", "experimedia");
			importOntologyToKnowledgeBase("http://www.w3.org/ns/prov-o#/",
				"http://www.w3.org/ns/prov#", "prov");
			importOntologyToKnowledgeBase("http://xmlns.com/foaf/0.1/",
				"http://xmlns.com/foaf/0.1/", "foaf");
			importOntologyToKnowledgeBase("http://rdfs.org/sioc/ns#",
				"http://rdfs.org/sioc/ns#", "sioc");
			
			importOntologyToKnowledgeBase("skiing.rdf", "http://www.semanticweb.org/sw/ontologies/skiing#", "ski");

			logger.info("Adding EDMProvFactory contents");
			storeToKnowledgeBase();
			
			logger.info("Getting prov elements back from KB");
			String sparql = "SELECT DISTINCT * " +
				"WHERE {" +
				"?s ?p ?o ." +
				"?s a ?c ." +
				"?p a ?t ." +
				"FILTER(?c in(prov:Agent, prov:Activity, prov:Entity))." +
				"FILTER regex(str(?s),\"experimedia#.\")." +
				"FILTER(?t in(owl:ObjectProperty, owl:DatatypeProperty, owl:AnnotationProperty) " +
				"|| (?p=rdf:type && ?t=rdf:Property))" +
				"} ORDER BY ?c ?s ?t ?p";
			logger.debug(sparql);
			LinkedList<HashMap<String,String>> result = queryKnowledgeBase(sparql);
			
			logger.info("Recreating EDMProvFactory from results");
			recreateProv(result);
		
		} catch (Throwable t) {
			logger.error("Exception caught: " + t, t);
		} finally {
			if ((sCon != null) && sCon.isConnected()) {
				logger.warn("SesameConnector still got an open connection - disconnecting now");
				sCon.disconnect();
			}
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		KnowledgeBaseTest kbtest = new KnowledgeBaseTest();
	}

	private void init() {
		factory = EDMProvFactory.getInstance();
		factory.addOntology("foaf", "http://xmlns.com/foaf/0.1/");
		factory.addOntology("sioc", "http://rdfs.org/sioc/ns#");
	}
	
	private void fillFactory() {
		try {
			
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

	@SuppressWarnings("unused")
	private void clearAndReloadFactory() {

		//clear factory
		//HashMap<String, String> namespaces = factory.container.getNamespaces();
		factory.clear();
		factory = EDMProvFactory.getInstance(ontPrefix, ontBaseURI);
		//factory.container.addNamespaces(namespaces);
		factory.container.addNamespaces(sCon.getNamespacesForRepository(repositoryID));

		//load prov report contents into factory
		try {
			factory.loadReport(report);
		} catch (Exception e) {
			logger.error("Error loading report into factory", e);
		}
	}
	
	private void importOntologyToKnowledgeBase(String ontologypath, String baseURI, String prefix) {
		logger.info(" - " + prefix + " (" + baseURI + ")");
		OntologyDetails od = new OntologyDetails();
		File ontfile = new File(ontologypath);
		if (!ontfile.exists()) {
			//try URL
			if (ontologypath.startsWith("http://")) {
				try {
					URL remoteOntology = new URL(ontologypath);
					od.setURL(remoteOntology);
					        
				} catch (Exception e) {
					logger.error("Error loading ontology from URL " + ontologypath, e);
				}
			//try file from classpath
			} else {
				String resourcepath = KnowledgeBaseTest.class.getClassLoader().getResource(ontologypath).getPath();
				try {
					ontfile = new File(resourcepath);
					od.setURL(ontfile.toURI().toURL());
				} catch (MalformedURLException e) {
					logger.error("Error reading resource file", e);
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
			sCon.addOntology(repositoryID, od);
		} catch (Exception e) {
			logger.error("Error importing ontology", e);
		}
	}
	
	private void storeToKnowledgeBase() {
		
		ArrayList<Triple> triples = new ArrayList<Triple>();
		if (report==null || report.getTriples()==null || report.getTriples().values()==null) {
			logger.error("Error adding triples");
		}
			
    	for (EDMTriple t: report.getTriples().values()) {
    		
    		triples.add(new Triple(t.getSubject(), t.getPredicate(), t.getObject(),
    			RelationshipType.fromValue(t.getType().name())));
    	}
    	try {
			sCon.addTriples(repositoryID, triples);
		} catch (Exception e) {
			logger.error("Error adding triples", e);
		}
	}
	
	private LinkedList<HashMap<String,String>> queryKnowledgeBase(String queryString) {
		
		//get prefixes
		String prefixes = "";
		if (sCon.getNamespacesForRepository(repositoryID)!=null) {
			for (Entry<String, String> e: sCon.getNamespacesForRepository(repositoryID).entrySet()) {
				prefixes += "PREFIX " + e.getKey() + ":<" + e.getValue() + ">\n";
			}
		}
		queryString = prefixes + queryString;
				
        TupleQueryResult result = null;
        LinkedList<HashMap<String, String>> results = new LinkedList<HashMap<String,String>>();
		
		try {
			long queryBegin = System.nanoTime();
			result = sCon.query(repositoryID, queryString);
			if (result==null) {
				logger.error("SPARQL query result was null");
				return null;
			}

			int counter = 0;
			while (result.hasNext())
			{
				counter++;
				BindingSet bindingSet = result.next();

				//get all variables in the result set
				Iterator<Binding> i = bindingSet.iterator();
				HashMap<String, String> row = new HashMap<String, String>();
				while (i.hasNext()) {
					Binding b = i.next();
					row.put(b.getName(), b.getValue().stringValue());
				}
				results.add(row);
			}
			long queryEnd = System.nanoTime();
			logger.info(" - Got " + counter + " result(s) in " + (queryEnd - queryBegin) / 1000000 + "ms.");
			
		} catch (Exception ex) {
			logger.error("Exception caught when querying repository: " + ex, ex);
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (QueryEvaluationException e) {
					logger.error("Error closing connection to KB", e);
				}
			}
			
		}
		return results;
	}
	
	private void recreateProv(LinkedList<HashMap<String, String>> result) {	
		factory2 = new EDMProvDisplayFactory(ontPrefix, ontBaseURI);
		factory2.loadSPARQLResult(result);
		logger.info(factory2.toString());
	}

}
