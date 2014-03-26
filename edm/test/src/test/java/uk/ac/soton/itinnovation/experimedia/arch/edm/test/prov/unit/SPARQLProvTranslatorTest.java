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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.dao.SPARQLProvTranslator;

/**
 *
 * @author Vegard Engen
 */
@RunWith(JUnit4.class)
public class SPARQLProvTranslatorTest extends TestCase
{
    private SPARQLProvTranslator translator;
    private static Logger logger;
	private static Properties props;
    
    @BeforeClass
    public static void beforeClass() {
        // Configure logging system
        logger = Logger.getLogger(SPARQLProvTranslatorTest.class);
        
        logger.info("SPARQLProvTranslator tests executing...");
    }
    
    @Before
    public void beforeEachTest() {
        try {
            props = getProperties();
        } catch (Exception ex) {
            logger.error("Failed to get properites.", ex);
        }
		
		if ((translator == null)) {
			try {
				translator = new SPARQLProvTranslator(props);
			} catch (Exception e) {
				logger.error("Error setting up translator", e);
			}
        }
    }
    
    public Properties getProperties() {
        Properties props = new Properties();
        
        try {
            props.load(SPARQLProvTranslatorTest.class.getClassLoader().getResourceAsStream(EDMProvTestSuite.getPropertiesFile()));
        } catch (IOException ex) {
            logger.error("Error with loading properties file", ex);
            return null;
        }
        
        return props;
    }

	@Test
	public void testTranslate() {
		if ((translator == null)) {
            fail("Translator not set up, cannot perform test");
        }
		try {
			//TODO: translate(LinkedList<HashMap<String, String>> sparqlResult)
			//translator.translate(null);
		} catch (Exception e) {
			logger.error("Error translating SPARQL result to provenance data model", e);
			fail("Error translating SPARQL result to provenance data model");
		}
	}
}
