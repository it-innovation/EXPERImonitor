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
//      Created Date :          18-Jul-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.domain.explorer;

import java.util.UUID;




public class EccParticipant
{
    private final String name;
    private final String description;
    private final String metricEntityID; // No default ctor for UUID (JSON requires this)
    private final String provIRI;
    
    
    public EccParticipant( String pName,
                           String pDesc, 
                           UUID pmID, 
                           String ppID ) {
        
        name           = pName;
        description    = pDesc;
        metricEntityID = pmID.toString();
        provIRI        = ppID;
    }
    
    public String getName()
    { return name; }
    
    public String getDescription()
    { return description; }
    
    public UUID getMetricEntityID()
    { return UUID.fromString(metricEntityID); }
    
    public String getIRI()
    { return provIRI; }
}
