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
//      Created Date :          31-Jul-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.domain.explorer.distributions;

import java.util.Date;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.metrics.EccAttributeInfo;




public class EccINTRATSummary
{
    private EccAttributeInfo attrInfo;
    
    private float floorValue;
    private float ceilingValue;
    private float averageValue;
    private Date  startTime;
    private Date  endTime;
    
    public EccINTRATSummary( EccAttributeInfo info,
                             float fV, float cV, float aV,
                             Date sT, Date eT )
    {
        attrInfo     = info;
        floorValue   = fV;
        ceilingValue = cV;
        averageValue = aV;
        startTime    = sT;
        endTime      = eT;
    }
    
    public EccAttributeInfo getAttribute()
    { return attrInfo; }
    
    public float getFloorValue()
    { return floorValue; }
    
    public float getCeilingValue()
    { return ceilingValue; }
    
    public float getAverageValue()
    { return averageValue; }
    
    public Date getStartTime()
    { return startTime; }
    
    public Date getEndTime()
    { return endTime; }
}
