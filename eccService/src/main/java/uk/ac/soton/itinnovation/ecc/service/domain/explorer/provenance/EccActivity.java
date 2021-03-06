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
//      Created Date :          30-Jul-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.domain.explorer.provenance;

import java.util.Date;




public class EccActivity
{
    private String name;
    private String description;
    private String provIRI;
    private Date   actStarted;
    private Date   actEnded;
    
    
    public EccActivity( String aName,
                        String aDesc,
                        String aIRI,
                        Date   started,
                        Date   ended )
    {
        name        = aName;
        description = aDesc;
        provIRI     = aIRI;
        actStarted  = started;
        actEnded    = ended;
    }
    
    public String getName()
    { return name; }
    
    public String getDescription()
    { return description; }
    
    public String getIRI()
    { return provIRI; }
    
    public Date getStartTime()
    { return actStarted; }
    
    public Date getEndTime()
    { return actEnded; }
}
