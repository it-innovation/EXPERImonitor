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
//      Created Date :          16-Oct-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.test.prov;

//import java.util.logging.Logger;

import org.apache.log4j.Logger;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMActivity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMEntity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;

public class EDMProvClassesTest {
	
	static Logger logger = Logger.getLogger(EDMProvClassesTest.class);

	public static void main(String[] args) {
		
		EDMProvFactory factory = EDMProvFactory.getInstance("experimedia",
				"http://it-innovation.soton.ac.uk/ontologies/experimedia#");
		
		factory.addOntology("foaf", "http://xmlns.com/foaf/0.1/");
		
		EDMProvReport report = null;
		
		try {
			//This is Bob.
			EDMAgent bob = factory.createAgent("facebook_154543445", "Bob");
			bob.addOwlClass(factory.getNamespaceForPrefix("foaf") + "Person");
			
			//This is a video about Schladming.
			EDMEntity video = factory.createEntity("facebook_1545879879", "reallyCoolFacebookVideo");
			
			//Bob starts to watch the video and pauses it when he sees something interesting.
			EDMActivity watchVideo = bob.startActivity("activity123", "WatchVideo");
			watchVideo.useEntity(video);
			EDMActivity pauseVideo = bob.doDiscreteActivity("activity234", "PauseVideo");
			pauseVideo.useEntity(video);
			
			report = factory.createProvReport();
			System.out.println(report.toString());
			
			//Bob logs in to his FB account and posts something
			EDMActivity writePost = bob.startActivity("activity345", "WritePost");
			writePost.generateEntity("facebook_98763242347", "BobsFacebookPost", "1280512800");
			bob.stopActivity(writePost);
			
			//Bob goes back to watch the rest of the video.
			EDMAgent copyOfBob = factory.getAgent("facebook_154543445");
			EDMActivity resumeVideo = copyOfBob.doDiscreteActivity("activity456", "ResumeVideo");
			resumeVideo.useEntity(video);
			bob.stopActivity(watchVideo);
			
			report = factory.createProvReport();
			System.out.println(report.toString());
		
		} catch (Exception e) {
			logger.error("Error filling EDMProvFactory with test data", e);
		}
		
		//test print of resulting data
		//logger.info(factory.toString());
		
		report = factory.createProvReport();
		
		//clear factory
		factory.clear();
		factory = EDMProvFactory.getInstance("experimedia",
				"http://it-innovation.soton.ac.uk/ontologies/experimedia#");
		
		//load prov report contents into factory
		try {
			factory.loadReport(report);
		} catch (Exception e) {
			logger.error("Error loading EDMProvReport", e);
		}
		
		//test print of resulting data
		logger.info(factory.toString());
	}

}
