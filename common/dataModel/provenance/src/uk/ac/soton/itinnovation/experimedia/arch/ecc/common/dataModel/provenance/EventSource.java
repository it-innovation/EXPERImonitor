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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance;

import java.io.Serializable;
import java.util.UUID;

/**
 * This class represents the source of a provenance event, which specifies the
 * source according to an enum as being either an experiment, an entity or a
 * metric generator. The source is also associated with a UUID, which can be used
 * to get more information according to respective classes in the 
 * common.dataModel.metrics package (Experiment, Event and MetrigGenerator).
 * 
 * @author Vegard Engen
 */
public class EventSource implements Serializable
{
    private UUID sourceUUID; // uuid can be used to get more details of the source
    private EventSourceType eventSourceType; // experiment, entity or metric generator

    /**
     * Empty constructor - does nothing.
     */
    public EventSource(){}
    
    /**
     * Copy constructor, which takes a deep copy.
     * @param es The EventSource object from which a copy is made.
     */
    public EventSource(EventSource es)
    {
        this.sourceUUID = UUID.fromString(es.getSourceUUID().toString());
        this.eventSourceType = es.getEventSourceType();
    }
    
    /**
     * Constructor to set the event source UUID and the event source type.
     * @param srcUUID Event source UUID.
     * @param evtSrcType Event source type.
     */
    public EventSource(UUID srcUUID, EventSourceType evtSrcType)
    {
        this.sourceUUID = srcUUID;
        this.eventSourceType = evtSrcType;
    }
    
    /**
     * @return the sourceUUID
     */
    public UUID getSourceUUID()
    {
        return sourceUUID;
    }

    /**
     * @param sourceUUID the sourceUUID to set
     */
    public void setSourceUUID(UUID sourceUUID)
    {
        this.sourceUUID = sourceUUID;
    }

    /**
     * @return the eventSourceType
     */
    public EventSourceType getEventSourceType()
    {
        return eventSourceType;
    }

    /**
     * @param eventSourceType the eventSourceType to set
     */
    public void setEventSourceType(EventSourceType eventSourceType)
    {
        this.eventSourceType = eventSourceType;
    }
    
}
