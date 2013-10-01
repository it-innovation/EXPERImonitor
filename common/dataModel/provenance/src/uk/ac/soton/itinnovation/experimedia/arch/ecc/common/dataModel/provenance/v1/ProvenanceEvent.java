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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.v1;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * A class reflecting a provenance event, such as experiments, entities and 
 * metric generators being created or destroyed/terminated. Provenance is associated
 * with the lifetime of an experiment and the components that take part in the
 * experiment. The provenance events are typically state changes at a certain time,
 * such as:
 *   - Experiment started
 *   - Entity (such as a VM) created / added to the experiment
 *   - Entity (such as a VM) shutting down
 *   - Metric generator connecting
 * 
 * @author Vegard Engen
 */
public class ProvenanceEvent implements Serializable
{
    private UUID uuid;
    private UUID experimentUUID;
    private EventSource eventSource;
    private Date timeStamp;
    private String event;
    // additional data needed? map of meta data / key value pairs... metrics?
    //   could be useful if we want to say, e.g., how long it took to create something, like the timestamping stuff in BonFIRE
    //   or perhaps that is out of scope for what provenance is... perhaps wait until we have a proper usecase?

    /**
     * Default constructor which generates a random UUID.
     */
    public ProvenanceEvent()
    {
        this.uuid = UUID.randomUUID();
    }
    
    /**
     * Copy constructor, which takes a deep copy of all objects.
     * @param pe The ProvenanceEvent object from which a copy is made.
     */
    public ProvenanceEvent(ProvenanceEvent pe)
    {
        this.uuid = UUID.fromString(pe.getUUID().toString());
        this.experimentUUID = UUID.fromString(pe.getExperimentUUID().toString());
        this.eventSource = new EventSource(pe.getEventSource());
        this.timeStamp = new Date(pe.getTimeStamp().getTime());
        this.event = pe.getEvent();
    }
    
    /**
     * Overloaded constructor to set all the attributes of the class.
     * @param uuid
     * @param experimentUUID
     * @param eventSource
     * @param timeStamp
     * @param event 
     */
    public ProvenanceEvent(UUID uuid, UUID experimentUUID, EventSource eventSource, Date timeStamp, String event)
    {
        this.uuid = uuid;
        this.experimentUUID = experimentUUID;
        this.eventSource = eventSource;
        this.timeStamp = timeStamp;
        this.event = event;
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
     * @return the experimentUUID
     */
    public UUID getExperimentUUID()
    {
        return experimentUUID;
    }

    /**
     * @param experimentUUID the experimentUUID to set
     */
    public void setExperimentUUID(UUID experimentUUID)
    {
        this.experimentUUID = experimentUUID;
    }

    /**
     * @return the eventSource
     */
    public EventSource getEventSource()
    {
        return eventSource;
    }

    /**
     * @param eventSource the eventSource to set
     */
    public void setEventSource(EventSource eventSource)
    {
        this.eventSource = eventSource;
    }

    /**
     * @return the timeStamp
     */
    public Date getTimeStamp()
    {
        return timeStamp;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(Date timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    /**
     * @return the event
     */
    public String getEvent()
    {
        return event;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(String event)
    {
        this.event = event;
    }
}
