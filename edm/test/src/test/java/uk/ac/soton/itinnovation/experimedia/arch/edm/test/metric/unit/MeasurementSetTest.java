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
//      Created Date :          02-May-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.edm.test.metric.unit;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.edm.test.metrics.PopulateDB;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMeasurementSetDAO;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.*;

import org.slf4j.*;

import java.util.*;




@RunWith(JUnit4.class)
public class MeasurementSetTest extends TestCase
{
    private static final Logger log = LoggerFactory.getLogger( MeasurementSetTest.class );
    
    private IMonitoringEDM     edm = null;
    private IMeasurementSetDAO msDAO = null;
    
    @Before
    public void beforeTest()
    {        
        try
        {
            Properties prop = new Properties();
            prop.load( AGeneralTest.class.getClassLoader().getResourceAsStream(EDMTestSuite.propertiesFile) );
            
            edm = EDMInterfaceFactory.getMonitoringEDM(prop);
            edm.clearMetricsDatabase();
            msDAO = edm.getMeasurementSetDAO();
            
            // Create experiment 1
            PopulateDB.saveExperiment( edm, PopulateDB.expUUID );
            
            PopulateDB.saveEntity1( edm, 
                                    PopulateDB.entity1UUID, 
                                    PopulateDB.entity1attribute1UUID, 
                                    PopulateDB.entity1attribute2UUID, 
                                    PopulateDB.entity1attribute3UUID );
        } 
        catch (Exception ex)
        {
            String msg = "Unable to set up EDM and populate DB with necessary data to perform the MetricGenerator tests: " + ex.toString();
            Assert.fail( msg );
        }
    }
    
    @Test
    public void testGetSingleMeasurementSetForAttribute()
    {
        try
        {
            PopulateDB.saveMetricGenerator1( edm, 
                                             PopulateDB.expUUID, 
                                             PopulateDB.entity1UUID, 
                                             PopulateDB.entity1attribute1UUID, 
                                             PopulateDB.entity1attribute2UUID, 
                                             PopulateDB.entity1attribute3UUID, 
                                             PopulateDB.mGen1UUID, 
                                             PopulateDB.mGrp1UUID, 
                                             PopulateDB.mGrp1mSet1UUID, 
                                             PopulateDB.mGrp1mSet2UUID, 
                                             PopulateDB.mGrp1mSet3UUID );
            
            Set<MeasurementSet> mSets = msDAO.getMeasurementSetsForAttribute( PopulateDB.entity1attribute1UUID, true );
            
            Assert.assertNotNull( mSets );
            Assert.assertTrue( mSets.size() == 1 );
            
            MeasurementSet ms = mSets.iterator().next();
            Assert.assertNotNull( ms );
            Assert.assertNotNull( ms.getMetric() );
            Assert.assertTrue( ms.getID().equals(PopulateDB.mGrp1mSet1UUID) );
            
            Assert.assertNotNull( ms.getAttributeID() );
            Assert.assertTrue( ms.getAttributeID().equals(PopulateDB.entity1attribute1UUID) );
            
            log.info( "MeasurementSet test: single measurement set from attribute completed" );
        }
        catch ( Exception ex )
        { Assert.fail( ex.getMessage() ); }
    }
    
    @Test
    public void testGetMultipleMeasurementSetsForAttribute()
    {
        try
        {
            PopulateDB.saveMetricGenerator1( edm, 
                                             PopulateDB.expUUID, 
                                             PopulateDB.entity1UUID, 
                                             PopulateDB.entity1attribute1UUID, 
                                             null, 
                                             null, 
                                             PopulateDB.mGen1UUID, 
                                             PopulateDB.mGrp1UUID, 
                                             PopulateDB.mGrp1mSet1UUID, 
                                             PopulateDB.mGrp1mSet2UUID, 
                                             PopulateDB.mGrp1mSet3UUID );
            
            Set<MeasurementSet> mSets = msDAO.getMeasurementSetsForAttribute( PopulateDB.entity1attribute1UUID, true );
            
            Assert.assertNotNull( mSets );
            Assert.assertTrue( mSets.size() == 3 );
            
            for ( MeasurementSet ms : mSets )
            {
                Assert.assertNotNull( ms );
                Assert.assertNotNull( ms.getMetric() );
                Assert.assertNotNull( ms.getAttributeID() );
                Assert.assertTrue( ms.getAttributeID().equals(PopulateDB.entity1attribute1UUID) );
            }
            
            log.info( "MeasurementSet test: multiple measurement sets from attribute completed" );
        }
        catch ( Exception ex )
        { Assert.fail( ex.getMessage() ); }
    }
    
