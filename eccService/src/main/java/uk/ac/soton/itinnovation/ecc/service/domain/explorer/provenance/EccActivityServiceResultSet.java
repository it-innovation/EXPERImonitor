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
//      Created By :            sgc
//      Created Date :          11-Sep-2014
//      Created for Project :   EccService
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.domain.explorer.provenance;

import java.util.ArrayList;




public class EccActivityServiceResultSet
{
    private EccActivity           activity;
    private ArrayList<EccService> serviceList;
    
    
    public EccActivityServiceResultSet( EccActivity act )
    {
        activity = act;
        serviceList = new ArrayList<>();
    }
    
    public EccActivity getActivity()
    { return activity; }
    
    public ArrayList<EccService> getServices()
    { return serviceList; }
    
    public int getServiceTotal()
    { return serviceList.size(); }
    
    public void addService( EccService service )
    {
        if ( service != null && !serviceList.contains(service) )
            serviceList.add( service );
    }
}
