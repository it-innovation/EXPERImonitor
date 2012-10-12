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
    
    public UUID getMeasurementSetID()
    { return measurementSet.getUUID(); }
    
    public eTaskRecurrance getTaskRecurranceType()
    { return taskRecurrance; }
    
    public int getRecurrances()
    { return reoccurCount; }
    
    public long getMeasurementInterval()
    { return intervalMilliSecs; }
    
    public boolean isMeasuring()
    { return isTakingMeasurements; }
    
    public void startMeasuring()
    {
        if ( !isTakingMeasurements )
        {
            isTakingMeasurements = true;
            measurementScheduler.startMeasurementTask( this );
        }
    }
    
    public void stopMeasuring()
    {
        if ( isTakingMeasurements )
        {
            timedMeasurementTask.cancel();
            measurementScheduler.removeMeasurementTask( this );
            isTakingMeasurements = false;
        }
    }
  
    // Protected methods -------------------------------------------------------
    protected MeasurementTask( MeasurementScheduler scheduler,
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
    
    protected void executeMeasurement()
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

            Report report = measurementListener.takeMeasure( msClone );
            measurementScheduler.storeMeasurement( report );
        }
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        stopMeasuring();
        super.finalize();
    }
    
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
