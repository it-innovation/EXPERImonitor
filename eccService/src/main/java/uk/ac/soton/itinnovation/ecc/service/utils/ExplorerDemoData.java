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
//      Created By :            Simon Crowle
//      Created Date :          21-Jul-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.ecc.service.utils;

import uk.ac.soton.itinnovation.ecc.service.domain.explorer.metrics.*;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.provenance.*;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.distributions.*;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.*;

import uk.ac.soton.itinnovation.ecc.service.domain.EccMeasurement;

import java.util.*;
import java.text.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.*;

/**
 * This class is only to be used during the development of the explorer service
 * and should be removed once service is complete.
 */
public class ExplorerDemoData {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private enum ProvAct { eWeather, eTweet, eLiftWaiting };
    
    // All attributes are public - this is a demo support class only, so we don't care
    public UUID expID = UUID.fromString("c91c05ed-c6ba-4880-82af-79eb5d4a58cd");
    public Date expStartDate;
    public EccExperimentSummary expSummary;

    public EccParticipantResultSet eccParticipants;
    public EccParticipant AlicePART, BobPART, CarolPART;
    public HashMap<String, EccParticipant> participantsByName;

    public EccAttributeInfo qoeAttrONE;
    public EccAttributeInfo qoeAttrTWO;
    public EccAttributeInfo qoeAttrTHREE;
    public EccParticipantAttributeResultSet partAttrInfoSet;
    public HashMap<String, EccNOMORDAttributeSummary> qoeSummaryDistribData;
    public HashMap<String, EccNOMORDParticipantSummary> qoeParticipantSummaryData;
    public ArrayList<EccNOMORDStratifiedSummary> qoeStratifiedSummaryDistribData;

    public ArrayList<EccActivity> linearActivities;
    public ArrayList<EccApplication> linearApplications;
    public HashMap<String, EccService> servicesByIRI;

    public HashMap<String, EccParticipantActivitySummaryResultSet> participantActivitySummary;
    public HashMap<String, EccParticipantActivityResultSet> participantActivities;
    public HashMap<String, EccActivityApplicationResultSet> activityApplications;
    public HashMap<String, EccApplicationServiceResultSet> applicationServices;
    
    public HashMap<UUID, EccAttributeInfo> qosAttributesByID;
    public HashMap<String, EccAttributeResultSet> qosAttributesByIRI;
    
    public HashMap<UUID, EccINTRATSeries>  qosSeries; 
    public HashMap<UUID, EccINTRATSummary> qosSummaries;

    private final Questionnaire questionnaire;
    public HashMap<UUID, UUID> usersParticipants;

    public ExplorerDemoData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m");
        questionnaire = new Questionnaire();

        try {
            expStartDate = sdf.parse("2014-07-31 07:00");
            
        } catch (ParseException pe) {
            logger.error("Could not create demo experiment date! ", pe);
        }

        usersParticipants = new HashMap<>();

        createParticipants();
        createPARTAttributeInfo();
        createNOMORDDistributionData();
        createActivityData();
        createParticipantActivitySummaryData();
        createApplicationData();
        createServiceData();
        
