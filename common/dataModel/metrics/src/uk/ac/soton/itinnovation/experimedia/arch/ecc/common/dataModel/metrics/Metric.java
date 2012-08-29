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
//      Created Date :          2012-08-09
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;

import java.io.Serializable;
import java.util.UUID;
import javax.measure.unit.Unit;

/**
 * The metric class details the measurement of an attribute (of an entity).
 * 
 * For information about using the JScience unit, have a look at these resources:
 * 
 *   http://www.javaworld.com/javaworld/jw-10-2007/jw-10-jsr275.html
 *   http://www.jscience.org/api/index.html
 *   http://www.jscience.org/api/javax/measure/unit/Unit.html
 *   http://www.jscience.org/api/javax/measure/quantity/Quantity.html
 *   http://www.jscience.org/api/javax/measure/unit/SI.html
 * 
 * Examples of units are:
 * 
 *   Unit min = MINUTE;
 *   Unit ms = MILLI(SECOND);
 * 
 *   Unit metre = METRE;
 *   Unit cm = CENTI(METER);
 *   Unit foot = METER.times(3048).divide(10000);
 * 
 *   Unit bps = BIT.divide(SECOND);
 * 
 * Setting a unit:
 * 
 *   Metric m = new Metric();
 *   m.setUnit(MINUTE);
 * 
 * @author Vegard Engen
 */
public class Metric implements Serializable
{
    private UUID uuid;
    private MetricType metricType;
    private Unit unit;
    
    // other things?
    //   - conversion stuff?

    /**
     * Default constructor, which sets a random UUID for the object instance.
     */
    public Metric()
    {
        this.uuid = UUID.randomUUID();
    }
    
    /**
     * Copy constructor; OBS: does a shallow copy of the unit.
     * @param m The metric object a copy is made of.
     */
    public Metric(Metric m)
    {
        if (m == null)
            return;
        
        if (m.getUUID() != null)
            this.uuid = UUID.fromString(m.getUUID().toString());
        if (m.getMetricType() != null)
            this.metricType = m.getMetricType();
        if (m.getUnit() != null)
            this.unit = m.getUnit();
    }
    
    /**
     * Constructor to set all the fields of the Metric object.
     * @param uuid The UUID used to uniquely identify a metric in this framework.
     * @param metricType The type of metric (e.g., nominal or interval).
     * @param unit The unit of the metric (e.g., meters or miles/second).
     */
    public Metric(UUID uuid, MetricType metricType, Unit unit)
    {
        this.uuid = uuid;
        this.metricType = metricType;
        this.unit = unit;
    }
    
    /**
     * @return the uuid
     */
    public UUID getUUID()
    {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUUID(UUID uuid)
    {
        this.uuid = uuid;
    }

    /**
     * @return the metricType
     */
    public MetricType getMetricType()
    {
        return metricType;
    }

    /**
     * @param metricType the metricType to set
     */
    public void setMetricType(MetricType metricType)
    {
        this.metricType = metricType;
    }

    /**
     * @return the unit
     */
    public Unit getUnit()
    {
        return unit;
    }

    /**
     * @param unit the unit to set
     */
    public void setUnit(Unit unit)
    {
        this.unit = unit;
    }
}
