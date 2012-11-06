/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
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
//      Created Date :          26-Oct-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;

import java.util.*;




public class MetricHelper
{
    public static Set<MeasurementSet> getAllMeasurementSets( Collection<MetricGenerator> mgenSet )
    {
        HashSet<MeasurementSet> mSets = new HashSet<MeasurementSet>();
        
        if ( mgenSet != null )
        {
            Iterator<MetricGenerator> mgenIt = mgenSet.iterator();
            while ( mgenIt.hasNext() )
            {
                Iterator<MetricGroup> mgIt = mgenIt.next().getMetricGroups().iterator();
                while ( mgIt.hasNext() )
                {
                    Iterator<MeasurementSet> msIt = mgIt.next().getMeasurementSets().iterator();
                    while ( msIt.hasNext() )
                    { mSets.add( msIt.next() ); }
                }
            }
        }
        
        return mSets;        
    }
    
    public static MeasurementSet getMeasurementSet( Collection<MetricGenerator> mgenSet,
                                                    UUID measurementSetID )
    {
        MeasurementSet targetSet = null;
      
        if ( mgenSet != null || measurementSetID != null )
        {
            Iterator<MetricGenerator> mgenIt = mgenSet.iterator();
            while ( mgenIt.hasNext() )
            {
                Iterator<MetricGroup> mgIt = mgenIt.next().getMetricGroups().iterator();
                while ( mgIt.hasNext() )
                {
                    Iterator<MeasurementSet> msIt = mgIt.next().getMeasurementSets().iterator();
                    while ( msIt.hasNext() )
                    { 
                        MeasurementSet ms = msIt.next();
                        if ( ms.getUUID().equals( measurementSetID) )
                        {
                            targetSet = ms;
                            break;
                        }
                    }
                }
            }
        }
        
        return targetSet;        
    }
}
