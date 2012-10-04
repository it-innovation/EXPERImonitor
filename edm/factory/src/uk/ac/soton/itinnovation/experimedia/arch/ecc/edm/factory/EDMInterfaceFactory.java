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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.MonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.MonitoringEDMLight;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDMLight;


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
     * 
     * @return Instance of the EDM monitoring.
     * @throws Exception 
     */
    public static IMonitoringEDM getMonitoringEDM() throws Exception
    {
        IMonitoringEDM edm = new MonitoringEDM();
        
        return edm;
    }
    
    /**
     * Get a Monitoring EDM Light interface, to get access to storing and retrieving
     * monitoring data.
     * 
     * @return Instance of the light version of EDM monitoring.
     * @throws Exception 
     */
    public static IMonitoringEDMLight getMonitoringEDMLight() throws Exception
    {
        IMonitoringEDMLight edm = new MonitoringEDMLight();
        
        return edm;
    }
}