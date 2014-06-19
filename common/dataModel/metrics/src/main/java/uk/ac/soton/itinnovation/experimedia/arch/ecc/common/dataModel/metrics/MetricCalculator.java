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
//      Created Date :          30-Apr-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;

import java.util.*;




public class MetricCalculator
{
    public static Map<String, Integer> countValueFrequencies( Set<Measurement> measurements )
    {
        HashMap<String, Integer> freqMap = new HashMap<String, Integer>();
        
        if ( measurements != null )
        {
            for ( Measurement m : measurements )
            {
                String value = m.getValue();
                if ( value != null )
                {
                    if ( freqMap.containsKey(value) )
                    {
                        int count = freqMap.remove( value );
                        freqMap.put( value, ++count );
                    }
                    else freqMap.put(value, 1);
                }
            }
        }
                
        return freqMap;        
    }
}
