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
//      Created Date :          15-Aug-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.domain.explorer.distributions;




public class EccParallelSetTuple
{
    private final String partName;
    private final String activityLabel;
    private final String applicationName;
    private final String serviceName;
    private final String qosAttribute;
    private final String qoSStatistic;
    private final String qosStatValue;
    
    public EccParallelSetTuple( String pName,
                                String actLabel,
                                String appName,
                                String servName,
                                String qosAttr,
                                String qosStat,
                                String statVal )
    {
        partName = pName;
        activityLabel = actLabel;
        applicationName = appName;
        serviceName = servName;
        qosAttribute = qosAttr;
        qoSStatistic = qosStat;
        qosStatValue = statVal;
    }
    
    public String getParticipantName()
    { return partName; }
    
    public String getActivityLabel()
    { return activityLabel; }
    
    public String getApplicationName()
    { return applicationName; }
    
    public String getQoSAttribute()
    { return qosAttribute; }
    
    public String getQoSStat()
    { return qoSStatistic + " : " + qosStatValue; }
}
