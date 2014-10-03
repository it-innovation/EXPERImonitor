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
//      Created Date :          05-Aug-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.domain.explorer.distributions;

import java.util.ArrayList;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.provenance.EccParticipant;




public class EccNOMORDParticipantSummary
{
    EccParticipant                participant;
    ArrayList<EccNOMORDItemCount> qoeResponseSummary;
    

    public EccNOMORDParticipantSummary( EccParticipant part )
    {
        participant = part;
        qoeResponseSummary = new ArrayList<>();
    }
    
    public EccParticipant getParticipant()
    { return participant; }
    
    public ArrayList<EccNOMORDItemCount> getSummary()
    {
        return qoeResponseSummary;
    }
    
    public void addNOMINALResponse( String label, String value )
    {
        if ( label != null && value != null )
        {
            EccNOMORDItemCount nic = new EccNOMORDItemCount( label, value );
            qoeResponseSummary.add( nic );
        }
    }
    
    public void addORDINALResponse( String label, String value, int order, int maxOrder )
    {
        if ( label != null && value != null )
        {
            EccNOMORDItemCount nic = new EccNOMORDItemCount( label, value, order, maxOrder );
            qoeResponseSummary.add( nic );
        }
    }
}
