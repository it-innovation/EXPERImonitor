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

package uk.ac.soton.itinnovation.ecc.service.domain.explorer;

import java.util.ArrayList;




public class EccActivityApplicationResultSet
{
    private EccPROVActivity               activity;
    private ArrayList<EccPROVApplication> applicationList;
    
    public EccActivityApplicationResultSet( EccPROVActivity act )
    {
        activity = act;
        applicationList = new ArrayList<>();
    }
    
    public void addApplication( EccPROVApplication app )
    {
        if ( app != null && !applicationList.contains(app) )
            applicationList.add( app );
    }
    
    public EccPROVActivity getActivity()
    { return activity; }
    
    public ArrayList<EccPROVApplication> getApplications()
    { return applicationList; }
    
    public int getApplicationTotal()
    { return applicationList.size(); }
}
