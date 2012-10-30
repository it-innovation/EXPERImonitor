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
//	Created By :			Ken Meacham
//	Created Date :			2011-09-29
//	Created for Project :	WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.tools;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import eu.wegov.coordinator.Activity;
import eu.wegov.coordinator.Configuration;
import eu.wegov.coordinator.ConfigurationSet;
import eu.wegov.coordinator.Coordinator;
import eu.wegov.coordinator.Policymaker;
import eu.wegov.coordinator.Run;
import eu.wegov.coordinator.Worksheet;
import org.apache.log4j.Logger;

public abstract class WegovTool {

	private String myRunID = null;
	private String myConfigurationID = null;
	private String configPath = null;
	private Run previousRun = null;
	private Run run = null;
	protected Coordinator coordinator = null;
	protected Activity activity = null;
	protected Policymaker policymaker;
	protected ConfigurationSet configurationSet = null;
	protected Configuration configuration = null;
	private Properties properties;
	protected boolean error;

  private static final Logger log = Logger.getLogger(WegovTool.class);

	public WegovTool(String[] args) throws Exception {
        init(args);
	}

	public WegovTool(String myRunID, String myConfigurationID, String configPath) throws Exception {
		init(myRunID, myConfigurationID, configPath);
	}

	public WegovTool(String[] args, String myRunID, String myConfigurationID, String configPath) throws Exception {
		if (args.length > 0) {
			init(args);
		}
		else {
			init(myRunID, myConfigurationID, configPath);
		}
	}

	private void init(String[] args) throws Exception {
        if (args.length > 0) {
            this.myRunID = args[0].trim();
            this.myConfigurationID = args[1].trim();

            if (args.length > 2) {
                this.configPath = args[2].trim();
            }
        }

        init();
	}

	private void init(String myRunID, String myConfigurationID, String configPath) throws Exception {
		this.myRunID = myRunID;
		this.myConfigurationID = myConfigurationID;
		this.configPath = configPath;



		init();
	}

	private void init() throws Exception {
		setCoordinator();
		setupDatabase();
		getRun();

    log.info(
            "Initialising wegovTool - runID = " + this.myRunID
            + ", configID = " + this.myConfigurationID
            + ", config path = " + this.configPath + "");

		try {
			getConfiguration();
			configure();
		} catch (Exception e) {
			// Now we have a run, report error via the run comment
			reportError(e);
		}
	}

	private void setCoordinator() throws Exception {
		System.out.println("Coordinator config path: " + configPath);
		this.coordinator = new Coordinator(configPath);

        // Get properties from configuration file (this should ideally come directly from coordinator)
		this.properties = new Properties();
        File tempFile = new File(configPath);
        FileInputStream in = new FileInputStream(tempFile.getAbsolutePath());
        properties.load(in);
        in.close();

        verifyProperties(this.properties);
	}

    private void verifyProperties(Properties properties) throws Exception {
        String result = null;
    	String[] mustHaveProperties = getMustHaveProperties();

    	if (mustHaveProperties == null) return;

        for (String mustHaveproperty : mustHaveProperties) {
            if (!properties.containsKey(mustHaveproperty)) {
                result = "Missing key: " + mustHaveproperty;
                break;
            } else {
                if (properties.getProperty(mustHaveproperty).equals("")) {
                    result = "Missing property for key: " + mustHaveproperty;
                    break;
                }
            }
        }

        if (result != null)
            throw new Exception(result);
    }

	protected String[] getMustHaveProperties() {
		return null;
	}

    public Properties getCoordinatorProperties() {
		return properties;
	}

	protected String getValueOfParameter(String param) throws Exception {
		String val = null;
		try {
			val = configuration.getValueOfParameter(param);
		}
		catch (Exception e) {
			throw new Exception("Parameter is undefined: " + param);
		}
		return val;
	}

	protected String getValueOfParameter(String param, String defaultVal) {
		String val = null;
		try {
			val = getValueOfParameter(param);
		}
		catch (Exception e) {
			val = defaultVal;
		}
		return val;
	}

	private void setupDatabase() throws Exception {
		coordinator.setupWegovDatabase();
	}

	public String getMyRunId() {
		return myRunID;
	}

