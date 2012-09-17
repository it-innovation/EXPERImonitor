/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
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
//	Created By :			Maxim Bashevoy
//	Created Date :			2012-09-12
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////
package eu.experimedia.itinnovation.ecc.wegov;

import java.util.HashSet;
import java.util.Set;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

public class MetricGenerators {

    public static void main(String[] args) throws Throwable {
        MetricGenerator mg = makeWegovMetricGenerator();
        printMetricGenerator(mg);
    }

    public static MetricGenerator makeWegovMetricGenerator() {
        MetricGenerator theMetricGenerator = new MetricGenerator();
        theMetricGenerator.setName("Wegov Metric Generator");
        theMetricGenerator.setDescription("Metric generator for WeGov Social Analytics Dashboard");


        MetricGroup wegovMetricGroup = new MetricGroup();
        wegovMetricGroup.setName("Wegov Metric Group");
        wegovMetricGroup.setDescription("Metric group for all WeGov Social Analytics Dashboard metrics");
        theMetricGenerator.addMetricGroup(wegovMetricGroup);

        Entity twitterSchladming = new Entity();
        twitterSchladming.setName("Schladming Twitter Group");
        twitterSchladming.setDescription("People found in Schladming Twitter search for keyword \'Schladming\'");
        theMetricGenerator.addEntity(twitterSchladming);

        // NUMBER OF PEOPLE
        Attribute twitterSchladmingNumPeople = new Attribute();
        twitterSchladmingNumPeople.setName("Number of people");
        twitterSchladmingNumPeople.setDescription("Number of people who tweeted about Schladming");
        twitterSchladming.addAttribute(twitterSchladmingNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingNumPeople, MetricType.INTERVAL, new Unit("Person"));

        // NUMBER OF TWEETS
        Attribute twitterSchladmingNumTweets = new Attribute();
        twitterSchladmingNumTweets.setName("Number of tweets");
        twitterSchladmingNumTweets.setDescription("Number of tweets collected about Schladming");
        twitterSchladming.addAttribute(twitterSchladmingNumTweets);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingNumTweets, MetricType.INTERVAL, new Unit("Tweet"));

