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
//      Created Date :          17-Apr-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.ecc.service.test.experiment;

import uk.co.soton.itinnovation.ecc.service.Application;
import uk.co.soton.itinnovation.ecc.service.process.LivePROVConsumer;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.junit.runner.RunWith;
import org.junit.*;

import java.util.Properties;
import java.io.*;
import java.util.*;






/**
 * Tests the basic operation of connecting to the PROVenance repository and
 * storing data
 * 
 * @author Simon Crowle
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LivePROVConsumerTest
{
    private Properties provProps;

    @Before
    public void setUp()
    {
        // Try loading the PROV repo properties
        provProps = new Properties();
        try
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream is = cl.getResourceAsStream( "prov.properties" );
            
            provProps.load( is );
        }
        catch ( IOException ioex )
        { Assert.fail(ioex.getMessage()); }
    }
    
    @Test
    public void testConnectDisconnect()
    {
        // Check PROV repo properties have been retrieved
        Assert.assertNotNull( provProps );
        
        // Try connecting/disconnecting a LIVEPROVConsumer
        LivePROVConsumer lpc = new LivePROVConsumer();
        UUID expID = UUID.randomUUID();
        
        try
        {
            lpc.createExperimentRepository( expID, "LivePROVConsumerTest instance", provProps );
            
            lpc.closeCurrentExperimentRepository();
        }
        catch ( Exception ex )
        { Assert.fail(ex.getMessage()); }
    }    
}
