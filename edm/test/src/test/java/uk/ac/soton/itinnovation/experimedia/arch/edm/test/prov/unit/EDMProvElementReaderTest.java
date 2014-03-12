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
import java.util.Properties;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.dao.EDMProvElementReaderImpl;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvElementReader;

/**
 *
 * @author Vegard Engen
 */
@RunWith(JUnit4.class)
public class EDMProvElementReaderTest extends TestCase
{
    private IEDMProvElementReader reader;
    private static Logger logger;
	private static Properties props;
    
    @BeforeClass
    public static void beforeClass() {
        // Configure logging system
        logger = Logger.getLogger(EDMProvElementReaderTest.class);
		
		//TODO: prepare test data and add to store
        
        logger.info("EDMProvElementReader tests executing...");
    }
	
	@AfterClass
	public static void afterClass() {
		//TODO: clean up
	}
    
    @Before
    public void beforeEachTest() {
        try {
            props = getProperties();
        } catch (Exception ex) {
            logger.error("Failed to get properites.", ex);
        }
		
		if ((reader == null)) {
			try {
				reader = new EDMProvElementReaderImpl(props);
			} catch (Exception e) {
				logger.error("Error connecting to store", e);
			}
        }
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
            props.load(EDMProvElementReaderTest.class.getClassLoader().getResourceAsStream(EDMProvTestSuite.getPropertiesFile()));
        } catch (IOException ex) {
            logger.error("Error with loading properties file", ex);
            return null;
        }
        
        return props;
    }
	
	@Test
	public void testGetElement() {
		if ((reader == null)) {
            fail("EDM Prov store not set up, cannot perform test");
        }
		try {
			//TODO: public EDMProvBaseElement getElement(String IRI);
			//reader.getElement(null);
		} catch (Exception e) {
			logger.error("Error getting element from store", e);
			fail("Error getting element from store");
		}
		//TODO: check data integrity
	}
	
	@Test
	public void testGetElementWithDate() {
		if ((reader == null)) {
            fail("EDM Prov store not set up, cannot perform test");
        }
		try {
			//TODO: public EDMProvBaseElement getElement(String IRI, Date start, Date end);
			//reader.getElement(null,null,null);
		} catch (Exception e) {
			logger.error("Error getting element from store", e);
			fail("Error getting element from store");
		}
		//TODO: check data integrity
	}
	
	@Test
	public void testGetElementsWithDate() {
		if ((reader == null)) {
            fail("EDM Prov store not set up, cannot perform test");
        }
		try {
			//TODO:  public EDMProvDataContainer getElements(Date start, Date end);
			//reader.getElements(null, null);
		} catch (Exception e) {
			logger.error("Error getting elements from store", e);
			fail("Error getting elements from store");
		}
		//TODO: check data integrity
	}
	
	@Test
	public void testGetElementsWithDateAndType() {
		if ((reader == null)) {
            fail("EDM Prov store not set up, cannot perform test");
        }
		try {
			//TODO:  public EDMProvDataContainer getElements(EDMProvBaseElement.PROV_TYPE type, Date start, Date end);
			//reader.getElements(EDMProvBaseElement.PROV_TYPE.ePROV_AGENT, null, null);
		} catch (Exception e) {
			logger.error("Error getting elements from store", e);
			fail("Error getting elements from store");
		}
		//TODO: check data integrity
	}
	
	@Test
	public void testGetElementsFromRelations() {
		if ((reader == null)) {
            fail("EDM Prov store not set up, cannot perform test");
        }
		try {
			//TODO:  public EDMProvDataContainer getElements(Set<EDMTriple> rels);
			//reader.getElements(null);
		} catch (Exception e) {
			logger.error("Error getting elements from store", e);
			fail("Error getting elements from store");
		}
		//TODO: check data integrity
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
