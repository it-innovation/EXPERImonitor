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
//      Created Date :          09-Jul-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.edm.test.prov;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentDataGeneratorTest {

	private static final Logger logger = LoggerFactory.getLogger(ExperimentDataGeneratorTest.class);

	public static void main(String[] args) {
		ExperimentDataGenerator ssggentest = new ExperimentDataGenerator();

		try {

			logger.info("Reading log");
			ssggentest.readLog("alice.txt");

			logger.info("Parsing log");
			ssggentest.parseLog("PerfectLog");

			logger.info("Prov report:");
			logger.info(ssggentest.getFactory().createProvReport().toString());

		} catch (Exception e) {
			logger.error("Exception caught: ", e);
		}
	}

}
