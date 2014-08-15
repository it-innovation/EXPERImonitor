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

package uk.ac.soton.itinnovation.ecc.service.utils;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;





public class MetricCalculator
{
    public static Map<String, Integer> countValueFrequencies( Set<Measurement> measurements )
    {
        HashMap<String, Integer> freqMap = new HashMap<>();
        
        if ( measurements != null && !measurements.isEmpty() )
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
    
    public static String getMostFrequentValue( Set<Measurement> measurements )
    {
        String result = null;
        
        if ( measurements != null && !measurements.isEmpty() )
        {
            Map<String, Integer> freqMap = countValueFrequencies( measurements );
            int largest = 0;
            
            for ( String value : freqMap.keySet() )
            {
                int size = freqMap.get( value );
                if ( largest < size )
                {
                    result = value;
                    largest = size;
                }
            }
        }
        
        return result;
    }
    
    public static float calcORDINALMedianValuePosition( MeasurementSet mSet )
    {
        float result = Float.NaN;
        
        if ( mSet != null )
        {
            // Check semantics before trying calculation
            Metric metric = mSet.getMetric();
            
            if ( metric != null && metric.getMetricType() == MetricType.ORDINAL &&
                 !metric.getMetaType().equals("Unknown") )
            {
                // Initialise scale map
                HashMap<String, Integer> ordMap = new HashMap<>();
                
                String[] orderedItems = metric.getMetaContent().split( "," );
                int index = 0;
                for ( String ordItem : orderedItems )
                {
                    ordMap.put( ordItem, index );
                    ++index;
                }
                
                // Create distribution map of values
                TreeSet<Integer> medMap = new TreeSet<>();
                
                for ( Measurement m : mSet.getMeasurements() )
                {
                    Integer order = ordMap.get( m.getValue() );
                    if ( order != null )
                        medMap.add( order );
                }
                
                // Create sorted list of results
                ArrayList<Integer> medList = new ArrayList();
                Iterator<Integer> medIt    = medMap.iterator();
                
                while ( medIt.hasNext() )
                    medList.add( medIt.next() );
                
                // If we have items to work with, find the median
                if ( !medList.isEmpty() )
                {
                    int medListSize = medList.size();
                    
                    // Single item ---------------------------------------------
                    if ( medListSize == 1 )
                        result = medList.get( 0 );
                    
                    // Two items -----------------------------------------------
                    else if ( medListSize == 2 )
                    {
                        // If the values are the same, return the index
                        if ( medList.get(0).compareTo(medList.get(1)) == 0 )
                            result = medList.get( 0 );
                        else
                            // Otherwise return point in the middle
                            result = (float) medList.get( 1 ) / (float) medList.get( 0 );
                    }
                    // More than two items -------------------------------------
                    else
                    {
                        int middle = medListSize / 2;
                        
                        // Even number of items means checking centre two values
                        if ( medListSize % 2 == 0 )
                        {
                            // If the values are the same, return the index
                            if ( medList.get(middle).compareTo(medList.get(middle -1)) == 0 )
                                result = medList.get( middle );
                            else
                                // Otherwise return point in the middle
                                result = (float) medList.get(middle) / (float) medList.get(middle-1);
                        }
                        // Odd number of items resolves to centre
                        else
                            result = middle;
                    }
                }
            }
        }
        
        return result;
    }
    
    public static MeasurementSet findNearestMeasurements( MeasurementSet   mSet,
                                                          Collection<Date> timeStamps )
    {
        MeasurementSet result = null;
        
        if ( mSet != null && timeStamps != null && !timeStamps.isEmpty() )
        {
            // Create a result set based on the input (but without measurements)
            result = new MeasurementSet( mSet, false );
            Set<Measurement> resultMS = result.getMeasurements();
            
            // Search through input samples
            Set<Measurement> samples = mSet.getMeasurements();
            
            if ( samples != null && !samples.isEmpty() )
                for ( Date stamp : timeStamps )
                {
                     Measurement nearest = null;
                     long stampTime      = stamp.getTime();
                     long lastDist       = 999999999999999999L;
                     
                     for ( Measurement m : samples )
                     {
                         long sampTime = m.getTimeStamp().getTime();
                         long diff = Math.abs( sampTime - stampTime );
                         
                         if ( diff < lastDist )
                         {
                             nearest  = m;
                             lastDist = diff;
                         }
                     }
                     
                     // Add result
                     if ( nearest != null ) resultMS.add( nearest );
                }
        }
        
        return result;
    }
    
    public static Properties calcINTRATSummary( MeasurementSet ms ) throws Exception
    {
        Properties result = new Properties();
        
        try
        {
            if ( validateINTRATMeasurementSet(ms) )
            {
                DescriptiveStatistics ds = new DescriptiveStatistics();
                
                for ( Measurement m : ms.getMeasurements() )
                    ds.addValue( Double.parseDouble(m.getValue()) );
                
                result.put( "floor",   ds.getMin() );
                result.put( "mean",    ds.getMean() );
                result.put( "ceiling", ds.getMax() );
            }
        }
        catch ( Exception ex ) { throw ex; }
        
        return result;
    }
    
    // Private methods ---------------------------------------------------------
    private static boolean validateINTRATMeasurementSet( MeasurementSet ms ) throws Exception
    {
        if ( ms == null ) throw new Exception( "Measurement set invalid" );
        
        Set<Measurement> measures = ms.getMeasurements();
        if ( measures == null ) throw new Exception( "Measurements in set invalid" );
        
        Metric metric = ms.getMetric();
        if ( metric == null ) throw new Exception( "Metric is invalid" );
        
        MetricType mt = metric.getMetricType();
        
        if ( mt == MetricType.NOMINAL || mt == MetricType.ORDINAL )
            throw new Exception( "Metric is not INTERVAL or RATIO" );
        
        return true;            
    }
}
