/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2012
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
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
//	Created By :			Maxim Bashevoy
//	Created Date :			2012-10-08
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////

package eu.experimedia.itinnovation.scc.web.adapters;

import java.util.EnumSet;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.EMIAdapterListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.EMInterfaceAdapter;

public class WeGovEMInterfaceAdapter extends EMInterfaceAdapter {

    public WeGovEMInterfaceAdapter(EMIAdapterListener listener) {
        super(listener);
    }
    
    @Override
    public void onRequestActivityPhases( UUID senderID )
    {
        if ( senderID.equals(expMonitorID) )
        {
            EnumSet<EMPhase> phases = EnumSet.noneOf( EMPhase.class );
            phases.add( EMPhase.eEMSetUpMetricGenerators );
            phases.add( EMPhase.eEMLiveMonitoring );
            phases.add( EMPhase.eEMPostMonitoringReport );
            phases.add( EMPhase.eEMTearDown );

            discoveryFace.sendActivePhases( phases );
        }
    }

}
