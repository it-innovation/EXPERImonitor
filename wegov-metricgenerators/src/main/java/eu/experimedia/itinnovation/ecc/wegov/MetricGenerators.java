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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

/**
 * Generates sample WeGov metrics based on the following:
 *
 * Twitter search performed on a keyword, say "weather". That search returns 100
 * tweets and 100 associated users. This is our "Community".
 *
 * Metric generators below are all about that Community.
 *
 *
 *
 * @author max
 */
public class MetricGenerators {

    // Based on what wegov analysis components can do
    public void analysisMetricGenerator() {
        MetricGenerator theMetricGenerator = new MetricGenerator();
        theMetricGenerator.setName("Wegov Analysis Metric Generator");

        MetricGroup topicAnalysisMetricGroup = new MetricGroup();
        topicAnalysisMetricGroup.setName("Topic Analysis Metric Group");
        topicAnalysisMetricGroup.setName("All metrics to do with Topic Analysis");
        theMetricGenerator.addMetricGroup(topicAnalysisMetricGroup);

        MetricGroup discussionActivityMetricGroup = new MetricGroup();
        topicAnalysisMetricGroup.setName("Discussion Activity Metric Group");
        topicAnalysisMetricGroup.setName("All metrics to do with Discussion Activity");
        theMetricGenerator.addMetricGroup(discussionActivityMetricGroup);

        MetricGroup roleAnalysisMetricGroup = new MetricGroup();
        topicAnalysisMetricGroup.setName("Role Analysis Metric Group");
        topicAnalysisMetricGroup.setName("All metrics to do with Role Analysis");
        theMetricGenerator.addMetricGroup(roleAnalysisMetricGroup);

        MetricGroup buzzAnalysisMetricGroup = new MetricGroup();
        topicAnalysisMetricGroup.setName("Buzz Analysis Metric Group");
        topicAnalysisMetricGroup.setName("All metrics to do with Buzz Analysis");
        theMetricGenerator.addMetricGroup(buzzAnalysisMetricGroup);

        Entity e1 = new Entity();
        e1.setName("Topic Analysis");
        e1.setDescription("Topics that came up from tweets about weather in the past 5 minutes");

        Attribute a1e1 = new Attribute();
        a1e1.setName("Discussion topics variety");
        a1e1.setDescription("Number of topics that came from topic analysis");
        e1.addAttribute(a1e1);

        Attribute a2e1 = new Attribute();
        a2e1.setName("Discussion topics keywords");
        a2e1.setDescription("Keywords identifying the topics from topic analysis");
        e1.addAttribute(a2e1);

        Attribute a3e1 = new Attribute();
        a3e1.setName("Most controvertial topics");
        a3e1.setDescription("Topics with controvercy value above 5");
        e1.addAttribute(a3e1);

        Attribute a4e1 = new Attribute();
        a4e1.setName("Positive topics");
        a4e1.setDescription("Topics with sentiment value 0");
        e1.addAttribute(a4e1);

        Attribute a5e1 = new Attribute();
        a5e1.setName("Negative topics");
        a5e1.setDescription("Topics with sentiment value 0");
        e1.addAttribute(a5e1);

        Entity e2 = new Entity();
        e2.setName("Discussion Activity");
        e2.setDescription("Avalanche of tweets about weather in the past 5 minutes");

        Attribute a1e2 = new Attribute();
        a1e2.setName("Discussion activity over time");
        a1e2.setDescription("Number of tweets per minute/day/week/month/year");
        e2.addAttribute(a1e2);

        // ...

        MeasurementSet ms1 = new MeasurementSet();
        ms1.setMetricGroupUUID(topicAnalysisMetricGroup.getUUID());
        ms1.setAttributeUUID(a1e1.getUUID());
        topicAnalysisMetricGroup.addMeasurementSets(ms1);

        // ...

        Metric memMetric = new Metric();
        memMetric.setMetricType(MetricType.RATIO);
        memMetric.setUnit(new Unit("Topics"));
        ms1.setMetric(memMetric);

    }
    
    public void searchMetricGenerator() {
        // ...
    }
    
    public void injectionMetricGenerator() {
        // ...
    }
}
