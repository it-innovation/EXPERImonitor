/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2011
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
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
//	Created By :			Steve Taylor, modifying a file by Ken Meacham
//	Created Date :			2012-07-05
//	Created for Project :	WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.tools.analysis;

import eu.wegov.coordinator.web.WidgetDataAsJson;
import java.util.ArrayList;

import eu.wegov.common.model.TopicOpinionAnalysisTopic;
import eu.wegov.common.model.TopicOpinionAnalysisResult;

import eu.wegov.tools.WegovTool;
import eu.wegov.tools.analysis.WegovAnalysisTool;
import java.sql.SQLException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.log4j.Logger;

public class WegovAnalysisTool extends WegovTool {

  private static final Logger logger = Logger.getLogger(WegovAnalysisTool.class);

  int ownerPmId;
  String analysisType = null;
  String analysisSubType = null;
  int numTopicsToReturn = -1; // -1 = auto
  int numTermsPerTopic = -1; // -1 = auto

  String analysisLanguage = "en";

	private ArrayList<WeGovAnalysis> Analyses;

	public WegovAnalysisTool(String[] args, String myRunID, String myConfigurationID, String configPath) throws Exception {
		super(args, myRunID, myConfigurationID, configPath);


    System.out.println("Run ID for analysis = " + this.getMyRunId());
    // the owner PMID is to ensure that we do not get anyone else's data as input
    this.ownerPmId = coordinator.getPolicymakerOwnerIdFromRunId(this.getMyRunId());
    logger.debug("Owner PM ID = " + this.ownerPmId);



	}

	@Override
	protected void configure() throws Exception {
		setupAnalysis();
	}

  public int getOwnerPmId() {
    return this.ownerPmId;
  }

  public String getAnalysisType() {
    return this.analysisType;
  }

  public String getAnalysisSubType() {
    return this.analysisSubType;
  }

	protected String[] getMustHaveProperties() {
		// NEED TO CHANGE THESE!
    return new String[]{"oauthConsumerKey", "oauthConsumerSecret", "oauthConsumerAccessToken", "oauthConsumerAccessTokenSecret"};
	}

	private void setupAnalysis() throws Exception {

    Analyses = new ArrayList<WeGovAnalysis>();
		this.analysisType = getValueOfParameter("analysis.type");
		if ( (this.analysisType == null) || (this.analysisType.equals("")) ) throw new Exception("No analysis type defined");

		this.analysisSubType = getValueOfParameter("analysis.subType");
		if ( (this.analysisSubType == null) || (this.analysisSubType.equals("")) ) throw new Exception("No analysis sub type defined");

    // optional parameter - defaults to "en"
		this.analysisLanguage = getValueOfParameter("analysisLanguage", "en");


    // only for topic analysis - both optional - defaults to -1 (meaning automatically set by the topic analyser)
    this.numTopicsToReturn = Integer.parseInt(getValueOfParameter("numberOfTopicsToReturn", Integer.toString(-1)));
    this.numTermsPerTopic = Integer.parseInt(getValueOfParameter("numTermsPerTopic", Integer.toString(-1)));



    System.out.println("Setting up analysis of type: " + analysisType + " and sub type " + analysisSubType + ". Language = " + this.analysisLanguage);

    String inputSourceIDsJSONString = getValueOfParameter("analysis.input-data-spec");

    System.out.println("Input data spec string : " + inputSourceIDsJSONString);

    JSONArray inputSourceIDsJSON =
              (JSONArray) JSONSerializer.toJSON(inputSourceIDsJSONString);

    //System.out.println("Input data spec JSON : " + inputSourceIDsJSON);

    // get configuration & parse
    // find list of source activity & run IDs
    // store this list in an array list of objects


    if (this.analysisType.equals("topic-opinion")) {
      Analyses.add(new WeGovTopicAnalysis(
              this,
              this.analysisSubType,
              inputSourceIDsJSON,
              this.numTopicsToReturn,
              this.numTermsPerTopic,
              this.analysisLanguage
            )
      );
    }
    else if (this.analysisType.equals("behaviour")) {
      // choose language - "de" = german, currently anything else defaults to english
      if (this.analysisLanguage.equals("de")) {
        Analyses.add(new WeGovBehaviourAnalysis(this, this.analysisSubType, inputSourceIDsJSON, "de"));
      }
      else {
        Analyses.add(new WeGovBehaviourAnalysis(this, this.analysisSubType, inputSourceIDsJSON, "en"));
      }
    }
    else {
      System.out.println("WARNING: analysis type is not supported: " + analysisType);
    }

	}


	@Override
	public int execute() {
		int exitCode = 0;

		for (WeGovAnalysis Analysis : Analyses) {
			try {
				Analysis.execute();
				//Analysis.storeResults();
			}
			catch (Exception e) {
				reportError(e);
				exitCode = -1;
			}
		}

		return exitCode;
	}




  /*
  public TopicOpinionAnalysisResult getTopicAnalysisOutputFromDb (String runId) {

    WidgetDataAsJson resultWidgetData = loginService.getResultsDataForRun(runId);

    JSONObject tempDataAsJSONObject = (JSONObject)JSONSerializer.toJSON(resultWidgetData.getDataAsJson());

    String


  }
  */

	public static void main(String[] args) throws Exception {
		//Redirect stderr to stdout
		System.setErr(System.out);

		System.out.println("WeGov Analysis Tool v2.0");

    //String configPath = "C:/Users/kem/Projects/WeGov/workspace/wegov-parent/wegov-dashboard/coordinator.properties";

    // SJT work computer
    String configPath = "C:/Users/sjt/Documents/Work/WeGov-Code-External-SVN/trunk/wegov/wegov-parent/wegov-dashboard/coordinator.properties";

    //SJT home computer
    //String configPath = "C:/work_code/wegov/trunk/wegov/wegov-parent/wegov-dashboard/coordinator.properties";

    String myRunID = ""; // request to create a new Run
    String myConfigurationID = "";

		WegovAnalysisTool wegovAnalysis = null;
		int exitCode = 0;

		try {
			wegovAnalysis = new WegovAnalysisTool(args, myRunID, myConfigurationID, configPath);
			if (wegovAnalysis.error) {
				exitCode = -1;
			}
			else {
				exitCode = wegovAnalysis.execute();
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			//System.out.println();
			exitCode = -1;
		}

		System.out.println("/nAnalysis Tool exit code: " + exitCode);

		//System.err.flush();
		System.out.flush();
		//System.err.close();
		System.out.close();

		if (args.length == 0) {
			if (wegovAnalysis != null)
				wegovAnalysis.finalizeManualRun(exitCode);
		}

		System.exit(exitCode);
    }

}
