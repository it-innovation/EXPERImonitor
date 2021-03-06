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
//      Created Date :          11-Aug-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.domain.explorer.distributions;

import java.util.ArrayList;




public class EccINTRATSeriesSet
{
    private ArrayList<EccINTRATSeries> seriesSet;
    
    public EccINTRATSeriesSet()
    {
        seriesSet = new ArrayList<>();
    }
    
    public ArrayList<EccINTRATSeries> getSeriesSet()
    { return seriesSet; }
    
    public void addSeries( EccINTRATSeries series )
    {
        if ( series != null && !seriesSet.contains(series) )
            seriesSet.add( series );
    }
}
