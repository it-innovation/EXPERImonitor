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
//      Created By :            Vegard Engen
//      Created Date :          2012-08-22
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.unit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *
 * @author Vegard Engen
 */
@RunWith(JUnit4.class)
public class EDMTestSuite extends TestCase
{
    static Logger log = Logger.getLogger(EDMTestSuite.class);

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(getTestSuite());

        log.info("EDM Tests Complete");
        System.exit(0);
    }

    // Private methods -----------------------------------------------------------
    private static Test getTestSuite()
    {
        TestSuite suite = new TestSuite("EDM Tests");

        suite.addTestSuite(AGeneralTest.class);
        suite.addTestSuite(APopulateDBTest.class);
        suite.addTestSuite(ExperimentTest.class);

        return suite;
    }
}