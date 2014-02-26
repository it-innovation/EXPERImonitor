/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
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
//      Created By :            Vegard Engen
//      Created Date :          01-October-2012
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.client.IEDMClientPersistence;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.MonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.MonitoringEDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDM;

import java.util.Properties;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.client.EDMClientPersistence;


/**
 * EDMInterfaceFactory is a simple factory class that generates EDM interfaces.
 * 
 * @author Vegard Engen
 */
public class EDMInterfaceFactory
{
    /**
     * Default constructor for the EDM interface factory.
     */
    public EDMInterfaceFactory()
    {
        
    }
    
    /**
     * Get a Monitoring EDM interface, to get access to storing and retrieving
     * monitoring data.
     * Will set up the EDM DAO according to the EDM properties file on the class path.
     * @return Instance of the EDM monitoring.
     * @throws Exception 
     */
    public static IMonitoringEDM getMonitoringEDM() throws Exception
    {
        IMonitoringEDM edm = new MonitoringEDM();
        
        return edm;
    }
    
    /**
     * Get a Monitoring EDM interface, to get access to storing and retrieving
     * monitoring data.
     * @param config Configuration parameters, which will be used instead of reading config file from disk.
     * @return Instance of the EDM monitoring.
     * @throws Exception 
     */
    public static IMonitoringEDM getMonitoringEDM(Properties config) throws Exception
    {
        IMonitoringEDM edm = new MonitoringEDM(config);
        
        return edm;
    }
    
    /**
     * Get a Monitoring EDM Agent interface, to get access to storing and retrieving
     * monitoring data.
     * Will set up the EDM DAO according to the EDM properties file on the class path.
     * @return Instance of the light version of EDM monitoring.
     * @throws Exception 
     */
    public static IMonitoringEDMAgent getMonitoringEDMAgent() throws Exception
    {
        IMonitoringEDMAgent edm = new MonitoringEDMAgent();
        
        return edm;
    }
    
    /**
     * Get a Monitoring EDM Agent interface, to get access to storing and retrieving
     * monitoring data.
     * @param config Configuration parameters, which will be used instead of reading config file from disk.
     * @return Instance of the light version of EDM monitoring.
     * @throws Exception 
     */
    public static IMonitoringEDMAgent getMonitoringEDMAgent(Properties config) throws Exception
    {
        IMonitoringEDMAgent edm = new MonitoringEDMAgent(config);
        
        return edm;
    }
		
		public static IEDMClientPersistence getEDMClientPersistence(Properties config) throws Exception
		{
			IEDMClientPersistence ecp = new EDMClientPersistence(config);
			
			return ecp; 
		}
}
