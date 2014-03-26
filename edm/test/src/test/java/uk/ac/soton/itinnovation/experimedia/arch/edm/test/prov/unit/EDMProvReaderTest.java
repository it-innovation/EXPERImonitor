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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvBaseElement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvDataContainer;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.dao.EDMProvReaderImpl;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvReader;

/**
 *
 * @author Vegard Engen
 */
@RunWith(JUnit4.class)
public class EDMProvReaderTest extends TestCase
{
    private IEDMProvReader reader;
    private static Logger logger;
	private static Properties props;
	private static final String repoID = "experimedia-junit";
	private static final String repoName = "Experimedia-JUnit Testrepository";
    
    @BeforeClass
    public static void beforeClass() {
        // Configure logging system
        logger = Logger.getLogger(EDMProvReaderTest.class);
        logger.info("EDMProvElementReader tests executing...");
    }
    
    @Before
    public void beforeEachTest() {
        try {
            props = getProperties();
			
			//overwrite repo id and name for test
			props.setProperty("owlim.repositoryID", repoID);
			props.setProperty("owlim.repositoryName", repoName);
			
        } catch (Exception ex) {
            logger.error("Failed to get properites.", ex);
        }
		
		if ((reader == null)) {
			try {
				reader = new EDMProvReaderImpl(props);
			} catch (Exception e) {
				logger.error("Error connecting to test store, please make sure it is set up correctly " +
					"using the testrepo.rdf file. The repositoryID should be \"experimedia-junit\".", e);
			}
        }
		
		((EDMProvReaderImpl) reader).getEDMProvStoreWrapper().getPrefixes();
    }
	
	@After
	public void afterEachTest() {
		try {
			reader.disconnect();
		} catch (Exception e) {
			logger.error("Error disconnecting from store", e);
		}
	}
    
    public Properties getProperties() {
        Properties props = new Properties();
        
        try {
            props.load(EDMProvReaderTest.class.getClassLoader().getResourceAsStream(EDMProvTestSuite.getPropertiesFile()));
        } catch (IOException ex) {
            logger.error("Error with loading properties file", ex);
            return null;
        }
        
        return props;
    }
	