        expSummary = new EccExperimentSummary("Test experiment",
                "This experiment has been created to test the EXPERIMonitor data explorer",
                expID.toString(),
                3, // Alice, Bob, Carol
                10, // 10 activities
                3, // 3 applications
                1);  // 1 service
    }

    public EccParticipant getParticipant(String IRI) {
        EccParticipant result = null;

        ArrayList<EccParticipant> parts = eccParticipants.getParticipants();
        for (EccParticipant part : parts) {
            if (part.getIRI().equals(IRI)) {
                result = part;
                break;
            }
        }

        return result;
    }

    public EccParticipantResultSet getParticipantsByAttributeScaleLabel(String attrName, String nomOrdLabel) {
        EccParticipantResultSet partInfoSet = new EccParticipantResultSet();

        logger.debug("Requested users for attribute '" + attrName + "' and label '" + nomOrdLabel + "'");

        int option = -1;
        for (EccAttributeInfo attr : partAttrInfoSet.getQoEAttributes()) {
            if (attr.getName().equals(attrName)) {
                int counter = 1;
                for (String label : attr.getMetaContent().split(",")) {
                    if (label.trim().equals(nomOrdLabel.trim())) {
                        option = counter;
                        break;
                    }
                    counter++;
                }
                break;
            }
        }

        if (option > 0) {
            for (User u : questionnaire.getUsersForQuestionAndOption(option, questionnaire.getQuestionByName(attrName))) {
                for (EccParticipant participant : eccParticipants.getParticipants()) {
                    if (participant.getMetricEntityID().equals(usersParticipants.get(u.getId()))) {
                        partInfoSet.addParticipant(participant);
                        logger.debug("Matching user found: " + participant.getName());
                    }
                }
            }
        } else {
            logger.error("No user selected this option");
        }

        return partInfoSet;
    }

    public EccParticipantActivitySummaryResultSet getActivitySummaryByParticipant(String partIRI) {
        return participantActivitySummary.get(partIRI);
    }

    public EccParticipantActivityResultSet getActivitiesByParticipant(String partIRI) {
        return participantActivities.get(partIRI);
    }

    public EccActivityApplicationResultSet getApplicationsByActivity(String actIRI) {
        return activityApplications.get(actIRI);
    }

    public EccApplicationServiceResultSet getServicesByApplication(String appIRI) {
        return applicationServices.get(appIRI);
    }

    public EccINTRATSummary getINTRATDistDataDiscrete( UUID attrID, ArrayList<Date> stamps )
    {
        EccINTRATSummary result = null;
        
        // Safety
        if ( attrID != null && stamps != null )
        {
            EccAttributeInfo info = qosAttributesByID.get( attrID );
            EccINTRATSeries srcSeries = qosSeries.get( attrID );
            
            if ( info != null && srcSeries != null )
            {
                // Copy source series data with just those timestamps
                ArrayList<EccMeasurement> targMeasures = new ArrayList<>();
       
                for ( EccMeasurement srcM : srcSeries.getValues() )
                    for ( Date targDate : stamps )
                        if ( srcM.getTimestamp().equals(targDate) )
                            targMeasures.add( new EccMeasurement(srcM) );
                
                // If we've got any data, create a summary
                if ( !targMeasures.isEmpty() )
                {
                    DescriptiveStatistics ds = new DescriptiveStatistics();
                
                    for ( EccMeasurement m : targMeasures )
                        ds.addValue( Double.parseDouble(m.getValue()) );
                    
                    result = new EccINTRATSummary( info,
                                                   ds.getMin(), 
                                                   ds.getMean(), 
                                                   ds.getMax() );
                }
            }
        }
        
        return result;
    }

    public EccINTRATSummary getQosSummary( UUID attrID )
    {
        EccINTRATSummary result = null;
        
        if ( attrID != null )
        {
            // Find QoS series and calculate summary data
            EccINTRATSeries series = qosSeries.get( attrID );
            
            if ( series != null )
            {
                DescriptiveStatistics ds = new DescriptiveStatistics();
                
                for ( EccMeasurement m : series.getValues() )
                    ds.addValue( Double.parseDouble(m.getValue()) );
                
                result = new EccINTRATSummary( qosAttributesByID.get( attrID ),
                                               ds.getMin(), ds.getMean(), ds.getMax());
            }
        }        
        
        return result;
    }

    public EccINTRATSeriesSet getINTRATSeriesHighlightActivities( UUID   seriesAttrID,
                                                                  String partIRI,
                                                                  String activityLabel )
    {
        EccINTRATSeriesSet result = new EccINTRATSeriesSet();
        
        // Safety
        if ( seriesAttrID != null && partIRI != null && activityLabel != null )
        {
            // Get all activities for this participant
            EccParticipantActivityResultSet pars = participantActivities.get( partIRI );
            
            EccINTRATSeries srcSeries = qosSeries.get( seriesAttrID );
            EccINTRATSeries hilights  = null;
            
            if ( pars != null && srcSeries != null )
            {
                // Pick out just the activities with the relevant label
                ArrayList<EccActivity> targActs = new ArrayList<>();
                
                for ( EccActivity act : pars.getActivities() )
                    if ( act.getName().equals(activityLabel) )
                        targActs.add( act );
                
                // Create a highlight series based on activity time frame
                String seriesName = srcSeries.getKey() + " (" + pars.getParticipant().getName() + ")";
                hilights = createHiliteSeries( seriesName, srcSeries, targActs );
            }
            
            // Add results as required
            if ( srcSeries != null ) result.addSeries( srcSeries );
            if ( hilights  != null ) result.addSeries( hilights );
        }
        
        return result;
    }

    // Private methods ---------------------------------------------------------
    private void createParticipants() {
        eccParticipants    = new EccParticipantResultSet();
        participantsByName = new HashMap<>();

        AlicePART = new EccParticipant("Alice", "Alice is a test participant",
                UUID.fromString("02bcc340-3254-4eee-b9dc-5132c2e25cbf"),
                "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_02bcc340-3254-4eee-b9dc-5132c2e25cbf");

        participantsByName.put( "Alice", AlicePART );
        eccParticipants.addParticipant(AlicePART);
        
        BobPART = new EccParticipant("Bob", "Bob is a test participant",
                UUID.fromString("74e3e2aa-0d86-49c9-8336-a44a1482a887"),
                "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_74e3e2aa-0d86-49c9-8336-a44a1482a887");

        participantsByName.put( "Bob", BobPART );
        eccParticipants.addParticipant(BobPART);
        
        CarolPART = new EccParticipant("Carol", "Carol is a test participant",
                UUID.fromString("81ace737-818e-42e3-b7c3-d2c1fa1d7a0c"),
                "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_81ace737-818e-42e3-b7c3-d2c1fa1d7a0c");

        participantsByName.put( "Carol", CarolPART );
        eccParticipants.addParticipant(CarolPART);
    }

    private void createPARTAttributeInfo() {
        qoeAttrONE = new EccAttributeInfo("Ease of use: Ski lift app",
                "Questionnaire: item 1",
                UUID.fromString("eaad2d89-d543-47b2-9485-5b1d7ba6cf29"),
                "scale item",
                "ORDINAL",
                "Likert scale",
                "very difficult, difficult, not easy/difficult, easy, very easy");

        qoeAttrTWO = new EccAttributeInfo("Usefulness: Ski lift app",
                "Questionnaire: item 2",
                UUID.fromString("e7f7a75e-4a41-421f-89bf-20bb4bab57ab"),
                "scale item",
                "ORDINAL",
                "Likert scale",
                "not at all useful, not very useful, sometimes useful, often useful, always useful");

        qoeAttrTHREE = new EccAttributeInfo("Responsiveness: Ski lift app",
                "Questionnaire: item 3",
                UUID.fromString("e0069254-3875-44e6-8670-4c477057c578"),
                "scale item",
                "ORDINAL",
                "Likert scale",
                "very unresponsive, not very responsive, moderately responsive, quite responsive, very responsive");

        partAttrInfoSet = new EccParticipantAttributeResultSet();
        partAttrInfoSet.addQoEAttribute(qoeAttrONE);
        partAttrInfoSet.addQoEAttribute(qoeAttrTWO);
        partAttrInfoSet.addQoEAttribute(qoeAttrTHREE);
    }

    private String getStringForIndexStartingAtOne(String labels, int index) {
        String result = labels.split(",")[index - 1];

        return result.trim();
    }

    private void createNOMORDDistributionData() {

        Question q1 = new Question(qoeAttrONE.getName());
        Question q2 = new Question(qoeAttrTWO.getName());
        Question q3 = new Question(qoeAttrTHREE.getName());
        questionnaire.addQuestion(q1);
        questionnaire.addQuestion(q2);
        questionnaire.addQuestion(q3);
        User alice = new User(AlicePART.getMetricEntityID(), AlicePART.getName());
        questionnaire.addUser(alice);
        usersParticipants.put(alice.getId(), AlicePART.getMetricEntityID());
        User bob = new User(BobPART.getMetricEntityID(), BobPART.getName());
        questionnaire.addUser(bob);
        usersParticipants.put(bob.getId(), BobPART.getMetricEntityID());
        User carol = new User(CarolPART.getMetricEntityID(), CarolPART.getName());
        questionnaire.addUser(carol);
        usersParticipants.put(carol.getId(), CarolPART.getMetricEntityID());

        // Create summary for each participant
        qoeParticipantSummaryData = new HashMap<>();

        // Alice
        int[] answers = new int[]{1, 3, 5};
        EccNOMORDParticipantSummary ps = new EccNOMORDParticipantSummary(AlicePART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(alice, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(alice, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(alice, q3, answers[2]));
        qoeParticipantSummaryData.put(AlicePART.getIRI(), ps);

        // Bob
        answers = new int[]{1, 1, 5};
        ps = new EccNOMORDParticipantSummary(BobPART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(bob, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(bob, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(bob, q3, answers[2]));
        qoeParticipantSummaryData.put(BobPART.getIRI(), ps);

        // Carol
        answers = new int[]{4, 5, 5};
        ps = new EccNOMORDParticipantSummary(CarolPART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(carol, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(carol, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(carol, q3, answers[2]));
        qoeParticipantSummaryData.put(CarolPART.getIRI(), ps);

        qoeSummaryDistribData = new HashMap<>();

        // Distribution of answers to Question 1: number of people in each category 1 - 5
        // getDistributionOfAnswersForQuestion(q1)
        EccNOMORDAttributeSummary data = new EccNOMORDAttributeSummary(qoeAttrONE, createNOMORDDistributionDataSet(qoeAttrONE.getMetaContent(), q1));
        qoeSummaryDistribData.put(qoeAttrONE.getName(), data);
        qoeAttrONE.setSampleCount(distributionDataTotal(data));

        // Distribution of answers to Question 2: number of people in each category 1 - 5
        data = new EccNOMORDAttributeSummary(qoeAttrTWO, createNOMORDDistributionDataSet(qoeAttrTWO.getMetaContent(), q2));
        qoeSummaryDistribData.put(qoeAttrTWO.getName(), data);
        qoeAttrTWO.setSampleCount(distributionDataTotal(data));

        // Distribution of answers to Question 3: number of people in each category 1 - 5
        data = new EccNOMORDAttributeSummary(qoeAttrTHREE, createNOMORDDistributionDataSet(qoeAttrTHREE.getMetaContent(), q3));
        qoeSummaryDistribData.put(qoeAttrTHREE.getName(), data);
        qoeAttrTHREE.setSampleCount(distributionDataTotal(data));

        // Stratified summary data (simply mock up this data)
        qoeStratifiedSummaryDistribData = new ArrayList<>();

        // Number of people who picked answer i for each question
        for (int i = 1; i < 6; i++) {
            EccNOMORDStratifiedSummary nss = new EccNOMORDStratifiedSummary(i + " of 5");
            nss.addStratifiedItem(new EccItemCount(qoeAttrONE.getName(), questionnaire.getDistributionOfAnswersForOptionAndQuestion(i, q1)));
            nss.addStratifiedItem(new EccItemCount(qoeAttrTWO.getName(), questionnaire.getDistributionOfAnswersForOptionAndQuestion(i, q2)));
            nss.addStratifiedItem(new EccItemCount(qoeAttrTHREE.getName(), questionnaire.getDistributionOfAnswersForOptionAndQuestion(i, q3)));
            qoeStratifiedSummaryDistribData.add(nss);
        }

//        // Number of people who picked option 1 for each question
//        // getDistributionOfAnswersForOption(o1)
//        EccNOMORDStratifiedSummary nss = new EccNOMORDStratifiedSummary("1 of 5");
//        nss.addStratifiedItem(new EccItemCount(qoeAttrONE.getName(), questionnaire.getDistributionOfAnswersForOptionAndQuestion(1, q1)));
//        nss.addStratifiedItem(new EccItemCount(qoeAttrTWO.getName(), questionnaire.getDistributionOfAnswersForOptionAndQuestion(1, q2)));
//        nss.addStratifiedItem(new EccItemCount(qoeAttrTHREE.getName(), questionnaire.getDistributionOfAnswersForOptionAndQuestion(1, q3)));
//        qoeStratifiedSummaryDistribData.add(nss);
//
//        // Number of people who picked option 2 for each question
//        nss = new EccNOMORDStratifiedSummary("2 of 5");
//        nss.addStratifiedItem(new EccItemCount(qoeAttrONE.getName(), 3));
//        nss.addStratifiedItem(new EccItemCount(qoeAttrTWO.getName(), 2));
//        nss.addStratifiedItem(new EccItemCount(qoeAttrTHREE.getName(), 3));
//        qoeStratifiedSummaryDistribData.add(nss);
//
//        // Number of people who picked option 3 for each question
//        nss = new EccNOMORDStratifiedSummary("3 of 5");
//        nss.addStratifiedItem(new EccItemCount(qoeAttrONE.getName(), 2));
//        nss.addStratifiedItem(new EccItemCount(qoeAttrTWO.getName(), 4));
//        nss.addStratifiedItem(new EccItemCount(qoeAttrTHREE.getName(), 4));
//        qoeStratifiedSummaryDistribData.add(nss);
//
//        // Number of people who picked option 4 for each question
//        nss = new EccNOMORDStratifiedSummary("4 of 5");
//        nss.addStratifiedItem(new EccItemCount(qoeAttrONE.getName(), 4));
//        nss.addStratifiedItem(new EccItemCount(qoeAttrTWO.getName(), 3));
//        nss.addStratifiedItem(new EccItemCount(qoeAttrTHREE.getName(), 5));
//        qoeStratifiedSummaryDistribData.add(nss);
//
//        // Number of people who picked option 5 for each question
//        nss = new EccNOMORDStratifiedSummary("5 of 5");
//        nss.addStratifiedItem(new EccItemCount(qoeAttrONE.getName(), 5));
//        nss.addStratifiedItem(new EccItemCount(qoeAttrTWO.getName(), 6));
//        nss.addStratifiedItem(new EccItemCount(qoeAttrTHREE.getName(), 4));
//        qoeStratifiedSummaryDistribData.add(nss);
    }

    private Map<String, Integer> createNOMORDDistributionDataSet(String labels, Question q) {
        HashMap<String, Integer> dataSet = new HashMap<>();

        int counter = 1, numPeople;
        for (String label : labels.split(",")) {
            numPeople = questionnaire.getDistributionOfAnswersForQuestion(q).get(counter);
            dataSet.put(label, numPeople);
            counter++;
        }

        return dataSet;
    }

    private int distributionDataTotal(EccNOMORDAttributeSummary data) {
        int count = 0;

        for (EccItemCount eic : data.getValues()) {
            count += eic.getCount();
        }

        return count;
    }

    private void createActivityData() {
        linearActivities = new ArrayList<>();

        // Bob's activities
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 8:00", 10 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 8:10", 5 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 8:12", 30 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 8:25", 5 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 8:28", 35 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 8:30", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 8:32", 25 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 8:40", 5 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 8:42", 5 );
 
        // Alice's activities
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 9:50",  15 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 9:55",  12 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 10:03", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 10:15", 10 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 10:17", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 10:27", 10 );
        
        // Carols activities
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 10:32", 8 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 10:39", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 10:44", 10 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 10:50", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 10:53", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 10:55", 10 );

        // Map to participants
        participantActivities = new HashMap<>();
        
        mapActivityInstances( "Alice", 9,  14 );
        mapActivityInstances( "Bob",   0,  8 );
        mapActivityInstances( "Carol", 15, 20 );
    }

    private void createActivityInstance( ProvAct provAct, String timeStamp, int secondsOffset )
    {
        
        String actLabel = null;
        String actDesc  = null;
        
        switch ( provAct )
        {
            case eWeather: 
            {
                actLabel = "Checked weather";
                actDesc  = "Checked weather prediction";
            } break;
                
            case eTweet:
            {
                actLabel = "Checked Twitter messages";
                actDesc  = "Checked for hot tweet";
            } break;
                
            case eLiftWaiting:
            {
                actLabel = "Checked lift waiting times";
                actDesc  = "Checked lift waiting times on slope";
            } break;
        }        
        
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m");
            Date start = sdf.parse( timeStamp );
            Date end   = new Date( start.getTime() + (secondsOffset *1000) );
            
            String actIRI = "http://it-innovation.soton.ac.uk/ontologies/experimedia#activity_" +
                            UUID.randomUUID().toString();
            
            EccActivity act = new EccActivity( actLabel, actDesc, actIRI, start, end );
            
            linearActivities.add( act );
        } 
        catch (ParseException pe) 
        { logger.error("Could not create demo activity date! ", pe); }
    }
    
    private void mapActivityInstances( String partName, int actStart, int actEnd )
    {
        EccParticipant part = participantsByName.get( partName );
        
        if ( part != null )
        {
            EccParticipantActivityResultSet pais = new EccParticipantActivityResultSet( part );
            
            for ( int i = actStart; i <= actEnd; i++ )
                if ( linearActivities.size() > i )
                    pais.addActivity( linearActivities.get(i) );
            
            participantActivities.put( part.getIRI(), pais );
        }
    }
    
    private void createApplicationData() 
    {
        linearApplications   = new ArrayList<>();
        activityApplications = new HashMap<>();
        
        // Create a single application instance for each participant
        for ( String partName : participantsByName.keySet() )
        {
            EccParticipant part = participantsByName.get( partName );
            
            EccApplication app = new EccApplication( "Smart Ski Goggles App",
                                                     "Mobile application for skiers on the slope",
                                                     "http://it-innovation.soton.ac.uk/ontologies/experimedia#application " +
                                                     UUID.randomUUID().toString() );
            
            linearApplications.add( app );
            
            // Associate the participant's activities with the application
            EccParticipantActivityResultSet pais = participantActivities.get( part.getIRI() );
            if ( pais != null )
            {
                for ( EccActivity act : pais.getActivities() )
                {
                    EccActivityApplicationResultSet appSet = new EccActivityApplicationResultSet( act );
                    appSet.addApplication( app );
                    
                    activityApplications.put( act.getIRI(), appSet );
                }
            }
        }
    }

    private void createServiceData()
    {
        servicesByIRI       = new HashMap<>();
        applicationServices = new HashMap<>();
        qosAttributesByID   = new HashMap<>();
        qosAttributesByIRI  = new HashMap<>();
        qosSeries           = new HashMap<>();
        qosSummaries        = new HashMap<>();
        
        // VAS -----------------------------------------------------------------
        EccService serv           = createServiceInstance( "Video Analytics Service (VAS)", "Provides on-slope video analytics" );
        EccAttributeResultSet ars = new EccAttributeResultSet();
        
        // Average response time
        EccAttributeInfo aInfo = createQoSAttrInfo( "VAS average response time", "Time taken between query time and response being sent", "Seconds", "RATIO");
        UUID aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        EccINTRATSeries series = createQoSDataSeries( aInfo.getName(), 0.0f, 5.0f, 1.8f, 400.0f, 60, 600 );
        qosSeries.put( aID, series );
        
        // CPU Usage
        aInfo = createQoSAttrInfo( "CPU Usage", "Current CPU usage", "%", "RATIO" );
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 0.0f, 33.0f, 1.25f, 90.0f, 60, 600 );
        qosSeries.put( aID, series );
        
        // Memory usage
        aInfo = createQoSAttrInfo( "Memory Usage", "Current memory usage", "%", "RATIO" );
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 0.0f, 25.0f, 1.1f, 80.0f, 60, 600 );
        qosSeries.put( aID, series );
        
        qosAttributesByIRI.put( serv.getIRI(), ars );
        
        // Twitter -------------------------------------------------------------
        serv = createServiceInstance( "Twitter Service", "Provides twitter feeds" );
        ars  = new EccAttributeResultSet();
        
        // Twitter response time
        aInfo = createQoSAttrInfo( "Twitter query response time", "Time taken between query time and response being sent", "Seconds", "RATIO");
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 0.0f, 10.0f, 0.2f, 0.0f, 0, 600 );
        qosSeries.put( aID, series );
        
        // Twitter server load        
        aInfo = createQoSAttrInfo( "Twitter server load", "Server load", "%", "RATIO");
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 0.0f, 50.0f, 0.6f, 0.0f, 0, 600 );
        qosSeries.put( aID, series );
        
        // Twitter server memory usage        
        aInfo = createQoSAttrInfo( "Twitter server memory usage", "Current usage of available RAM", "%", "RATIO");
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 0.0f, 100.0f, 0.2f, 0.0f, 0, 600 );
        qosSeries.put( aID, series );
        
        qosAttributesByIRI.put( serv.getIRI(), ars );
        
        // Weather -------------------------------------------------------------
        serv = createServiceInstance( "Weather Service", "Provides local weather data" );
        ars = new EccAttributeResultSet();
        
        // Response time        
        aInfo = createQoSAttrInfo( "Weather service query response time", "Time taken between query time and response being sent", "Seconds", "RATIO");
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 0.0f, 5.0f, 0.3f, 0.0f, 0, 600 );
        qosSeries.put( aID, series );
        
        // Server load        
        aInfo = createQoSAttrInfo( "Weather server load", "Server load", "%", "RATIO");
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 0.0f, 50.0f, 0.2f, 0.0f, 0, 600 );
        qosSeries.put( aID, series );
        
        // Memory usage        
        aInfo = createQoSAttrInfo( "Weather server memory usage", "Current usage of available RAM", "%", "RATIO");
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 0.0f, 100.0f, 0.1f, 0.0f, 0, 600 );
        qosSeries.put( aID, series );
        
        qosAttributesByIRI.put( serv.getIRI(), ars );
        
        mapApplicationsToAllServices();
    }

    private EccService createServiceInstance( String name, String desc )
    {
        String IRI = "http://it-innovation.soton.ac.uk/ontologies/experimedia#service " + UUID.randomUUID().toString();
        
        EccService service = new EccService( name, desc, IRI );
        servicesByIRI.put( IRI, service );
        
        return service;                                            
    }
    
    private void mapApplicationsToAllServices()
    {
        for ( EccApplication app : linearApplications )
        {
            EccApplicationServiceResultSet asrs = new EccApplicationServiceResultSet(app);
            
            for ( EccService serv : servicesByIRI.values() )
                asrs.addService( serv );
            
            applicationServices.put( app.getIRI(), asrs );
        }
    }
    
    private EccAttributeInfo createQoSAttrInfo( String name, String desc,
                                                String unit, String metType )
    {
        EccAttributeInfo result = new EccAttributeInfo( name, desc, UUID.randomUUID(),
                                                        unit, metType,
                                                        "None", "None" );
        
        result.setSampleCount( 600 ); // Same number of samples for all QoS Data
        
        return result;
    }
    
    private void createParticipantActivitySummaryData()
    {
        // Run through participants summarising their activities
        participantActivitySummary = new HashMap<>();
        
        for ( String name : participantsByName.keySet() )
        {
            EccParticipant part = participantsByName.get( name );
            
            HashMap<String, Integer> actCount = new HashMap<>();
            EccParticipantActivityResultSet ars = participantActivities.get( part.getIRI() );
            
            if ( ars != null )
            {
                // Count activity instances by name
                for ( EccActivity act : ars.getActivities() )
                {
                    String actName = act.getName();
                    
                    if ( actCount.containsKey(actName) )
                    {
                        int count = actCount.get( actName );
                        actCount.put( actName, ++count );
                    }
                    else
                        actCount.put( actName, 1 );
                }
                
                // Place in summary
                if ( !actCount.isEmpty() )
                {
                    EccParticipantActivitySummaryResultSet psrs = new EccParticipantActivitySummaryResultSet( part );
                    
                    for ( String actName : actCount.keySet() )
                        psrs.addActivitySummary( new EccActivitySummaryInfo( actName, actCount.get(actName)) );
                    
                    participantActivitySummary.put( part.getIRI(), psrs );
                }
            }
        }
    }
    
    private EccINTRATSeries createQoSDataSeries( String seriesKey,
                                                 float  minValue,
                                                 float  maxValue,
                                                 float  changeRange,
                                                 float  influence,
                                                 int    influenceCount,
                                                 int    count )
    {
        ArrayList<EccMeasurement> dataSeries = new ArrayList<>();
        
        Date ts     = new Date( expStartDate.getTime() );
        float value = 0.0f;
        Random rand = new Random();
        
        for ( int i = 0; i < count; i++ )
        {
            // Create measurement and add to data set
            EccMeasurement m = new EccMeasurement();
            m.setTimestamp( ts );
            dataSeries.add( m );
            
            // Update next value
            value += ( rand.nextFloat() * changeRange ) - (rand.nextFloat() * changeRange);
            
            // Boundary
            if ( value < minValue ) value = minValue;
            else if ( value > maxValue ) value = maxValue;
            
            // Set measurement value with influence (or not)
            if ( influenceCount > 0 )
            {
                float infValue = influence + value;
                
                // Check just floor boundary with influence
                if ( infValue < minValue ) infValue = minValue;
                
                m.setValue( Float.toString(infValue) );
                --influenceCount;
            }
            else 
                m.setValue( Float.toString(value) );
            
            // Update next time stamp
            ts = new Date( ts.getTime() + 60000 );
        }
        
        return new EccINTRATSeries( seriesKey, false, dataSeries );
    }
    
    private EccINTRATSeries createHiliteSeries( String newKey, EccINTRATSeries srcSeries, ArrayList<EccActivity> actList )
    {        
        // Copy source series data
        ArrayList<EccMeasurement> targMeasures = new ArrayList<>();
        for ( EccMeasurement srcM : srcSeries.getValues() )
        {
            EccMeasurement targM = new EccMeasurement( srcM );
            targMeasures.add( targM );
        }
        
        // Run through list making null those measurements that do not match activity start or end times
        for ( EccMeasurement targM : targMeasures )
        {
            // See if it falls within activity set
            boolean makeNull = true;
            
            for ( EccActivity act : actList )
            {
                Date actStart = act.getStartTime();
                Date actEnd   = act.getEndTime();
                Date mStamp   = targM.getTimestamp();
                
                if ( (mStamp.equals(actStart) || mStamp.after(actStart)) &&
                     (mStamp.equals(actEnd)   || mStamp.before(actEnd)) )
                    makeNull = false;
            }
            
            // Nullify value if not in set
            if ( makeNull ) targM.setValue( null );
        }
        
        return new EccINTRATSeries( newKey, true, targMeasures );
    }

}
