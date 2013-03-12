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
//      Created Date :          11-Oct-2012
//      Created for Project :   EXPERIMENT
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;



/**
 * This utility class represents a measurement task that will be carried out
 * periodically. Use the MeasurementScheduler to construct instances of
 * these classes.
 * 
 * @author Simon Crowle
 */
public class MeasurementTask
{
    private MeasurementScheduler measurementScheduler;
    private ITakeMeasurement     measurementListener;
    private MeasurementSet       measurementSet;
    private eTaskRecurrance      taskRecurrance;
    private TimedMeasurementTask timedMeasurementTask;
    private int                  reoccurCount;
    private long                 intervalMilliSecs;
    private boolean              isTakingMeasurements = false;
    
    public enum eTaskRecurrance { NEVER,
                                  FINITE,
                                  INFINITE };
    
    /**
     * Construction of the measurement task should be carried out by the 
     * MeasurementScheduler.
     * 
     * @param scheduler   - Instance of the measurement scheduler used to construct this task
     * @param listener    - Instance of the class that actual makes a measurement for this task
     * @param ms          - The MeasurementSet that this task is associated with
     * @param repetitions - The number of repetitions of this task that should be carried out. -1 = infinite; 0 = once only; other positive integers for finite number
     * @param intervalMS  - The time in milliseconds that should elapse before the task is carried out again
     */
    public MeasurementTask( MeasurementScheduler scheduler,
                            ITakeMeasurement listener,
                            MeasurementSet ms,
                            int repetitions,
                            long intervalMS )
    {
        measurementScheduler = scheduler;
        measurementListener  = listener;
        measurementSet       = ms;
        intervalMilliSecs    = intervalMS;
      
        if ( repetitions == -1 )
        {
            taskRecurrance = eTaskRecurrance.INFINITE;
            reoccurCount   = repetitions;
        }
        else if ( repetitions == 0 )
        {
            taskRecurrance = eTaskRecurrance.NEVER;
            reoccurCount   = repetitions;
        }
        else if ( repetitions > 0 )
        {
            taskRecurrance = eTaskRecurrance.FINITE;
            reoccurCount   = repetitions;
        }
        
        timedMeasurementTask = new TimedMeasurementTask();
    }
    
    /**
     * Returns the MeasurementSet ID for this task.
     * 
     * @return - UUID of the MeasurementSet
     */
    public UUID getMeasurementSetID()
    { return measurementSet.getID(); }
    
    /**
     * Returns the recurrance type for this task
     * 
     * @return - eTaskRecurrance type.
     */
    public eTaskRecurrance getTaskRecurranceType()
    { return taskRecurrance; }
    
    /**
     * Depending on the recurrance type for this task, the value return is either:
     * FINITE   : The number of times the task will be executed again from this point onwards
     * INFINITE : The number of times the task has already been executed
     * 
     * @return - integer representing the recurrance of this task.
     */
    public int getRecurrances()
    { return reoccurCount; }
    
    /**
     * Returns the interval elapse time for this task.
     * 
     * @return - elapse time in milliseconds.
     */
    public long getMeasurementInterval()
    { return intervalMilliSecs; }
    
    /**
     * Determines whether the task is active.
     * 
     * @return - returns true if the task is currently scheduled to measure.
     */
    public boolean isMeasuring()
    { return isTakingMeasurements; }
    
    /**
     * Call this method to start the active scheduling of this task.
     */
    public void startMeasuring()
    {
        if ( !isTakingMeasurements )
        {
            isTakingMeasurements = true;
            measurementScheduler.startMeasurementTask( this );
        }
    }
    
    /**
     * Call this method to stop the scheduled execution of this task.
     */
    public void stopMeasuring()
    {
        if ( isTakingMeasurements )
        {
            timedMeasurementTask.cancel();
            measurementScheduler.removeMeasurementTask( this );
            isTakingMeasurements = false;
        }
    }
    
    /**
     * Causes the task the run its measurement activity. It is not recommended to call this method; 
     * clients should leave this up to the MeasurementScheduler.
     */
    public void executeMeasurement()
    {
        if ( isTakingMeasurements )
        {
            switch ( taskRecurrance )
            {
                case FINITE   : reoccurCount--; break;
                case INFINITE : reoccurCount++; break;
            }
            
            // Create empty MeasurementSet clone ready to populate with data
            MeasurementSet msClone = new MeasurementSet( measurementSet, false );
            
            Report report = new Report();
            report.setMeasurementSet( msClone );

            measurementListener.takeMeasure( report );
            measurementScheduler.storeMeasurement( report );
        }
    }
    
  
    // Protected methods -------------------------------------------------------    
    protected TimerTask getTimerTask()
    { return timedMeasurementTask; }
    
    // Private methods/classes -------------------------------------------------
    private class TimedMeasurementTask extends TimerTask
    {
        public TimedMeasurementTask()
        { super(); }

        @Override
        public void run()
        { executeMeasurement(); }
    }   
}
