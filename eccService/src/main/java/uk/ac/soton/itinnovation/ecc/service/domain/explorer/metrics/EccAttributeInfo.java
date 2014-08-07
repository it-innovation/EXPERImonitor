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
//      Created Date :          22-Jul-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.domain.explorer.metrics;

import java.util.UUID;




public class EccAttributeInfo
{
    private String name;
    private String description;
    private UUID   metricID;
    private String metricUnit;
    private String metricType;
    private String metricMetaType;
    private String metricMetaContent;
    private int    sampleCount;
    
    public EccAttributeInfo( String attrName,
                             String attrDesc,
                             UUID   attrID,
                             String metUnit,
                             String metType,
                             String metMetaType,
                             String metMetCont )
    {
        name              = attrName;
        description       = attrDesc;
        metricID          = attrID;
        metricUnit        = metUnit;
        metricType        = metType;
        metricMetaType    = metMetaType;
        metricMetaContent = metMetCont;
    }
    
    public String getName()
    { return name; }
    
    public String getDescription()
    { return description; }
    
    public String getMetricID()
    { return metricID.toString(); }
    
    public String getUnit()
    { return metricUnit; }
    
    public String getMetricType()
    { return metricType; }
    
    public String getMetaType()
    { return metricMetaType; }
    
    public String getMetaContent()
    { return metricMetaContent; }
    
    public int getSampleCount()
    { return sampleCount; }
    
    public void setSampleCount( int count )
    { sampleCount = count; }
}
