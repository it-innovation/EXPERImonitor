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
//	Created Date :			2011-12-19
//	Created for Project :	WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.tools.inject;

import java.util.ArrayList;

import eu.wegov.tools.WegovTool;

public class WegovInjectTool extends WegovTool {

    private ArrayList<SingleSiteInject> injections;

    public WegovInjectTool(String[] args, String myRunID, String myConfigurationID, String configPath) throws Exception {
        super(args, myRunID, myConfigurationID, configPath);
    }

    @Override
    protected void configure() throws Exception {
        setupInject();
    }

    protected String[] getMustHaveProperties() {
        return new String[]{"oauthConsumerKey", "oauthConsumerSecret", "oauthConsumerAccessToken", "oauthConsumerAccessTokenSecret"};
    }

    private void setupInject() throws Exception {
        injections = new ArrayList<SingleSiteInject>();
        String sitesStr = configuration.getValueOfParameter("sites");
        System.out.println("sites: " + sitesStr);

        if ((sitesStr == null) || (sitesStr.equals(""))) {
            throw new Exception("No sites defined");
        }

        String[] sites = sitesStr.split(",");

        for (String site : sites) {
            if (site.equals("twitter")) {
                injections.add(new TwitterInject(this));
            } //else if (site.equals("socialmention")) {
            //	injections.add(new SocialMentionInject(this));
            //}
            //else if (site.equals("poblish")) {
            //	injections.add(new PoblishInject(this));
            //}
            else {
                System.out.println("WARNING: site is not supported: " + site);
            }

            // Limit to a single site inject for now
            // TODO: remove this once multi-site injectes are supported by UI
            if (injections.size() > 0) {
                break;
            }
        }
    }

    /*
     * private boolean siteSelected(String site) { boolean siteSelected = false;
     * try { String siteValue = configuration.getValueOfParameter(site); if
     * (siteValue != null) siteSelected = siteValue.equals("true"); } catch
     * (Exception e) { e.printStackTrace(); }
     *
     * return siteSelected; }
     */
    @Override
    public int execute() {
        int exitCode = 0;

        for (SingleSiteInject inject : injections) {
            try {
                inject.execute();
            } catch (Exception e) {
                reportError(e);
                exitCode = -1;
            }
        }

        return exitCode;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("WeGov Inject Tool v1.0");

        String configPath = "C:/Users/kem/Projects/WeGov/workspace/wegov-search-tool/coordinator.properties";
        String myRunID = ""; // request to create a new Run
        String myConfigurationID = "";

        WegovInjectTool wegovInject = null;
        int exitCode = 0;

        try {
            wegovInject = new WegovInjectTool(args, myRunID, myConfigurationID, configPath);
            if (wegovInject.error) {
                exitCode = -1;
            } else {
                exitCode = wegovInject.execute();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println();
            exitCode = -1;
        }

        System.out.println("\nInject Tool exit code: " + exitCode);

        //System.err.flush();
        //System.out.flush();
        //System.err.close();
        //System.out.close();

        if (args.length == 0) {
            if (wegovInject != null) {
                wegovInject.finalizeManualRun(exitCode);
            }
        }

        System.exit(exitCode);
    }
}
