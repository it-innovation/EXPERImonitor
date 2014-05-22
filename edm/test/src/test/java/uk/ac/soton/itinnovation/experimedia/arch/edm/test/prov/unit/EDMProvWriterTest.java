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
//      Created Date :          2014-03-07
//      Created for Project :   Experimedia
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.prov.unit;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.UUID;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.owlimstore.common.RepositoryExistsException;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMActivity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMEntity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.dao.EDMProvWriterImpl;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvWriter;

/**
 *
 * @author Vegard Engen
 */
@RunWith(JUnit4.class)
public class EDMProvWriterTest extends TestCase
{
    private IEDMProvWriter writer;
    private static Logger logger;
	private static Properties props;
	private EDMProvFactory factory = null;
	private EDMProvReport report; 
	private static String repoID = "testrepo-";
	private static String repoName = "This is a test repository";
    
    @BeforeClass
    public static void beforeClass() {
        // Configure logging system
		logger = LoggerFactory.getLogger(EDMProvWriterTest.class);
		repoID += UUID.randomUUID().toString();
		repoName += ", created at " + (new SimpleDateFormat("yyyy/MM/dd, HH:mm:ss").format(Calendar.getInstance().getTime()));
        
        logger.info("EDMProvWriter tests executing...");
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
		
		if ((writer == null)) {
			try {
				writer = new EDMProvWriterImpl(props);
			} catch (Exception e) {
				logger.error("Error connecting to store", e);
			}
        }

		try {
			((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().createNewRepository(repoID, repoName);
		} catch (RepositoryExistsException e) {
			logger.debug("Repository already exists - clearing contents");
			try {
				((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().clearRepository(repoID);
			} catch (Exception ex) {
				logger.error("Error clearing repository", ex);
			}
		} catch (Exception e) {
			logger.error("Error creating repository", e);
		}

		createTestProvReport();
    }
	
	@After
	public void after() {
		
		logger.debug("Shutting down store after test");

		if (writer!=null && ((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().isConnected()) {

			try {
				if (((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().repositoryExists(repoID)) {
					((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().deleteRepository(repoID);	
				}
			} catch (Exception ex) {
				logger.error("Error deleting repository", ex);
			}

			if (writer!=null) {
				writer.disconnect();
				writer = null;
			}
		}
        
        if (factory != null) {
            factory.clear();
			factory = null;
		}
	}

	
	@Test
	public void testStoreReport() {
		try {
			//need to add namespaces before prefixes can be used
			((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().importOntologyToKnowledgeBase("http://xmlns.com/foaf/0.1/",
				"http://xmlns.com/foaf/0.1/", "foaf", EDMProvWriterTest.class);
			((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().importOntologyToKnowledgeBase("http://rdfs.org/sioc/ns#",
				"http://rdfs.org/sioc/ns#", "sioc", EDMProvWriterTest.class);
			((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().importOntologyToKnowledgeBase("skiing.rdf",
				"http://it-innovation.soton.ac.uk/ontologies/skiing#", "ski", EDMProvWriterTest.class);
			((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().importOntologyToKnowledgeBase("http://www.w3.org/ns/prov-o#/",
				"http://www.w3.org/ns/prov#", "prov", EDMProvWriterTest.class);
			((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().importOntologyToKnowledgeBase("experimedia.rdf",
				"http://it-innovation.soton.ac.uk/ontologies/experimedia#", "experimedia", EDMProvWriterTest.class);
		} catch (Exception e) {
			logger.error("Failed to import required ontologies", e);
			fail("Failed to import required ontologies");
		}
		try {
			writer.storeReport(report);
		} catch (Exception e) {
			logger.error("Error storing report", e);
			fail("Error storing report");
		}
		//check integrity of stored data
		//--namespaces
		HashMap<String, String> namespaces = ((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().getNamespacesForRepository(repoID);
		int hits = 0;
		ArrayList<String> targets = new ArrayList<String>();
		targets.add("foaf");
		targets.add("sioc");
		targets.add("ski");
		targets.add("prov");
		targets.add("experimedia");
		for (String ns: namespaces.keySet()) {
			if (targets.contains(ns)) {
				hits++;
			}
		}
		if (hits<5) {
			fail("Namespaces are incomplete");
		}
		//--prov agent : quick check: just check for asserted types and relationships
		/*
		!	{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/ns/prov#Agent}
		!	{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://xmlns.com/foaf/0.1/Person}
			{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=_:node18iqtnpflx42}
			{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://schema.org/Person}
			{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/2000/10/swap/pim/contact#Person}
			{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://purl.org/dc/terms/Agent}
			{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://xmlns.com/foaf/0.1/Agent}
			{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing}
			{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/ns/prov#Entity}
			{p=http://www.w3.org/2000/01/rdf-schema#label, o="Bob"^^<http://www.w3.org/2001/XMLSchema#string>}
		!	{p=http://www.w3.org/ns/prov#influenced, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd}
		!	{p=http://www.w3.org/ns/prov#influenced, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh}
		!	{p=http://www.w3.org/ns/prov#influenced, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh}
		!	{p=http://www.w3.org/ns/prov#influenced, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing2-hhffghfghfjsd}
		!	{p=http://www.w3.org/ns/prov#influenced, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#finish-dskfhkjsdhf}
		 */
		String query = "SELECT * WHERE { experimedia:facebook_154543445 ?p ?o . }";
		LinkedList<HashMap<String, String>> result = ((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().query(query);
		hits = 0;
		for (HashMap<String, String> row: result) {
			if (row.get("p").equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") &&
				row.get("o").equals("http://www.w3.org/ns/prov#Agent")) {
				hits++;
			}
			if (row.get("p").equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") &&
				row.get("o").equals("http://xmlns.com/foaf/0.1/Person")) {
				hits++;
			}
			if (row.get("p").equals("http://www.w3.org/ns/prov#influenced") &&
				row.get("o").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd")) {
				hits++;
			}
			if (row.get("p").equals("http://www.w3.org/ns/prov#influenced") &&
				row.get("o").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh")) {
				hits++;
			}
			if (row.get("p").equals("http://www.w3.org/ns/prov#influenced") &&
				row.get("o").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh")) {
				hits++;
			}
			if (row.get("p").equals("http://www.w3.org/ns/prov#influenced") &&
				row.get("o").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing2-hhffghfghfjsd")) {
				hits++;
			}
			if (row.get("p").equals("http://www.w3.org/ns/prov#influenced") &&
				row.get("o").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#finish-dskfhkjsdhf")) {
				hits++;
			}
		}
		if (hits<7) {
			fail("Prov agent is incomplete");
		}
		
		//--prov entities : quick check: just check for asserted types and relationships
		/*
		!	{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.semanticweb.org/sw/ontologies/skiing#Skilift}
		!	{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/ns/prov#Entity}
			{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=_:node18iqvannox42}
			{p=http://www.w3.org/2000/01/rdf-schema#label, o="Skilift 1"^^<http://www.w3.org/2001/XMLSchema#string>}
		!	{p=http://www.w3.org/ns/prov#influenced, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh}
		!	{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.semanticweb.org/sw/ontologies/skiing#Skilift}
		!	{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/ns/prov#Entity}
			{p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=_:node18iqvannox42}
			{p=http://www.w3.org/2000/01/rdf-schema#label, o="Skilift 2"^^<http://www.w3.org/2001/XMLSchema#string>}
		!	{p=http://www.w3.org/ns/prov#influenced, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh}
		*/
		query = "SELECT * WHERE { { experimedia:skilift1-dfkjhdsjf ?p ?o . } UNION " +
				"{ experimedia:skilift2-dfkdfgdjf ?p ?o . } }";
		result = ((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().query(query);
		hits = 0;
		for (HashMap<String, String> row: result) {
			if (row.get("p").equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") &&
				row.get("o").equals("http://www.semanticweb.org/sw/ontologies/skiing#Skilift")) {
				hits++;
			}
			if (row.get("p").equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") &&
				row.get("o").equals("http://www.w3.org/ns/prov#Entity")) {
				hits++;
			}
			if (row.get("p").equals("http://www.w3.org/ns/prov#influenced") &&
				row.get("o").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh")) {
				hits++;
			}
			if (row.get("p").equals("http://www.w3.org/ns/prov#influenced") &&
				row.get("o").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh")) {
				hits++;
			}
		}
		if (hits<6) {
			fail("Prov entities are incomplete");
		}
		
		//--prov activities : quick check: just check for existence
		/*
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#finish-dskfhkjsdhf, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=_:node18ir0vac9x38}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#finish-dskfhkjsdhf, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.semanticweb.org/sw/ontologies/skiing#FinishActivity}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#finish-dskfhkjsdhf, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/ns/prov#Activity}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#finish-dskfhkjsdhf, p=http://www.w3.org/2000/01/rdf-schema#label, o="Finish skiing"^^<http://www.w3.org/2001/XMLSchema#string>}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#finish-dskfhkjsdhf, p=http://www.w3.org/ns/prov#endedAtTime, o="2010-07-30T19:56:40Z"^^<http://www.w3.org/2001/XMLSchema#dateTime>}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#finish-dskfhkjsdhf, p=http://www.w3.org/ns/prov#startedAtTime, o="2010-07-30T19:56:40Z"^^<http://www.w3.org/2001/XMLSchema#dateTime>}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#finish-dskfhkjsdhf, p=http://www.w3.org/ns/prov#wasEndedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#finish-dskfhkjsdhf, p=http://www.w3.org/ns/prov#wasInfluencedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#finish-dskfhkjsdhf, p=http://www.w3.org/ns/prov#wasStartedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=_:node18ir0vac9x38}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.semanticweb.org/sw/ontologies/skiing#SkiingActivity}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/ns/prov#Activity}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd, p=http://www.w3.org/2000/01/rdf-schema#label, o="Skiing 1"^^<http://www.w3.org/2001/XMLSchema#string>}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd, p=http://www.w3.org/ns/prov#startedAtTime, o="2010-07-30T18:16:40Z"^^<http://www.w3.org/2001/XMLSchema#dateTime>}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd, p=http://www.w3.org/ns/prov#wasInfluencedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd, p=http://www.w3.org/ns/prov#wasStartedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing2-hhffghfghfjsd, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=_:node18ir0vac9x38}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing2-hhffghfghfjsd, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.semanticweb.org/sw/ontologies/skiing#SkiingActivity}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing2-hhffghfghfjsd, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/ns/prov#Activity}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing2-hhffghfghfjsd, p=http://www.w3.org/2000/01/rdf-schema#label, o="Skiing 2"^^<http://www.w3.org/2001/XMLSchema#string>}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing2-hhffghfghfjsd, p=http://www.w3.org/ns/prov#startedAtTime, o="2010-07-30T19:06:40Z"^^<http://www.w3.org/2001/XMLSchema#dateTime>}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing2-hhffghfghfjsd, p=http://www.w3.org/ns/prov#wasInfluencedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing2-hhffghfghfjsd, p=http://www.w3.org/ns/prov#wasStartedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=_:node18ir0vac9x38}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.semanticweb.org/sw/ontologies/skiing#UsingSkiliftActivity}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/ns/prov#Activity}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh, p=http://www.w3.org/2000/01/rdf-schema#label, o="Using skilift 1"^^<http://www.w3.org/2001/XMLSchema#string>}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh, p=http://www.w3.org/ns/prov#endedAtTime, o="2010-07-30T18:16:40Z"^^<http://www.w3.org/2001/XMLSchema#dateTime>}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh, p=http://www.w3.org/ns/prov#startedAtTime, o="2010-07-30T18:00:00Z"^^<http://www.w3.org/2001/XMLSchema#dateTime>}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh, p=http://www.w3.org/ns/prov#used, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#skilift1-dfkjhdsjf}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh, p=http://www.w3.org/ns/prov#wasEndedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh, p=http://www.w3.org/ns/prov#wasInfluencedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh, p=http://www.w3.org/ns/prov#wasInfluencedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#skilift1-dfkjhdsjf}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh, p=http://www.w3.org/ns/prov#wasStartedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=_:node18ir0vac9x38}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.semanticweb.org/sw/ontologies/skiing#UsingSkiliftActivity}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh, p=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, o=http://www.w3.org/ns/prov#Activity}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh, p=http://www.w3.org/2000/01/rdf-schema#label, o="Using skilift 1"^^<http://www.w3.org/2001/XMLSchema#string>}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh, p=http://www.w3.org/ns/prov#endedAtTime, o="2010-07-30T19:06:40Z"^^<http://www.w3.org/2001/XMLSchema#dateTime>}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh, p=http://www.w3.org/ns/prov#startedAtTime, o="2010-07-30T18:50:00Z"^^<http://www.w3.org/2001/XMLSchema#dateTime>}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh, p=http://www.w3.org/ns/prov#used, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#skilift2-dfkdfgdjf}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh, p=http://www.w3.org/ns/prov#wasEndedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh, p=http://www.w3.org/ns/prov#wasInfluencedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
			{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh, p=http://www.w3.org/ns/prov#wasInfluencedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#skilift2-dfkdfgdjf}
		!	{s=http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh, p=http://www.w3.org/ns/prov#wasStartedBy, o=http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445}
		*/
		query = "SELECT * WHERE { ?s ?p ?o . ?s rdf:type prov:Activity . } ORDER BY ?s ?p ?o";
		result = ((EDMProvWriterImpl) writer).getEDMProvStoreWrapper().query(query);
		hits = 0;
		for (HashMap<String, String> row: result) {
			if (row.get("s").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#finish-dskfhkjsdhf")) {
				hits++;
			} else if (row.get("s").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd")) {
				hits++;
			} else if (row.get("s").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing2-hhffghfghfjsd")) {
				hits++;
			} else if (row.get("s").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh")) {
				hits++;
			} else if (row.get("s").equals("http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift2-dsdsfdsffh")) {
				hits++;
			}
		}
		if (hits<5) {
			fail("Prov activities are incomplete");
		}
	}

	
	//helper classes
	public Properties getProperties() {
        Properties properties = new Properties();
        
        try {
            properties.load(EDMProvWriterTest.class.getClassLoader().getResourceAsStream(EDMProvTestSuite.getPropertiesFile()));
        } catch (IOException ex) {
            logger.error("Error with loading properties file", ex);
            return null;
        }
        
        return properties;
    }

	/**
	 * Creates a prov factory and report with the following test data:
	 * 
	 * Prov factory and report statistics
	 * ###################################
	 * prefixes:		foaf, sioc, ski, prov, experimedia
	 * agents:		bob
	 * entities:		skilift1, skilift2, InheritanceTest
	 * activities:	usingSkilift1, skiing1, usingSkilift2, skiing2, finish
	 */
	private void createTestProvReport() {
		if (factory==null) {
			try {
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

				//get number
				report = factory.createProvReport();

			} catch (Exception e) {
				logger.error("Error filling EDMProvFactory with test data", e);
			}
		}
	}
}