	@Test
	public void testGetElementCore() {
		if ((reader == null)) {
            fail("EDM Prov store not set up, cannot perform test");
        }
		EDMProvBaseElement result = null;
		try {
			result = reader.getElementCore("http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd");
		} catch (Exception e) {
			logger.error("Error getting element from store", e);
			fail("Error getting element from store");
		}
		//check data integrity
		if (result==null) {
			fail("Query returned empty resultset");
		}
		EDMTriple essentialTriple1 = new EDMTriple("http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd",
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/ns/prov#Activity",EDMTriple.TRIPLE_TYPE.CLASS_ASSERTION);
		EDMTriple essentialTriple2 = new EDMTriple("http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd",
				"http://www.w3.org/2000/01/rdf-schema#label","\"Skiing 1\"^^xsd:string",EDMTriple.TRIPLE_TYPE.ANNOTATION_PROPERTY);
		if (!result.contains(essentialTriple1) || !result.contains(essentialTriple2)) {
			fail("Triples missing from test result" + essentialTriple2.getObject().toString());
		}
	}
	
	@Test
	public void testGetElement() {
		if ((reader == null)) {
            fail("EDM Prov store not set up, cannot perform test");
        }
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		EDMProvBaseElement result = null;
		//checksums
		int op, dp, ca, ap;
		
		//check 1: using dates enclosing the object
		logger.info(" - testGetElement check 1: using dates enclosing the object");
		try {
			Date d1 = sdf.parse("2010-07-30 17:00:00");
			Date d2 = sdf.parse("2010-07-30 19:00:00");

			result = reader.getElement("http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh",d1,d2);
		} catch (Exception e) {
			logger.error("Error getting element from store in check 1", e);
			fail("Error getting element from store in check 1");
		}
		//check 1 expected results
		if (result==null || result.getTriples()==null) {
			fail("Query returned empty resultset in check 1");
		}
		//simple check
		//[ePROV_ACTIVITY] Using skilift 1 (http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh)
		//	[CLASS_ASSERTION] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/1999/02/22-rdf-syntax-ns#type _:node119
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#used http://it-innovation.soton.ac.uk/ontologies/experimedia#skilift1-dfkjhdsjf
		//	[DATA_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#startedAtTime "2010-07-30T18:00:00Z"^^xsd:dateTime
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#wasEndedBy http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445
		//	[DATA_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#endedAtTime "2010-07-30T18:16:40Z"^^xsd:dateTime
		//	[CLASS_ASSERTION] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.semanticweb.org/sw/ontologies/skiing#UsingSkiliftActivity
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#wasInfluencedBy http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#wasStartedBy http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445
		//	[CLASS_ASSERTION] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.w3.org/ns/prov#Activity
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#wasInfluencedBy http://it-innovation.soton.ac.uk/ontologies/experimedia#skilift1-dfkjhdsjf
		//	[ANNOTATION_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/2000/01/rdf-schema#label "Using skilift 1"^^xsd:string

		//checksums:
		op = dp = ca = ap = 0;
		for (EDMTriple t: result.getTriples().values()) {
			switch (t.getType()) {
				case CLASS_ASSERTION:
					ca++;
					break;
				case ANNOTATION_PROPERTY:
					ap++;
					break;
				case DATA_PROPERTY:
					dp++;
					break;
				case OBJECT_PROPERTY:
					op++;
					break;
				default:
					break;
			}
		}
		if (!(ca==3 && ap==1 && dp==2 && op==5)) {
			fail("Query returned unexpected result in check 1: " + result.toString());
		}
		
		//check 2: enclose start time
		logger.info(" - testGetElement check 2: enclose start time");
		try {
			Date d1 = sdf.parse("2010-07-30 17:00:00");
			Date d2 = sdf.parse("2010-07-30 18:15:00");

			result = reader.getElement("http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh",d1,d2);
		} catch (Exception e) {
			logger.error("Error getting element from store in check 2", e);
			fail("Error getting element from store in check 2");
		}
		//check 2 expected results: same as check 1
		if (result==null || result.getTriples()==null) {
			fail("Query returned empty resultset in check 2");
		}
		//simple check
		//[ePROV_ACTIVITY] Using skilift 1 (http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh)
		//	[CLASS_ASSERTION] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/1999/02/22-rdf-syntax-ns#type _:node119
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#used http://it-innovation.soton.ac.uk/ontologies/experimedia#skilift1-dfkjhdsjf
		//	[DATA_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#startedAtTime "2010-07-30T18:00:00Z"^^xsd:dateTime
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#wasEndedBy http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445
		//	[DATA_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#endedAtTime "2010-07-30T18:16:40Z"^^xsd:dateTime
		//	[CLASS_ASSERTION] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.semanticweb.org/sw/ontologies/skiing#UsingSkiliftActivity
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#wasInfluencedBy http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#wasStartedBy http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445
		//	[CLASS_ASSERTION] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.w3.org/ns/prov#Activity
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#wasInfluencedBy http://it-innovation.soton.ac.uk/ontologies/experimedia#skilift1-dfkjhdsjf
		//	[ANNOTATION_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/2000/01/rdf-schema#label "Using skilift 1"^^xsd:string

		//checksums:
		op = dp = ca = ap = 0;
		for (EDMTriple t: result.getTriples().values()) {
			switch (t.getType()) {
				case CLASS_ASSERTION:
					ca++;
					break;
				case ANNOTATION_PROPERTY:
					ap++;
					break;
				case DATA_PROPERTY:
					dp++;
					break;
				case OBJECT_PROPERTY:
					op++;
					break;
				default:
					break;
			}
		}
		if (!(ca==3 && ap==1 && dp==2 && op==5)) {
			fail("Query returned unexpected result in check 1: " + result.toString());
		}
		
		//check 3: enclose end time
		logger.info(" - testGetElement check 3: enclose end time");
		try {
			Date d1 = sdf.parse("2010-07-30 18:15:00");
			Date d2 = sdf.parse("2010-07-30 18:30:00");

			result = reader.getElement("http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh",d1,d2);
		} catch (Exception e) {
			logger.error("Error getting element from store in check 3", e);
			fail("Error getting element from store in check 3");
		}
		//check 3 expected results: same as check 1 and 2
		if (result==null || result.getTriples()==null) {
			fail("Query returned empty resultset in check 3");
		}
		//simple check
		//[ePROV_ACTIVITY] Using skilift 1 (http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh)
		//	[CLASS_ASSERTION] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/1999/02/22-rdf-syntax-ns#type _:node119
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#used http://it-innovation.soton.ac.uk/ontologies/experimedia#skilift1-dfkjhdsjf
		//	[DATA_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#startedAtTime "2010-07-30T18:00:00Z"^^xsd:dateTime
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#wasEndedBy http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445
		//	[DATA_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#endedAtTime "2010-07-30T18:16:40Z"^^xsd:dateTime
		//	[CLASS_ASSERTION] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.semanticweb.org/sw/ontologies/skiing#UsingSkiliftActivity
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#wasInfluencedBy http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#wasStartedBy http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445
		//	[CLASS_ASSERTION] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.w3.org/ns/prov#Activity
		//	[OBJECT_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/ns/prov#wasInfluencedBy http://it-innovation.soton.ac.uk/ontologies/experimedia#skilift1-dfkjhdsjf
		//	[ANNOTATION_PROPERTY] http://it-innovation.soton.ac.uk/ontologies/experimedia#usingskilift1-dskfhskdfh http://www.w3.org/2000/01/rdf-schema#label "Using skilift 1"^^xsd:string

		//checksums:
		op = dp = ca = ap = 0;
		for (EDMTriple t: result.getTriples().values()) {
			switch (t.getType()) {
				case CLASS_ASSERTION:
					ca++;
					break;
				case ANNOTATION_PROPERTY:
					ap++;
					break;
				case DATA_PROPERTY:
					dp++;
					break;
				case OBJECT_PROPERTY:
					op++;
					break;
				default:
					break;
			}
		}
		if (!(ca==3 && ap==1 && dp==2 && op==5)) {
			fail("Query returned unexpected result in check 3: " + result.toString());
		}
		
		//check 4: choose timeframe that doesn't cover the activity
		logger.info(" - testGetElement check 4: choose timeframe that doesn't cover the activity");
		try {
			Date d1 = sdf.parse("2010-07-30 20:10:00");
			Date d2 = sdf.parse("2010-07-30 22:20:00");

			result = reader.getElement("http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd",d1,d2);
		} catch (Exception e) {
			logger.error("Error getting element from store in check 4", e);
			fail("Error getting element from store in check 4");
		}
		//check 4 expected results: should return empty resultset
		if (result==null && result.getTriples()!=null && !result.getTriples().isEmpty()) {
			fail("Query returned unextected resultset in check 4; should have been empty: " + result.toString());
		}
	}
	
	@Test
	public void testGetElements() {
		if ((reader == null)) {
            fail("EDM Prov store not set up, cannot perform test");
        }
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		EDMProvDataContainer result = null;
		
		//check 1: without anything
		logger.info(" - testGetElements check 1: get all elements regardless of type and time");
		try {
			result = reader.getElements(null, null);
		} catch (Exception e) {
			logger.error("Error getting elements from store", e);
			fail("Error getting elements from store");
		}
		//check data integrity
		if (result==null || result.getAllElements()==null || result.getAllElements().isEmpty()) {
			fail("Query returned empty resultset in check 1");
		} else {
			if (result.getAllElements().size()!=17) {
				fail("Query returned unextected resultset in check 1: " + result.toString());
			}
		}
		
		//check 2: with date
		logger.info(" - testGetElements check 2: get elements of any type within a timeframe");
		try {
			Date d1 = sdf.parse("2010-07-30 18:00:00");
			Date d2 = sdf.parse("2010-07-30 19:00:00");
			
			result = reader.getElements(d1, d2);
		} catch (ParseException e) {
			logger.error("Error getting elements from store", e);
			fail("Error getting elements from store");
		}
		//check data integrity
		if (result==null || result.getAllElements()==null || result.getAllElements().isEmpty()) {
			fail("Query returned empty resultset in check 2");
		} else {
			int ag, ac, en, un;
			ag = ac = en = un = 0;
			
			for (EDMProvBaseElement e: result.getAllElements().values()) {
				switch (e.getProvType()) {
					case ePROV_ACTIVITY:
						ac++;
						break;
					case ePROV_AGENT:
						ag++;
						break;
					case ePROV_ENTITY:
						en++;
						break;
					default:
						un++;
						break;
				}
			}
			
			if (result.getAllElements().size()!= 3 || ac!=1 || ag!=1 || en!=1 || un!=0) {
				fail("Query returned unexpected resultset in check 2: " + result.toString());
			}
		}
		
		//check 3: with type
		logger.info(" - testGetElements check 3: get elements of a given type");
		try {
			EDMProvBaseElement.PROV_TYPE type = EDMProvBaseElement.PROV_TYPE.ePROV_ENTITY;
			
			result = reader.getElements(type, null, null);
		} catch (Throwable e) {
			logger.error("Error getting elements from store", e);
			fail("Error getting elements from store");
		}
		//check data integrity
		if (result==null || result.getAllElements()==null || result.getAllElements().isEmpty()) {
			fail("Query returned empty resultset in check 3");
		} else {
			int ag, ac, en, un;
			ag = ac = en = un = 0;
			
			for (EDMProvBaseElement e: result.getAllElements().values()) {
				switch (e.getProvType()) {
					case ePROV_ACTIVITY:
						ac++;
						break;
					case ePROV_AGENT:
						ag++;
						break;
					case ePROV_ENTITY:
						en++;
						break;
					default:
						un++;
						break;
				}
			}
			
			if (result.getAllElements().size()!= 7 || ac!=0 || ag!=0 || en!=7 || un!=0) {
				fail("Query returned unexpected resultset in check 3: " + result.toString());
			}
		}

		//check 4: with date and type
		logger.info(" - testGetElements check 4: get elements of a given type within a timeframe");
		try {
			Date d1 = sdf.parse("2010-07-30 18:00:00");
			Date d2 = sdf.parse("2010-07-30 19:00:00");
			EDMProvBaseElement.PROV_TYPE type = EDMProvBaseElement.PROV_TYPE.ePROV_AGENT;
			
			result = reader.getElements(type, d1, d2);
		} catch (ParseException e) {
			logger.error("Error getting elements from store", e);
			fail("Error getting elements from store");
		}
		//check data integrity
		if (result==null || result.getAllElements()==null || result.getAllElements().isEmpty()) {
			fail("Query returned empty resultset in check 4");
		} else {
			int ag, ac, en, un;
			ag = ac = en = un = 0;
			
			for (EDMProvBaseElement e: result.getAllElements().values()) {
				switch (e.getProvType()) {
					case ePROV_ACTIVITY:
						ac++;
						break;
					case ePROV_AGENT:
						ag++;
						break;
					case ePROV_ENTITY:
						en++;
						break;
					default:
						un++;
						break;
				}
			}
			
			if (result.getAllElements().size()!= 1 || ac!=0 || ag!=1 || en!=0 || un!=0) {
				fail("Query returned unexpected resultset in check 4: " + result.toString());
			}
		}
	}
	
	@Test
	public void testGetTriples() {
		if ((reader == null)) {
            fail("EDM Prov store not set up, cannot perform test");
        }
		Set<EDMTriple> result = null;
		
		//check 1: 1 argument
		logger.info(" - testGetTriples check 1: 1 given argument");
		try {
			result = reader.getTriples("http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd", null, null);
		} catch (Exception e) {
			logger.error("Error getting elements from store", e);
			fail("Error getting elements from store");
		}
		//expected results: skiing1-hdskjdshfjsd is the subject in 7 triples
		if (result.size()!=7) {
			fail("Query returned unexpected result in check 1: " + result.toString());
		}
		
		//check 2: 2 arguments
		logger.info(" - testGetTriples check 2: 2 given arguments");
		try {
			result = reader.getTriples("http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd",
					null, "http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445");
		} catch (Exception e) {
			logger.error("Error getting elements from store", e);
			fail("Error getting elements from store");
		}
		//expected results: there are two triples linking skiing1-hdskjdshfjsd and facebook_154543445
		if (result.size()!=2) {
			fail("Query returned unexpected result in check 2: " + result.toString());
		}
		
		//check 3: 3 arguments - triple exists
		logger.info(" - testGetTriples check 3: 3 given arguments (triple exists)");
		try {
			result = reader.getTriples("http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd",
					"http://www.w3.org/ns/prov#wasInfluencedBy", "http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445");
		} catch (Exception e) {
			logger.error("Error getting elements from store", e);
			fail("Error getting elements from store");
		}
		//expected results: the specified triple exists in the store
		if (result.size()!=1) {
			fail("Query returned unexpected result in check 3: " + result.toString());
		}
		
		//check 4: 3 arguments - triple doesn't exist
		logger.info(" - testGetTriples check 4: 3 given arguments (triple doesn't exist)");
		try {
			result = reader.getTriples("http://it-innovation.soton.ac.uk/ontologies/experimedia#skiing1-hdskjdshfjsd",
					"http://www.w3.org/ns/prov#wasEndedBy", "http://it-innovation.soton.ac.uk/ontologies/experimedia#facebook_154543445");
		} catch (Exception e) {
			logger.error("Error getting elements from store", e);
			fail("Error getting elements from store");
		}
		//expected results: the specified triple exists in the store
		if (!result.isEmpty()) {
			fail("Query returned unexpected result in check 4, should have been empty: " + result.toString());
		}
		
		//check 5: 0 arguments (should return null)
		logger.info(" - testGetTriples check 5: 0 given arguments (should return null)");
		try {
			result = reader.getTriples(null, null, null);
		} catch (Exception e) {
			logger.error("Error getting elements from store", e);
			fail("Error getting elements from store");
		}
		//expected results: the specified triple exists in the store
		if (result!=null) {
			fail("Query returned unexpected result in check 5, should have been null: " + result.toString());
		}
	}

	@Test
	public void testDisconnect() {
		if ((reader == null)) {
            fail("EDM Prov store not set up, cannot perform test");
        }
		try {
			reader.disconnect();
		} catch (Exception e) {
			logger.error("Error disconnecting from store", e);
			fail("Error disconnecting from store");
		}
	}

}
