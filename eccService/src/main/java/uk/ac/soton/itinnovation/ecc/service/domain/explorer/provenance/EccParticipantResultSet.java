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
//      Created Date :          24-Jul-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.domain.explorer.provenance;

import uk.ac.soton.itinnovation.ecc.service.domain.explorer.provenance.EccParticipant;
import java.util.ArrayList;




public class EccParticipantResultSet
{
    private ArrayList<EccParticipant> participants;
    
    public EccParticipantResultSet()
    {
        participants = new ArrayList<>();
    }
    
    public void addParticipant( EccParticipant part )
    {
        if ( part != null && !participants.contains(part) )
                participants.add( part );
    }
    
    public ArrayList<EccParticipant> getParticipants()
    { return participants; }
}
