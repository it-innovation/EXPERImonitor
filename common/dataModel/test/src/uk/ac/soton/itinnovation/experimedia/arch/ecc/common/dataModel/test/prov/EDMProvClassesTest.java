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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMActivity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMEntity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;

public class EDMProvClassesTest {
	
	//private static Logger logger = Logger.getLogger("ProvTest");

	public static void main(String[] args) {
		
		EDMProvFactory factory = EDMProvFactory.getInstance("experimedia");
		
		try {
			//This is Bob.
			EDMAgent bob = factory.getOrCreateAgent("facebook_154543445", "Bob");
			bob.addOwlClass("foaf:Person");
			
			//This is a video about Schladming.
			EDMEntity video = factory.getOrCreateEntity("facebook_1545879879", "reallyCoolFacebookVideo");
			
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
			EDMAgent copyOfBob = factory.getOrCreateAgent("facebook_154543445", null);
			EDMActivity resumeVideo = copyOfBob.doDiscreteActivity("activity456", "ResumeVideo");
			resumeVideo.useEntity(video);
			bob.stopActivity(watchVideo);
		
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		//test print of resulting data
		//System.out.println(factory.toString());
		
		EDMProvReport report = factory.createProvReport();
		
		//clear factory
		factory.clear();
		factory = EDMProvFactory.getInstance("experimedia");
		
		//load prov report contents into factory
		try {
			factory.loadReport(report);
		} catch (Exception e) {
			System.out.println("Error loading report into factory.");
			e.printStackTrace();
		}
		
		//test print of resulting data
		System.out.println(factory.toString());
	}

}
