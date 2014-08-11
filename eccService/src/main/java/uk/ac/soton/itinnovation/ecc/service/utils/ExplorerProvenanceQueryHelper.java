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
//      Created By :            Stefanie Wiegand
//      Created Date :          08-Aug-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.utils;

import java.util.*;




public class ExplorerProvenanceQueryHelper {

    public ExplorerProvenanceQueryHelper() {
        
    }
    
    public void initialise() throws Exception {
        
    }
    
    public void shutdown() {
        
    }
    
    /**
     * Use this method to get the IRI for all Participants associated with an experiment.
     * 
     * @param expID - Non-null experiment ID
     * @return      - Returns a (possibly empty) set of IRIs representing participants
     */
    public Set<String> getParticipantIRIsForExperiment( UUID expID ) {
        
        HashSet<String> result = new HashSet<>();
        
        
        return result;
    }
}