    @Test
    public void testExperimentSpecificMeasurementSetForAttribute()
    {
        try
        {
            // Save a metric generator for experiment 1
            PopulateDB.saveMetricGenerator1( edm, 
                                             PopulateDB.expUUID, 
                                             PopulateDB.entity1UUID, 
                                             PopulateDB.entity1attribute1UUID, 
                                             PopulateDB.entity1attribute2UUID, 
                                             PopulateDB.entity1attribute3UUID, 
                                             PopulateDB.mGen1UUID, 
                                             PopulateDB.mGrp1UUID, 
                                             PopulateDB.mGrp1mSet1UUID, 
                                             PopulateDB.mGrp1mSet2UUID, 
                                             PopulateDB.mGrp1mSet3UUID );
            
            // Create additional experiment (we should not get data back from this one)
            createSecondExperimentData();
            
            // Try find measurement set from experiment 1
            Set<MeasurementSet> mSets = msDAO.getMeasurementSetsForAttribute( PopulateDB.entity1attribute1UUID,
                                                                              PopulateDB.expUUID,
                                                                              true );
            
            Assert.assertNotNull( mSets );
            Assert.assertTrue( mSets.size() == 1 );
            
            MeasurementSet ms = mSets.iterator().next();
            Assert.assertNotNull( ms );
            Assert.assertNotNull( ms.getMetric() );
            Assert.assertTrue( ms.getID().equals(PopulateDB.mGrp1mSet1UUID) );
            
            Assert.assertNotNull( ms.getAttributeID() );
            Assert.assertTrue( ms.getAttributeID().equals(PopulateDB.entity1attribute1UUID) );
            
            
            // Try NOT to find experiment sets for experiment 2
            mSets = msDAO.getMeasurementSetsForAttribute( PopulateDB.entity1attribute1UUID,
                                                          PopulateDB.exp2UUID,
                                                          true );
            
            Assert.assertNotNull( mSets );
            Assert.assertTrue( mSets.isEmpty() );
            
            log.info( "MeasurementSet test: experiment specific measurement set from attribute completed" );
        }
        catch ( Exception ex )
        { Assert.fail( ex.getMessage() ); }
    }
    
    @Test
    public void testExperimentSpecificMultipleMeasurementSetsForAttribute()
    {
        try
        {
            // Save a metric generator for experiment 1
            PopulateDB.saveMetricGenerator1( edm, 
                                             PopulateDB.expUUID, 
                                             PopulateDB.entity1UUID, 
                                             PopulateDB.entity1attribute1UUID, 
                                             null,
                                             null,
                                             PopulateDB.mGen1UUID, 
                                             PopulateDB.mGrp1UUID, 
                                             PopulateDB.mGrp1mSet1UUID, 
                                             PopulateDB.mGrp1mSet2UUID, 
                                             PopulateDB.mGrp1mSet3UUID );
            
            // Create additional experiment (we should not get data back from this one)
            createSecondExperimentData();
            
            // Try find measurement sets from experiment 1
            Set<MeasurementSet> mSets = msDAO.getMeasurementSetsForAttribute( PopulateDB.entity1attribute1UUID,
                                                                              PopulateDB.expUUID,
                                                                              true );
            
            Assert.assertNotNull( mSets );
            Assert.assertTrue( mSets.size() == 3 );
            
            for ( MeasurementSet ms : mSets )
            {
                Assert.assertNotNull( ms );
                Assert.assertNotNull( ms.getMetric() );
                Assert.assertNotNull( ms.getAttributeID() );
                Assert.assertTrue( ms.getAttributeID().equals(PopulateDB.entity1attribute1UUID) );
            }
            
            // Try NOT to find measurement set for experiment 2
            mSets = msDAO.getMeasurementSetsForAttribute( PopulateDB.entity1attribute1UUID,
                                                          PopulateDB.exp2UUID,
                                                          true );
            
            Assert.assertNotNull( mSets );
            Assert.assertTrue( mSets.isEmpty() );
            
            
            log.info( "MeasurementSet test: experiment specific multiple measurement sets from attribute completed" );
        }
        catch ( Exception ex )
        { Assert.fail( ex.getMessage() ); }
    }
    
    // Private methods ---------------------------------------------------------
    private void createSecondExperimentData()
    {
        try
        {
            PopulateDB.saveExperiment( edm, PopulateDB.exp2UUID );
            
            PopulateDB.saveEntity2( edm, 
                                    PopulateDB.entity2UUID, 
                                    PopulateDB.entity2attribute1UUID, 
                                    PopulateDB.entity2attribute2UUID, 
                                    PopulateDB.entity2attribute3UUID ); 
            
            PopulateDB.saveMetricGenerator2( edm, 
                                             PopulateDB.exp2UUID, 
                                             PopulateDB.entity2UUID, 
                                             PopulateDB.entity2attribute1UUID, 
                                             PopulateDB.entity2attribute2UUID, 
                                             PopulateDB.entity2attribute3UUID, 
                                             PopulateDB.mGen2UUID, 
                                             PopulateDB.mGrp2UUID, 
                                             PopulateDB.mGrp2mSet1UUID, 
                                             PopulateDB.mGrp2mSet2UUID, 
                                             PopulateDB.mGrp2mSet3UUID );
        }
        catch ( Exception ex )
        { Assert.fail( ex.getMessage() ); }
    }
}
