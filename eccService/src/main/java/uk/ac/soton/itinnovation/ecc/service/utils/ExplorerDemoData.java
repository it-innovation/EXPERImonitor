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
    public EccParticipant AlicePART, BobPART, CarolPART, DavidPART, ElizabethPART, FrankPART, GemmaPART, HenryPART, ImogenPART, JuliePART;
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
        
        expSummary = new EccExperimentSummary( "Test experiment",
                                               "This experiment has been created to test the EXPERIMonitor data explorer",
                                               expID.toString(),
                                               eccParticipants.getParticipants().size(),
                                               linearActivities.size(),
                                               linearApplications.size(),
                                               servicesByIRI.size() );
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

    public EccParticipantActivityResultSet getActivitiesByParticipantAndName(String partIRI, String actLabel) {
        EccParticipantActivityResultSet all = participantActivities.get(partIRI);
        EccParticipantActivityResultSet filtered = new EccParticipantActivityResultSet(all.getParticipant());
        for (EccActivity item: all.getActivities()) {
            if (item.getName().equals(actLabel)) {
                filtered.addActivity(item);
            }
        }
        return filtered;
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
                String seriesName = pars.getParticipant().getName() + "'s";
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

        DavidPART = new EccParticipant("David", "David is a test participant",
                UUID.fromString("d3971f0e-3b77-4049-b840-77859219240e"),
                "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_d3971f0e-3b77-4049-b840-77859219240e");

        participantsByName.put( "David", DavidPART );
        eccParticipants.addParticipant(DavidPART);

        ElizabethPART = new EccParticipant("Elizabeth", "Elizabeth is a test participant",
                UUID.fromString("c91a2ef5-6653-46e6-bc93-8aaad31260d2"),
                "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_c91a2ef5-6653-46e6-bc93-8aaad31260d2");

        participantsByName.put( "Elizabeth", ElizabethPART );
        eccParticipants.addParticipant(ElizabethPART);

        FrankPART = new EccParticipant("Frank", "Frank is a test participant",
                UUID.fromString("28ae77c8-10a1-4c10-b8e0-8557bec8b646"),
                "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_28ae77c8-10a1-4c10-b8e0-8557bec8b646");

        participantsByName.put( "Frank", FrankPART );
        eccParticipants.addParticipant(FrankPART);

        GemmaPART = new EccParticipant("Gemma", "Gemma is a test participant",
                UUID.fromString("262533b1-e90e-4de4-89cd-53f5cd5839f6"),
                "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_262533b1-e90e-4de4-89cd-53f5cd5839f6");

        participantsByName.put( "Gemma", GemmaPART );
        eccParticipants.addParticipant(GemmaPART);

        HenryPART = new EccParticipant("Henry", "Henry is a test participant",
                UUID.fromString("8df2eb3e-cdc3-47c7-8bfd-da6f60796e3a"),
                "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_8df2eb3e-cdc3-47c7-8bfd-da6f60796e3a");

        participantsByName.put( "Henry", HenryPART );
        eccParticipants.addParticipant(HenryPART);

        ImogenPART = new EccParticipant("Imogen", "Imogen is a test participant",
                UUID.fromString("e77dc128-eca4-4275-a359-aa213c823705"),
                "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_e77dc128-eca4-4275-a359-aa213c823705");

        participantsByName.put( "Imogen", ImogenPART );
        eccParticipants.addParticipant(ImogenPART);

        JuliePART = new EccParticipant("Julie", "Julie is a test participant",
                UUID.fromString("6492f4fb-9520-4cd6-8656-78d5e159196a"),
                "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_6492f4fb-9520-4cd6-8656-78d5e159196a");

        participantsByName.put( "Julie", JuliePART );
        eccParticipants.addParticipant(JuliePART);
    }

    private void createPARTAttributeInfo() {
        qoeAttrONE = new EccAttributeInfo("Ease of use",
                "Questionnaire: item 1",
                UUID.fromString("eaad2d89-d543-47b2-9485-5b1d7ba6cf29"),
                "scale item",
                "ORDINAL",
                "Likert scale",
                "very difficult, difficult, not easy/difficult, easy, very easy");

        qoeAttrTWO = new EccAttributeInfo("Usefulness",
                "Questionnaire: item 2",
                UUID.fromString("e7f7a75e-4a41-421f-89bf-20bb4bab57ab"),
                "scale item",
                "ORDINAL",
                "Likert scale",
                "not at all useful, not very useful, sometimes useful, often useful, always useful");

        qoeAttrTHREE = new EccAttributeInfo("Responsiveness",
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
        User david = new User(DavidPART.getMetricEntityID(), DavidPART.getName());
        questionnaire.addUser(david);
        usersParticipants.put(david.getId(), DavidPART.getMetricEntityID());
        User elizabeth = new User(ElizabethPART.getMetricEntityID(), ElizabethPART.getName());
        questionnaire.addUser(elizabeth);
        usersParticipants.put(elizabeth.getId(), ElizabethPART.getMetricEntityID());
        User frank = new User(FrankPART.getMetricEntityID(), FrankPART.getName());
        questionnaire.addUser(frank);
        usersParticipants.put(frank.getId(), FrankPART.getMetricEntityID());
        User gemma = new User(GemmaPART.getMetricEntityID(), GemmaPART.getName());
        questionnaire.addUser(gemma);
        usersParticipants.put(gemma.getId(), GemmaPART.getMetricEntityID());
        User henry = new User(HenryPART.getMetricEntityID(), HenryPART.getName());
        questionnaire.addUser(henry);
        usersParticipants.put(henry.getId(), HenryPART.getMetricEntityID());
        User imogen = new User(ImogenPART.getMetricEntityID(), ImogenPART.getName());
        questionnaire.addUser(imogen);
        usersParticipants.put(imogen.getId(), ImogenPART.getMetricEntityID());
        User julie = new User(JuliePART.getMetricEntityID(), JuliePART.getName());
        questionnaire.addUser(julie);
        usersParticipants.put(julie.getId(), JuliePART.getMetricEntityID());

        // Create summary for each participant
        qoeParticipantSummaryData = new HashMap<>();

        // Alice
        int[] answers = new int[]{4, 4, 3};
        EccNOMORDParticipantSummary ps = new EccNOMORDParticipantSummary(AlicePART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(alice, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(alice, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(alice, q3, answers[2]));
        qoeParticipantSummaryData.put(AlicePART.getIRI(), ps);

        // Bob
        answers = new int[]{3, 2, 1};
        ps = new EccNOMORDParticipantSummary(BobPART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(bob, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(bob, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(bob, q3, answers[2]));
        qoeParticipantSummaryData.put(BobPART.getIRI(), ps);

        // Carol
        answers = new int[]{3, 5, 4};
        ps = new EccNOMORDParticipantSummary(CarolPART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(carol, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(carol, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(carol, q3, answers[2]));
        qoeParticipantSummaryData.put(CarolPART.getIRI(), ps);

        answers = new int[]{4, 5, 5};
        ps = new EccNOMORDParticipantSummary(DavidPART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(david, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(david, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(david, q3, answers[2]));
        qoeParticipantSummaryData.put(DavidPART.getIRI(), ps);

        answers = new int[]{5, 5, 4};
        ps = new EccNOMORDParticipantSummary(ElizabethPART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(elizabeth, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(elizabeth, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(elizabeth, q3, answers[2]));
        qoeParticipantSummaryData.put(ElizabethPART.getIRI(), ps);

        answers = new int[]{3, 4, 5};
        ps = new EccNOMORDParticipantSummary(FrankPART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(frank, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(frank, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(frank, q3, answers[2]));
        qoeParticipantSummaryData.put(FrankPART.getIRI(), ps);

        answers = new int[]{4, 3, 3};
        ps = new EccNOMORDParticipantSummary(GemmaPART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(gemma, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(gemma, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(gemma, q3, answers[2]));
        qoeParticipantSummaryData.put(GemmaPART.getIRI(), ps);

        answers = new int[]{5, 5, 5};
        ps = new EccNOMORDParticipantSummary(HenryPART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(henry, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(henry, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(henry, q3, answers[2]));
        qoeParticipantSummaryData.put(HenryPART.getIRI(), ps);

        answers = new int[]{5, 5, 5};
        ps = new EccNOMORDParticipantSummary(ImogenPART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(imogen, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(imogen, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(imogen, q3, answers[2]));
        qoeParticipantSummaryData.put(ImogenPART.getIRI(), ps);

        answers = new int[]{4, 3, 4};
        ps = new EccNOMORDParticipantSummary(JuliePART);
        ps.addORDINALResponse(qoeAttrONE.getName(), getStringForIndexStartingAtOne(qoeAttrONE.getMetaContent(), answers[0]), answers[0]);
        questionnaire.addAnswer(new Answer(julie, q1, answers[0]));
        ps.addORDINALResponse(qoeAttrTWO.getName(), getStringForIndexStartingAtOne(qoeAttrTWO.getMetaContent(), answers[1]), answers[1]);
        questionnaire.addAnswer(new Answer(julie, q2, answers[1]));
        ps.addORDINALResponse(qoeAttrTHREE.getName(), getStringForIndexStartingAtOne(qoeAttrTHREE.getMetaContent(), answers[2]), answers[2]);
        questionnaire.addAnswer(new Answer(julie, q3, answers[2]));
        qoeParticipantSummaryData.put(JuliePART.getIRI(), ps);

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
            
            String labelValue = extractLabelFromIndex( i-1, qoeAttrONE.getMetaContent() );
            nss.addStratifiedItem(new EccItemCount(qoeAttrONE.getName(), questionnaire.getDistributionOfAnswersForOptionAndQuestion(i, q1), labelValue));
            
            labelValue = extractLabelFromIndex( i-1, qoeAttrTWO.getMetaContent() );
            nss.addStratifiedItem(new EccItemCount(qoeAttrTWO.getName(), questionnaire.getDistributionOfAnswersForOptionAndQuestion(i, q2), labelValue));
            
            labelValue = extractLabelFromIndex( i-1, qoeAttrTHREE.getMetaContent() );
            nss.addStratifiedItem(new EccItemCount(qoeAttrTHREE.getName(), questionnaire.getDistributionOfAnswersForOptionAndQuestion(i, q3), labelValue));
            
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
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 8:14", 30 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 9:00", 5 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 9:06", 35 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 9:11", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 9:48", 25 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 9:44", 5 );

        // Alice's activities
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 9:00",  15 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 9:17",  30 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 9:18", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 9:54", 30 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 10:17", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 10:27", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 11:04", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 11:45", 10 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 12:22",  15 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 12:24", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 14:19", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 14:59", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 15:36", 15 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 16:14", 10 );
        
        // Carol's activities
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 09:12", 8 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 09:13", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 09:17", 10 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 10:11", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 10:15", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 10:50", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 11:55", 20 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 11:57", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 12:48", 14 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 13:57", 20 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 13:59", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 14:50", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 15:49", 12 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 16:48", 22 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 16:50", 8 );

        // David's activities
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 10:12", 8 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 10:13", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 10:17", 10 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 11:11", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 11:15", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 11:50", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 12:55", 20 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 12:57", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 13:48", 14 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 14:57", 20 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 14:59", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 15:50", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 16:49", 12 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 17:48", 22 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 17:50", 8 );

        // Elizabeth's activities
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 10:22", 8 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 10:23", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 10:27", 10 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 11:21", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 11:25", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 11:50", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 12:55", 20 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 12:57", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 13:48", 14 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 14:47", 20 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 14:49", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 15:40", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 16:39", 12 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 17:38", 22 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 17:40", 8 );

        // Frank's activities
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 11:22", 8 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 11:23", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 11:27", 10 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 12:21", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 12:25", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 12:50", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 13:55", 20 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 13:57", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 14:48", 14 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 15:47", 20 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 15:49", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 16:40", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 17:39", 12 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 18:38", 22 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 18:40", 8 );

        // Gemma's activities
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 11:12", 8 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 11:13", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 11:17", 10 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 12:11", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 12:15", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 15:37", 20 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 15:39", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 16:30", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 17:29", 12 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 18:28", 22 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 18:30", 8 );

        // Henry's activities
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 11:13", 8 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 11:15", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 11:11", 10 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 12:13", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 15:37", 20 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 15:44", 8 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 16:32", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 17:22", 12 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 18:22", 22 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 18:30", 8 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 18:55", 7 );

        // Imogen's activities
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 10:22", 8 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 10:24", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 10:25", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 17:21", 22 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 18:35", 8 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 18:58", 7 );

        // Julie's activities
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 11:22", 8 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 11:24", 7 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 11:25", 10 );
        createActivityInstance( ProvAct.eLiftWaiting, "2014-07-31 16:21", 22 );
        createActivityInstance( ProvAct.eTweet,       "2014-07-31 17:35", 8 );
        createActivityInstance( ProvAct.eWeather,     "2014-07-31 17:58", 7 );

        // Map to participants
        participantActivities = new HashMap<>();
        
        mapActivityInstances( "Bob",       0,  7  );
        mapActivityInstances( "Alice",     8,  21 );
        mapActivityInstances( "Carol",     22, 36 );
        mapActivityInstances( "David",     37, 51 );
        mapActivityInstances( "Elizabeth", 52, 66 );
        mapActivityInstances( "Frank",     67, 81 );
        mapActivityInstances( "Gemma",     82, 92 );
        mapActivityInstances( "Henry",     93, 103 );
        mapActivityInstances( "Imogen",    104, 109 );
        mapActivityInstances( "Julie",     110, 115 );
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
        EccAttributeInfo aInfo = createQoSAttrInfo( "Average response time", "Time taken between query time and response being sent", "Seconds", "RATIO");
        UUID aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        EccINTRATSeries series = createQoSDataSeries( aInfo.getName(), 1.0f, 5.0f, 1.8f, 400.0f, 180, 840 );
        qosSeries.put( aID, series );
        
        // CPU Usage
        aInfo = createQoSAttrInfo( "CPU Usage", "Current CPU usage", "%", "RATIO" );
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 12.0f, 20.0f, 1.25f, 80.0f, 180, 840 );
        qosSeries.put( aID, series );
        
        // Memory usage
        aInfo = createQoSAttrInfo( "Memory Usage", "Current memory usage", "%", "RATIO" );
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 30.0f, 39.0f, 1.1f, 60.0f, 180, 840 );
        qosSeries.put( aID, series );
        
        qosAttributesByIRI.put( serv.getIRI(), ars );
        
        // Twitter -------------------------------------------------------------
        serv = createServiceInstance( "Hot Tweet Service", "Provides twitter feeds" );
        ars  = new EccAttributeResultSet();
        
        // Twitter response time
        aInfo = createQoSAttrInfo( "Average response time", "Time taken between query time and response being sent", "Seconds", "RATIO");
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 1.0f, 10.0f, 0.2f, 0.0f, 0, 840 );
        qosSeries.put( aID, series );
        
        // Twitter server load        
        aInfo = createQoSAttrInfo( "CPU Usage", "Current CPU usage", "%", "RATIO");
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 10.0f, 50.0f, 0.6f, 0.0f, 0, 840 );
        qosSeries.put( aID, series );
        
        // Twitter server memory usage        
        aInfo = createQoSAttrInfo( "Memory Usage", "Current memory usage", "%", "RATIO");
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 15.0f, 100.0f, 0.2f, 0.0f, 0, 840 );
        qosSeries.put( aID, series );
        
        qosAttributesByIRI.put( serv.getIRI(), ars );
        
        // Weather -------------------------------------------------------------
        serv = createServiceInstance( "Weather Service", "Provides local weather data" );
        ars = new EccAttributeResultSet();
        
        // Response time        
        aInfo = createQoSAttrInfo( "Average response time", "Time taken between query time and response being sent", "Seconds", "RATIO");
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 1.0f, 5.0f, 0.3f, 0.0f, 0, 840 );
        qosSeries.put( aID, series );
        
        // Server load        
        aInfo = createQoSAttrInfo( "CPU Usage", "Current CPU usage", "%", "RATIO");
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 10.0f, 50.0f, 0.2f, 0.0f, 0, 840 );
        qosSeries.put( aID, series );
        
        // Memory usage        
        aInfo = createQoSAttrInfo( "Memory Usage", "Current memory usage", "%", "RATIO");
        aID = UUID.fromString( aInfo.getMetricID() );
        qosAttributesByID.put( aID, aInfo );
        ars.addAttributeInfo( aInfo );
        series = createQoSDataSeries( aInfo.getName(), 20.0f, 100.0f, 0.1f, 0.0f, 0, 840 );
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
        
        // Run through list making null those measurements that match within time slices
        for ( int i = 0; i < targMeasures.size() -1; ++i )
        {          
            Date m1Start = targMeasures.get( i ).getTimestamp();
            Date m2Start = targMeasures.get( i +1 ).getTimestamp();
            
            // See if it falls within activity set
            boolean makeNull = true;
            
            for ( EccActivity act : actList )
            {
                Date actStamp = act.getStartTime();
                
                // If measurement data within an activity range
                if ( (actStamp.equals(m1Start) || actStamp.after(m1Start)) && actStamp.before(m2Start) )
                    makeNull = false;
            }
            
            // Make measurement null if not in activity ranges
            if ( makeNull ) targMeasures.get( i ).setValue( null );
        }
        
        // Always make last measurement null
        targMeasures.get( targMeasures.size() - 1 ).setValue( null );
        
        return new EccINTRATSeries( newKey, true, targMeasures );
    }

    private String extractLabelFromIndex( int i, String metaContent )
    {
        String[] items = metaContent.split( "," );
        
        if ( items.length > i )
            return items[i].trim();
        else
            return "Unknown";
    }
}