        // AVERAGE NUMBER OF TWEETS PER MINUTE
        Attribute twitterSchladmingAverageNumTweetsperMinute = new Attribute();
        twitterSchladmingAverageNumTweetsperMinute.setName("Average number of tweets per minute");
        twitterSchladmingAverageNumTweetsperMinute.setDescription("Average number of tweets collected about Schladming per minute");
        twitterSchladming.addAttribute(twitterSchladmingAverageNumTweetsperMinute);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingAverageNumTweetsperMinute, MetricType.RATIO, new Unit("Tweets per minute"));

        // TOPIC ANALYSIS TOPIC #1
        Attribute twitterSchladmingDiscussionTopic1 = new Attribute();
        twitterSchladmingDiscussionTopic1.setName("Topic analysis #1");
        twitterSchladmingDiscussionTopic1.setDescription("First topic of discussion on Twitter about Schladming");
        twitterSchladming.addAttribute(twitterSchladmingDiscussionTopic1);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingDiscussionTopic1, MetricType.NOMINAL, new Unit("Keyword"));

        // TOPIC ANALYSIS TOPIC #2
        Attribute twitterSchladmingDiscussionTopic2 = new Attribute();
        twitterSchladmingDiscussionTopic2.setName("Topic analysis #2");
        twitterSchladmingDiscussionTopic2.setDescription("Second topic of discussion on Twitter about Schladming");
        twitterSchladming.addAttribute(twitterSchladmingDiscussionTopic2);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingDiscussionTopic2, MetricType.NOMINAL, new Unit("Keyword"));

        // TOPIC ANALYSIS TOPIC #3
        Attribute twitterSchladmingDiscussionTopic3 = new Attribute();
        twitterSchladmingDiscussionTopic3.setName("Topic analysis #3");
        twitterSchladmingDiscussionTopic3.setDescription("Third topic of discussion on Twitter about Schladming");
        twitterSchladming.addAttribute(twitterSchladmingDiscussionTopic3);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingDiscussionTopic3, MetricType.NOMINAL, new Unit("Keyword"));

        // BROADCASTERS
        Attribute twitterSchladmingBroadcasterRoleNumPeople = new Attribute();
        twitterSchladmingBroadcasterRoleNumPeople.setName("Broadcaster role representation");
        twitterSchladmingBroadcasterRoleNumPeople.setDescription("Number of people identified as Broadcaster by Role analysis");
        twitterSchladming.addAttribute(twitterSchladmingBroadcasterRoleNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingBroadcasterRoleNumPeople, MetricType.INTERVAL, new Unit("Person"));

        // DAILY USERS
        Attribute twitterSchladmingDailyUserRoleNumPeople = new Attribute();
        twitterSchladmingDailyUserRoleNumPeople.setName("Daily user role representation");
        twitterSchladmingDailyUserRoleNumPeople.setDescription("Number of people identified as Daily users by Role analysis");
        twitterSchladming.addAttribute(twitterSchladmingDailyUserRoleNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingDailyUserRoleNumPeople, MetricType.INTERVAL, new Unit("Person"));

        // INFORMATION SEEKERS
        Attribute twitterSchladmingInformationSeekerRoleNumPeople = new Attribute();
        twitterSchladmingInformationSeekerRoleNumPeople.setName("Information Seeker role representation");
        twitterSchladmingInformationSeekerRoleNumPeople.setDescription("Number of people identified as Information seekers by Role analysis");
        twitterSchladming.addAttribute(twitterSchladmingInformationSeekerRoleNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingInformationSeekerRoleNumPeople, MetricType.INTERVAL, new Unit("Person"));

        // INFORMATION SOURCES
        Attribute twitterSchladmingInformationSourceRoleNumPeople = new Attribute();
        twitterSchladmingInformationSourceRoleNumPeople.setName("Information Source role representation");
        twitterSchladmingInformationSourceRoleNumPeople.setDescription("Number of people identified as Information sources by Role analysis");
        twitterSchladming.addAttribute(twitterSchladmingInformationSourceRoleNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingInformationSourceRoleNumPeople, MetricType.INTERVAL, new Unit("Person"));

        // RARE POSTERS
        Attribute twitterSchladmingRarePosterRoleNumPeople = new Attribute();
        twitterSchladmingRarePosterRoleNumPeople.setName("Rare Poster role representation");
        twitterSchladmingRarePosterRoleNumPeople.setDescription("Number of people identified as Rare posters by Role analysis");
        twitterSchladming.addAttribute(twitterSchladmingRarePosterRoleNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingRarePosterRoleNumPeople, MetricType.INTERVAL, new Unit("Person"));


        Entity wegovUsers = new Entity();
        wegovUsers.setName("Users of Wegov Dashboard");
        wegovUsers.setDescription("People using Wegov Dashboard");
        theMetricGenerator.addEntity(wegovUsers);

        // NUMBER OF PEOPLE USING WEGOV
        Attribute wegovUsersNumPeople = new Attribute();
        wegovUsersNumPeople.setName("Number of people");
        wegovUsersNumPeople.setDescription("Number of people who are using Wegov dashboard");
        wegovUsers.addAttribute(wegovUsersNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, wegovUsersNumPeople, MetricType.INTERVAL, new Unit("Person"));

        // AVERAGE NUMBER OF WIDGETS
        Attribute wegovUsersAverageNumWidgets = new Attribute();
        wegovUsersAverageNumWidgets.setName("Average number of widgets");
        wegovUsersAverageNumWidgets.setDescription("Average number of widgets created by people who are using Wegov dashboard");
        wegovUsers.addAttribute(wegovUsersAverageNumWidgets);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, wegovUsersAverageNumWidgets, MetricType.INTERVAL, new Unit("Widget"));

        return theMetricGenerator;
    }

    private static void addMetricToAttributeAndMetricGroup(MetricGroup metricGroup, Attribute attribute, MetricType metricType, Unit metricUnit) {
        MeasurementSet theMeasuringSet = new MeasurementSet();
        theMeasuringSet.setMetricGroupUUID(metricGroup.getUUID());
        theMeasuringSet.setAttributeUUID(attribute.getUUID());
        metricGroup.addMeasurementSets(theMeasuringSet);

        Metric theMetric = new Metric();
        theMetric.setMetricType(metricType);
        theMetric.setUnit(metricUnit);
        theMeasuringSet.setMetric(theMetric);
    }

    private static void printMetricGenerator(MetricGenerator mg) {
        pr(mg.getName() + " (" + mg.getDescription() + ")", 0);
        Set<MeasurementSet> allMeasurementSets = new HashSet<MeasurementSet>();

        for (MetricGroup mgroup : mg.getMetricGroups()) {
            allMeasurementSets.addAll(mgroup.getMeasurementSets());
        }

        for (Entity e : mg.getEntities()) {
            pr("Entity: " + e.getName() + " (" + e.getDescription() + ")", 1);
            for (Attribute a : e.getAttributes()) {
                pr("Attribute: " + a.getName() + " (" + a.getDescription() + ")", 2);

                for (MeasurementSet ms : allMeasurementSets) {
                    if (ms.getAttributeUUID().toString().equals(a.getUUID().toString())) {
                        pr("Metric type: " + ms.getMetric().getMetricType() + ", unit: " + ms.getMetric().getUnit() + "", 3);
                    }
                }
            }
        }
    }

    private static void pr(Object o, int indent) {
        String indentString = "";
        for (int i = 0; i < indent; i++) {
            indentString += "\t";
        }

        if (indent > 0) {
            System.out.println(indentString + "- " + o);
        } else {
            System.out.println(o);
        }
    }
}
