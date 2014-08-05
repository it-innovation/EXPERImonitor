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
//      Created Date :          23-Jul-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.domain.explorer.distributions;

import java.util.*;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.EccAttributeInfo;




public class EccNOMORDSummary
{
    private EccAttributeInfo        attrInfo;
    private ArrayList<EccItemCount> distribData;
    
    
    public EccNOMORDSummary( EccAttributeInfo     atInfo,
                             Map<String, Integer> valueCounts )
    {
        if ( atInfo != null && valueCounts != null )
        {
            // Sort value counts according to type
            if ( atInfo.getMetricType().equals("NOMINAL") )
            {
                attrInfo = atInfo;
                sortDataAlphabetically( valueCounts );
            }
            else if ( atInfo.getMetricType().equals("ORDINAL") )
            {
                attrInfo = atInfo;
                sortDataByOrder( atInfo, valueCounts );
            }
        }
    }
    
    public EccAttributeInfo getAttributeInfo()
    { return attrInfo; }
    
    public ArrayList<EccItemCount> getValues()
    { return distribData; }
    
    // Private methods ---------------------------------------------------------
    private void sortDataAlphabetically( Map<String, Integer> valueCounts )
    {
        // Sort
        TreeMap<String, Integer> sortedValues = new TreeMap<>();

        for ( String label : valueCounts.keySet() )
            sortedValues.put( label, valueCounts.get(label) );

        // Add to histogram data
        distribData = new ArrayList<>();

        for ( String label : sortedValues.keySet() )
        {
            EccItemCount eic = new EccItemCount( label, sortedValues.get(label) );
            distribData.add( eic );
        }
    }
    
    private void sortDataByOrder( EccAttributeInfo atInfo,
                                  Map<String, Integer> valueCounts )
    {
        distribData = new ArrayList<>();
        
        String[] orderedLabels = atInfo.getMetaContent().split(",");
        
        for ( String label : orderedLabels )
        {
            EccItemCount eic = new EccItemCount( label, valueCounts.get(label) );
            distribData.add( eic );
        }
    }
}
