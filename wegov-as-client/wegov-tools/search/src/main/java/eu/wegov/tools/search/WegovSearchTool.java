/////////////////////////////////////////////////////////////////////////
//
// � University of Southampton IT Innovation Centre, 2011
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
package eu.wegov.tools.search;

import java.util.ArrayList;

import eu.wegov.tools.WegovTool;

public class WegovSearchTool extends WegovTool {

    private ArrayList<SingleSiteSearch> searches;

    public WegovSearchTool(String[] args, String myRunID, String myConfigurationID, String configPath) throws Exception {
        super(args, myRunID, myConfigurationID, configPath);
    }

    @Override
    protected void configure() throws Exception {
        setupSearch();
    }

    protected String[] getMustHaveProperties() {
        return new String[]{"oauthConsumerKey", "oauthConsumerSecret", "oauthConsumerAccessToken", "oauthConsumerAccessTokenSecret"};
    }

    private void setupSearch() throws Exception {
        searches = new ArrayList<SingleSiteSearch>();
        String sitesStr = getValueOfParameter("sites");
        String resultsType = getValueOfParameter("results.type", "static");
        System.out.println("sites: " + sitesStr);

        if ((sitesStr == null) || (sitesStr.equals(""))) {
            throw new Exception("No sites defined");
        }

        String[] sites = sitesStr.split(",");

        for (String site : sites) {
            if (site.equals("twitter")) {
                if (resultsType.equals("dynamic")) {
                    searches.add(new TwitterStreamSearch(this));
                } else {
                    searches.add(new TwitterSearch(this));
                }
            } else if (site.equals("socialmention")) {
                searches.add(new SocialMentionSearch(this));
            } else if (site.equals("poblish")) {
                searches.add(new PoblishSearch(this));
            } else if (site.equals("trendsmap")) {
                searches.add(new TrendsMapSearch(this));
            } else if (site.equals("facebook")) {
                searches.add(new FacebookGroupPostsSearch(this));
            } else {
                System.out.println("WARNING: site is not supported: " + site);
            }

            // Limit to a single site search for now
            // TODO: remove this once multi-site searches are supported by UI
            if (searches.size() > 0) {
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

        for (SingleSiteSearch search : searches) {
            try {
                search.execute();
                search.storeResults();
            } catch (Exception e) {
                reportError(e);
                exitCode = -1;
            }
        }

        return exitCode;
    }

    public static void main(String[] args) throws Exception {
        //Redirect stderr to stdout
        System.setErr(System.out);

        System.out.println("WeGov Search Tool v2.0");

        String configPath = "C:/Users/kem/Projects/WeGov/workspace/wegov-parent/wegov-dashboard/coordinator.properties";
        String myRunID = ""; // request to create a new Run
        String myConfigurationID = "";

        WegovSearchTool wegovSearch = null;
        int exitCode = 0;

        try {
            wegovSearch = new WegovSearchTool(args, myRunID, myConfigurationID, configPath);
            if (wegovSearch.error) {
                exitCode = -1;
            } else {
                exitCode = wegovSearch.execute();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println();
            exitCode = -1;
        }

        System.out.println("\nSearch Tool exit code: " + exitCode);

        //System.err.flush();
        System.out.flush();
        //System.err.close();
        System.out.close();

        if (args.length == 0) {
            if (wegovSearch != null) {
                wegovSearch.finalizeManualRun(exitCode);
            }
        }

        System.exit(exitCode);
    }
}
