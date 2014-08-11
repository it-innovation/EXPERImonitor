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
import org.slf4j.*;




/**
 * This class is only to be used during the development of the explorer service 
 * and should be removed once service is complete.
 */
public class ExplorerDemoData
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    // All attributes are public - this is a demo support class only, so we don't care
    public UUID expID = UUID.fromString("c91c05ed-c6ba-4880-82af-79eb5d4a58cd");
    public Date expStartDate;
    public EccExperimentSummary expSummary;
    
    public EccParticipantResultSet eccParticipants;
    public EccParticipant AlicePART, BobPART, CarolPART;
    
    public EccAttributeInfo                             qoeAttrONE;
    public EccAttributeInfo                             qoeAttrTWO;
    public EccAttributeInfo                             qoeAttrTHREE;
    public EccParticipantAttributeResultSet             partAttrInfoSet;
    public HashMap<String, EccNOMORDAttributeSummary>   qoeSummaryDistribData;
    public HashMap<String, EccNOMORDParticipantSummary> qoeParticipantSummaryData;
    public ArrayList<EccNOMORDStratifiedSummary>        qoeStratifiedSummaryDistribData;
    
    public ArrayList<EccActivity>    linearActivities;
    public ArrayList<EccApplication> linearApplications;
    public ArrayList<EccService>     linearServices;
    
    public HashMap<String, EccParticipantActivitySummaryResultSet> participantActivitySummary;
    public HashMap<String, EccParticipantActivityResultSet>        participantActivities;
    public HashMap<String, EccActivityApplicationResultSet>        activityApplications;
    public HashMap<String, EccApplicationServiceResultSet>         applicationServices;
    
    public HashMap<String, EccAttributeResultSet> serviceQoSAttributes;
    public HashMap<Long,   EccINTRATSummary>      qosDiscreteDistributionData;
    public ArrayList<EccMeasurement>              qosSeriesDemo;
    public HashMap<String, EccINTRATSeries>       qosSeriesHighlights;
    
    
    public ExplorerDemoData()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m");
        
        try
        {
            expStartDate = sdf.parse( "2014-07-31 10:00" );
        }
        catch ( ParseException pe )
        { logger.error( "Could not create demo experiment date! ", pe ); }
        
        expSummary = new EccExperimentSummary( "Test experiment",
                                               "This experiment has been created to test the EXPERIMonitor data explorer",
                                               expID.toString(),
                                               3,    // Alice, Bob, Carol
                                               10,   // 10 activities
                                               3,    // 3 applications
                                               1 );  // 1 service
        createParticipants();
        createPARTAttributeInfo();
        createNOMORDDistributionData();
        createActivityData();
        createApplicationData();
        createServiceData();
        createParticipantSummaryData();
    }
    
    public EccParticipant getParticipant( String IRI )
    {
        EccParticipant result = null;
        
        ArrayList<EccParticipant> parts = eccParticipants.getParticipants();
        for ( EccParticipant part : parts )
        {
            if ( part.getIRI().equals(IRI) )
            {
                result = part;
                break;
            }
        }
        
        return result;
    }
    
    public EccParticipantResultSet getParticipantsByAttributeScaleLabel( String attrName,
                                                                         String nomOrdLabel  )
    {
        EccParticipantResultSet partInfoSet = new EccParticipantResultSet();
        
        // Just return Bob for now
        partInfoSet.addParticipant( BobPART );
        
        return partInfoSet;
    }
    
    public EccParticipantActivitySummaryResultSet getPROVSummaryByParticipant( String partIRI )
    {
        return participantActivitySummary.get( partIRI );
    }
    
    public EccParticipantActivityResultSet getActivitiesByParticipant( String partIRI )
    {
        return participantActivities.get( partIRI );
    }
    
    public EccActivityApplicationResultSet getApplicationsByActivity( String actIRI )
    {
        return activityApplications.get( actIRI );
    }
    
    public EccApplicationServiceResultSet getServicesByApplication( String appIRI )
    {
        return applicationServices.get( appIRI );
    }
    
    public EccINTRATSummary getINTRATDistDataDiscrete( UUID attrID, ArrayList<Long> stamps )
    {
        // Cheating here: just use first time stamp to return made-up distribution
        return qosDiscreteDistributionData.get( stamps.get(0) );
    }
    
    public EccINTRATSeriesSet getINTRATSeriesHighlightActivities( UUID seriesAttrID,
                                                                  String partIRI,
                                                                  String activityLabel )
    {
        // Just return some highlighted time stamps
        EccINTRATSeriesSet result = new EccINTRATSeriesSet();
        
        // Get QoS series first
        EccINTRATSeries series = new EccINTRATSeries( "VAS response time", false,
                                                      qosSeriesDemo );
        result.addSeries( series );
        
        // Then add highlight
        result.addSeries( qosSeriesHighlights.get(partIRI) );
        
        return result;
    }
    
    // Private methods ---------------------------------------------------------
    private void createParticipants()
    {
        eccParticipants = new EccParticipantResultSet();
        
        AlicePART = new EccParticipant( "Alice", "Alice is a test participant",
                                        UUID.fromString("02bcc340-3254-4eee-b9dc-5132c2e25cbf"),
                                        "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_02bcc340-3254-4eee-b9dc-5132c2e25cbf" );
        
        BobPART = new EccParticipant( "Bob", "Bob is a test participant",
                                      UUID.fromString("74e3e2aa-0d86-49c9-8336-a44a1482a887"),
                                      "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_74e3e2aa-0d86-49c9-8336-a44a1482a887" );
        
        CarolPART = new EccParticipant( "Carol", "Carol is a test participant",
                                        UUID.fromString("81ace737-818e-42e3-b7c3-d2c1fa1d7a0c"),
                                        "http://it-innovation.soton.ac.uk/ontologies/experimedia#participant_81ace737-818e-42e3-b7c3-d2c1fa1d7a0c" );
        
        eccParticipants.addParticipant( AlicePART );
        eccParticipants.addParticipant( BobPART );
        eccParticipants.addParticipant( CarolPART );
    }
    
    private void createPARTAttributeInfo()
    {
        qoeAttrONE = new EccAttributeInfo( "Ease of use: Ski lift app",
                                           "Questionnaire: item 1",
                                           UUID.fromString("eaad2d89-d543-47b2-9485-5b1d7ba6cf29"),
                                           "scale item",
                                           "ORDINAL",
                                           "Likert scale",
                                           "Very difficult, difficult, not easy/difficult, easy, very easy" );
        
        qoeAttrTWO = new EccAttributeInfo( "Usefulness: Ski lift app",
                                           "Questionnaire: item 2",
                                           UUID.fromString("e7f7a75e-4a41-421f-89bf-20bb4bab57ab"),
                                           "scale item",
                                           "ORDINAL",
                                           "Likert scale",
                                           "Not at all useful, not very useful, sometimes useful, often useful, always useful" );
        
        qoeAttrTHREE = new EccAttributeInfo( "Responsiveness: Ski lift app",
                                             "Questionnaire: item 3",
                                             UUID.fromString("e0069254-3875-44e6-8670-4c477057c578"),
                                             "scale item",
                                             "ORDINAL",
                                             "Likert scale",
                                             "Very unresponsive, not very responsive, moderately responsive, quite responsive, very responsive" );
        
        partAttrInfoSet = new EccParticipantAttributeResultSet();
        partAttrInfoSet.addQoEAttribute( qoeAttrONE );
        partAttrInfoSet.addQoEAttribute( qoeAttrTWO );
        partAttrInfoSet.addQoEAttribute( qoeAttrTHREE );
    }
    
    private void createNOMORDDistributionData()
    {
        qoeSummaryDistribData = new HashMap<>();
        
        // Question 1
        EccNOMORDAttributeSummary data = new EccNOMORDAttributeSummary( qoeAttrONE,
                                                      createNOMORDDistributionDataSet( qoeAttrONE.getMetaContent() ) );
        
        qoeSummaryDistribData.put( qoeAttrONE.getName(), data );
        qoeAttrONE.setSampleCount( distributionDataTotal(data) );
        
        // Question 2
        data = new EccNOMORDAttributeSummary( qoeAttrTWO, createNOMORDDistributionDataSet( qoeAttrTWO.getMetaContent() ) );
        qoeSummaryDistribData.put( qoeAttrTWO.getName(), data );
        qoeAttrTWO.setSampleCount( distributionDataTotal(data) );
        
        // Question 3
        data = new EccNOMORDAttributeSummary( qoeAttrTHREE, createNOMORDDistributionDataSet( qoeAttrTHREE.getMetaContent() ) );
        qoeSummaryDistribData.put( qoeAttrTHREE.getName(), data );
        qoeAttrTHREE.setSampleCount( distributionDataTotal(data) );
        
        // Create summary for each participant
        qoeParticipantSummaryData = new HashMap<>();
        
        // Alice
        EccNOMORDParticipantSummary ps = new EccNOMORDParticipantSummary( AlicePART );
        ps.addORDINALResponse( qoeAttrONE.getName(), "not easy/difficult", 3 );
        ps.addORDINALResponse( qoeAttrTWO.getName(), "sometimes useful", 3 );
        ps.addORDINALResponse( qoeAttrTHREE.getName(), "moderately responsive", 3 );
        qoeParticipantSummaryData.put( AlicePART.getIRI() , ps );        
                
        // Bob
        ps = new EccNOMORDParticipantSummary( BobPART );
        ps.addORDINALResponse( qoeAttrONE.getName(), "Very difficult", 1 );
        ps.addORDINALResponse( qoeAttrTWO.getName(), "Not at all useful", 1 );
        ps.addORDINALResponse( qoeAttrTHREE.getName(), "Very unresponsive", 1 );
        qoeParticipantSummaryData.put( BobPART.getIRI() , ps );             
                
        // Carol
        ps = new EccNOMORDParticipantSummary( CarolPART );
        ps.addORDINALResponse( qoeAttrONE.getName(), "easy", 4 );
        ps.addORDINALResponse( qoeAttrTWO.getName(), "always useful", 5 );
        ps.addORDINALResponse( qoeAttrTHREE.getName(), "very responsive", 5 );
        qoeParticipantSummaryData.put( CarolPART.getIRI() , ps );
                
        // Stratified summary data (simply mock up this data)
        qoeStratifiedSummaryDistribData = new ArrayList<>();
        
        // 1 of 5
        EccNOMORDStratifiedSummary nss = new EccNOMORDStratifiedSummary( "1 of 5" );
        nss.addStratifiedItem( new EccItemCount( qoeAttrONE.getName(), 1 ) );
        nss.addStratifiedItem( new EccItemCount( qoeAttrTWO.getName(), 2 ) );
        nss.addStratifiedItem( new EccItemCount( qoeAttrTHREE.getName(), 3 ) );
        qoeStratifiedSummaryDistribData.add( nss );
        
        // 2 of 5
        nss = new EccNOMORDStratifiedSummary( "2 of 5" );
        nss.addStratifiedItem( new EccItemCount( qoeAttrONE.getName(), 3 ) );
        nss.addStratifiedItem( new EccItemCount( qoeAttrTWO.getName(), 2 ) );
        nss.addStratifiedItem( new EccItemCount( qoeAttrTHREE.getName(), 3 ) );
        qoeStratifiedSummaryDistribData.add( nss );
        
        // 3 of 5
        nss = new EccNOMORDStratifiedSummary( "3 of 5" );
        nss.addStratifiedItem( new EccItemCount( qoeAttrONE.getName(), 2 ) );
        nss.addStratifiedItem( new EccItemCount( qoeAttrTWO.getName(), 4 ) );
        nss.addStratifiedItem( new EccItemCount( qoeAttrTHREE.getName(), 4 ) );
        qoeStratifiedSummaryDistribData.add( nss );
        
        // 4 of 5
        nss = new EccNOMORDStratifiedSummary( "4 of 5" );
        nss.addStratifiedItem( new EccItemCount( qoeAttrONE.getName(), 4 ) );
        nss.addStratifiedItem( new EccItemCount( qoeAttrTWO.getName(), 3 ) );
        nss.addStratifiedItem( new EccItemCount( qoeAttrTHREE.getName(), 5 ) );
        qoeStratifiedSummaryDistribData.add( nss );
        
        // 5 of 5
        nss = new EccNOMORDStratifiedSummary( "5 of 5" );
        nss.addStratifiedItem( new EccItemCount( qoeAttrONE.getName(), 5 ) );
        nss.addStratifiedItem( new EccItemCount( qoeAttrTWO.getName(), 6 ) );
        nss.addStratifiedItem( new EccItemCount( qoeAttrTHREE.getName(), 4 ) );
        qoeStratifiedSummaryDistribData.add( nss );
    }
    
    private Map<String, Integer> createNOMORDDistributionDataSet( String labels )
    {
        HashMap<String, Integer> dataSet = new HashMap<>();
        
        String[] labelItems = labels.split( "," );
        Random rand = new Random();
        
        for ( String label : labelItems )
            dataSet.put( label, rand.nextInt(labelItems.length) );
        
        // Fix the first item to always equal 1 (for demo purposes only)
        dataSet.remove( labelItems[0] );
        dataSet.put( labelItems[0], 1 );
        
        return dataSet;
    }
    
    private int distributionDataTotal( EccNOMORDAttributeSummary data )
    {
        int count = 0;
        
        for ( EccItemCount eic : data.getValues() )
            count += eic.getCount();
        
        return count;
    }
    
    private void createActivityData()
    {
        linearActivities = new ArrayList<>();
        
        
        // Activity 1 (1 minute)
        EccActivity act = new EccActivity( "Used lift application",
                                           "Checked lift waiting time",
                                           "http://it-innovation.soton.ac.uk/ontologies/experimedia#activity_d5b1ba72-f0e4-45f2-a996-fff97cdc2de2",
                                           new Date( expStartDate.getTime()),
                                           new Date( expStartDate.getTime() + 60000) );
        linearActivities.add( act );
        
        // Activity 2 (5 minutes)
        act = new EccActivity( "Used lift application",
                               "Checked lift waiting time",
                               "http://it-innovation.soton.ac.uk/ontologies/experimedia#activity_c108742d-d41d-40ee-b532-7f8fd6508baf",
                               new Date( expStartDate.getTime() +600000),
                               new Date( expStartDate.getTime() +900000) );
        linearActivities.add( act );
        
        // Activity 3 (5 minutes)
        act = new EccActivity( "Used lift application",
                               "Checked lift waiting time",
                               "http://it-innovation.soton.ac.uk/ontologies/experimedia#activity_0f9d7667-1276-4a20-9278-a355ccb6e467",
                               new Date( expStartDate.getTime() +960000),
                               new Date( expStartDate.getTime() +1260000) );
        linearActivities.add( act );
        
        // Activity 4 (5 minutes)
        act = new EccActivity( "Used lift application",
                               "Checked lift waiting time",
                               "http://it-innovation.soton.ac.uk/ontologies/experimedia#activity_aee642e6-ade7-4b44-870d-bb70ff5c02f1",
                               new Date( expStartDate.getTime() +1320000),
                               new Date( expStartDate.getTime() +1620000) );
        linearActivities.add( act );
        
        // Activity 5 (1 minute)
        act = new EccActivity( "Used lift application",
                               "Checked lift waiting time",
                               "http://it-innovation.soton.ac.uk/ontologies/experimedia#activity_98626f59-47d0-467c-b43b-a0ae253ec193",
                               new Date( expStartDate.getTime() +3000000),
                               new Date( expStartDate.getTime() +3060000) );
        linearActivities.add( act );
        
        // Activity 6 (1 minute)
        act = new EccActivity( "Used lift application",
                               "Checked lift waiting time",
                               "http://it-innovation.soton.ac.uk/ontologies/experimedia#activity_98626f59-47d0-467c-b43b-a0ae253ec193",
                               new Date( expStartDate.getTime() +3120000),
                               new Date( expStartDate.getTime() +3180000) );
        linearActivities.add( act );
        
        // Map to participants
        participantActivities = new HashMap<>();
        
        // Alice's activities (1)
        EccParticipantActivityResultSet pais = new EccParticipantActivityResultSet( AlicePART );
        pais.addActivity( linearActivities.get(0) );
        participantActivities.put( AlicePART.getIRI(), pais );
            
        // Bob's activities (3)
        pais = new EccParticipantActivityResultSet( BobPART );
        pais.addActivity( linearActivities.get(1) );
        pais.addActivity( linearActivities.get(2) );
        pais.addActivity( linearActivities.get(3) );
        participantActivities.put( BobPART.getIRI(), pais );
        
        // Carol's activities (1)
        pais = new EccParticipantActivityResultSet( CarolPART );
        pais.addActivity( linearActivities.get(4) );
        pais.addActivity( linearActivities.get(5) );
        participantActivities.put( CarolPART.getIRI(), pais );
    }
    
    private void createApplicationData()
    {
        linearApplications   = new ArrayList<>();
        activityApplications = new HashMap<>();
        
        // 3 x different application instancess
        EccApplication app = new EccApplication( "Ski Lift Waiting App",
                                                 "Mobile application used to predict waiting time",
                                                 "http://it-innovation.soton.ac.uk/ontologies/experimedia#application_14e3e008-95bb-4423-a21f-c216c614d591" );    
        linearApplications.add( app );
        
        app = new EccApplication( "Ski Lift Waiting App",
                                  "Mobile application used to predict waiting time",
                                  "http://it-innovation.soton.ac.uk/ontologies/experimedia#application_3af4c091-2019-4f1c-a867-89c44970a509" );    
        linearApplications.add( app );
        
        app = new EccApplication( "Ski Lift Waiting App",
                                  "Mobile application used to predict waiting time",
                                  "http://it-innovation.soton.ac.uk/ontologies/experimedia#application_7ee60153-0ba0-4765-b44c-bb1137533d16" );    
        linearApplications.add( app );
        
        // Associate one application per participant's activities
        for ( EccParticipantActivityResultSet ais : participantActivities.values() )
        {
            EccParticipant part = ais.getParticipant();
            
            int appIndex = 0;
            
            for ( EccActivity act : ais.getActivities() )
            {
                EccActivityApplicationResultSet aais = new EccActivityApplicationResultSet( act );
                aais.addApplication( linearApplications.get(appIndex) );
                appIndex++;
                
                activityApplications.put( act.getIRI(), aais );
            }
        }
    }
    
    private void createServiceData()
    {
        linearServices      = new ArrayList<>();
        applicationServices = new HashMap<>();
        
        // Create a single service for all the applications
        EccService service = new EccService( "VAS Component (Service)",
                                             "VAS provides video analytics",
                                             "http://it-innovation.soton.ac.uk/ontologies/experimedia#service_14e3e008-95bb-4423-a21f-c216c614d591" );
        linearServices.add( service );
        
        // Associate each application with the service
        for ( EccApplication app : linearApplications )
        {
            EccApplicationServiceResultSet asrs = new EccApplicationServiceResultSet( app );
            asrs.addService( service );
            
            applicationServices.put( app.getIRI(), asrs );
        }
        
        // Create QoS attribue for this service
        serviceQoSAttributes = new HashMap<>();
        
        EccAttributeInfo info = new EccAttributeInfo( "VAS response time",
                                                      "Time taken between query and response being sent",
                                                      UUID.fromString("e13ac84e-3a09-45f8-8287-a48a8f3f9d73"),
                                                      "Seconds",
                                                      "RATIO",
                                                      "None",
                                                      "None" );
        
        info.setSampleCount( 600 ); // 6 x 100 samples
        
        EccAttributeResultSet ars = new EccAttributeResultSet();
        ars.addAttributeInfo( info );
        
        serviceQoSAttributes.put( service.getIRI(), ars );
        
        // Create summary data for service
        qosDiscreteDistributionData = new HashMap<>();
        
        // Create generic QoS distribution data for each activity frame
        Random rand = new Random();
        int index = 0;
        for ( EccActivity act : linearActivities )
        {
            float floor = rand.nextFloat() * 3.0f;
            float ceil  = rand.nextFloat() * 10.0f;
            float avg   = rand.nextFloat() + 5.0f;
            
            // Artificially inflate QoS for Bob
            if ( index > 0 || index < 4)
            {
                floor += 300.0f;
                ceil  += 305.0f;
                avg   += 302.0f;
            }
            
            EccINTRATSummary dd = new EccINTRATSummary( info, floor, ceil, avg );
            
            qosDiscreteDistributionData.put( act.getStartTime().getTime(), dd );
            ++index;
        }
        
        // Create dummy QoS data (we won't bother artificially raising this data)
        qosSeriesDemo = new ArrayList<>();
        Date ts = new Date( expStartDate.getTime() );
        float value = 10.0f;
        
        for ( int i = 0; i < 600; ++i )
        {
            EccMeasurement m = new EccMeasurement();
            m.setTimestamp( ts );
            m.setValue( Float.toString(value) );
            
            qosSeriesDemo.add( m );
            
            // Update time & value
            ts = new Date ( ts.getTime() + 60000 );
            value += ( rand.nextFloat() * 10.0f );
            value -= ( rand.nextFloat() * 10.0f );
            
            if ( value < 1.0f ) value = 1.0f;
                else if ( value > 400.0f ) value = 400.0f;
        }
        
        // Create dummy series highlights based on activities
        qosSeriesHighlights = new HashMap<>();
  
        qosSeriesHighlights.put( AlicePART.getIRI(), 
                                 createHiliteSeries("Alice's activities", 150, 160) );
        
        qosSeriesHighlights.put( BobPART.getIRI(), 
                                 createHiliteSeries("Bob's activities", 10, 100) );
        
        qosSeriesHighlights.put( BobPART.getIRI(), 
                                 createHiliteSeries("Carol's activities", 400, 450) ); 
    }
    
    private void createParticipantSummaryData()
    {
        participantActivitySummary = new HashMap<>();
        
        // Alice
        EccParticipantActivitySummaryResultSet psrs = new EccParticipantActivitySummaryResultSet( AlicePART );
        psrs.addActivitySummary( new EccActivitySummaryInfo("Used lift application",1) );
        participantActivitySummary.put( AlicePART.getIRI(), psrs );
        
        // Bob
        psrs = new EccParticipantActivitySummaryResultSet( BobPART );
        psrs.addActivitySummary( new EccActivitySummaryInfo("Used lift application",3) );
        participantActivitySummary.put( BobPART.getIRI(), psrs );
        
        // Carol
        psrs = new EccParticipantActivitySummaryResultSet( CarolPART );
        psrs.addActivitySummary( new EccActivitySummaryInfo("Used lift application",1) );
        participantActivitySummary.put( CarolPART.getIRI(), psrs );
    }
    
    private EccINTRATSeries createHiliteSeries( String key, int startIndex, int endIndex )
    {
        ArrayList<EccMeasurement> copyMeasures = new ArrayList<>();
        
        int srcIndex = 0;
        for ( EccMeasurement srcM : qosSeriesDemo )
        {
            // Create copy of measurement components
            Date   targDate = new Date( srcM.getTimestamp().getTime() );
            String targValue = null;
            
            // Add the value if within index range
            if ( srcIndex >= startIndex && srcIndex <= endIndex )
                targValue = srcM.getValue();
            
            // Add the duplicated measurement
            copyMeasures.add( new EccMeasurement( targDate, targValue) );
            
            ++srcIndex;
        }
        
        // Return new series
        return new EccINTRATSeries( key, true, copyMeasures );
    }
    
}