	public void setMyRunID(String myId) {
		this.myRunID = myId;
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public String getMyConfigurationID() {
		return myConfigurationID;
	}

	public void setMyConfigurationID(String myConfigurationID) {
		this.myConfigurationID = myConfigurationID;
	}

	public Coordinator getCoordinator() {
		return coordinator;
	}

	public Run getRun() throws Exception {
		if (run == null) {
			if (myRunID.equals("")) {
				run = createRun();
				myRunID = new Integer(run.getID()).toString();
			}
			else {
				System.out.println("Getting run for ID: " + myRunID);
				run = coordinator.getRunByID(Integer.parseInt(myRunID));
				configurationSet = run.getConfigurationSet();
				//activity = configurationSet.getActivity();
				activity = run.getActivity();
				policymaker = activity.getPolicyMaker();
				System.out.println("My run: " + run.toString());
				System.out.println("My configurationSet: " + configurationSet.toString());
				System.out.println("My activity: " + activity.toString());
				System.out.println("My policymaker: " + policymaker.toString());
			}

			if (run != null) {
				System.out.println("My name: " + run.getName());
				System.out.println("My comment: " + run.getComment());
				System.out.println("My status: " + run.getStatus() + "\n");
			} else {
				throw new Exception("Did not find myself in the database :(");
			}
		}

		return run;
	}

	public Activity getActivity() {
		return activity;
	}

	public Policymaker getPolicyMaker() {
		return policymaker;
	}

	/*
	 * This method is used for creating a run in the database when the tool has not been launched by the coordinator
	 */
	public Run createRun() throws Exception {
        coordinator.setupWegovDatabase();

        //int configSetID = 63; //TODO: should be configurable
        //int configSetID = 81; //TODO: should be configurable
        //int configSetID = 47; //TODO: should be configurable
        //int configSetID = 54; //TODO: should be configurable
        int configSetID = 354; //TODO: should be configurable

        configurationSet = coordinator.getConfigurationSetByID(configSetID);
        System.out.println("\nSelected Configuration Set:");
        System.out.println(configurationSet);

        /*
        Worksheet worksheet1 = coordinator.getWorksheetByID(1);

        Activity worksheet1Activity = worksheet1.createActivity("Test Activity", "Test Activity");
        worksheet1Activity.setConfigurationSet(configurationSet);

        System.out.println("\nActivity configuration set: ");
        this.configuration = worksheet1Activity.getConfigurationSet().getConfigurations().get(0);
        System.out.println(configuration);

		Run newrun = coordinator.createRun(worksheet1Activity, "Test run", "Test");
		newrun.setConfigurationSet(configurationSet);
		*/

		activity = configurationSet.getActivity();
		policymaker = activity.getPolicyMaker();

		//previousRun = activity.getLastRun();

		System.out.println("\nActivity configuration set: ");
		configuration = configurationSet.getConfigurations().get(0);
		System.out.println(configuration);

		Run newrun = coordinator.createRun(activity, "Test run", "Test");
		newrun.setName("Test run [" + newrun.getID() + "] for activity [" + activity.getID() + "]");
		newrun.setConfigurationSet(configurationSet);

		return newrun;
	}

	public Configuration getConfiguration() throws Exception {
		if (configuration != null)
			return configuration;

		configuration = run.getConfigurationSet().getConfigurationByID(Integer.parseInt(myConfigurationID));
		System.out.println(configuration.toString() + "\n");

		return configuration;
	}

	protected abstract void configure() throws Exception;

	protected abstract int execute() throws Exception;

	protected void reportError(Exception e) {
		this.error = true;
		//System.err.println();
		e.printStackTrace(System.out);
		System.out.println();
		setRunComment("ERROR: " + e.getMessage());
	}

	public void reportMessage(String message) {
		setRunComment(message);
	}

	private void setRunComment(String comment) {
		if (run != null)
			run.setComment(comment);
		else
			System.out.println("WARNING: run object is null");
	}

	public void finalizeManualRun(int exitCode) {
		if (exitCode == 0) {
			run.setName(run.getName() + " [" + run.getID() + "]");
			run.setStatus("finished");
		}
		else {
			run.setStatus("failed");
		}
		run.setWhenFinished(new Timestamp(new Date().getTime()));
	}

	public Run getPreviousRun() throws Exception {
		System.out.println("\nGetting previous run for my activity");
		System.out.println("My run: " + run.toString());
		//System.out.println("My activity: " + activity.toString());
		if (previousRun == null) {
			previousRun = activity.getPreviousRunWithResults(run.getID());
		}
		System.out.println("Previous run: " + previousRun);
		System.out.println();
		return previousRun;
	}

}
