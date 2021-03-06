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
//      Created Date :          10-Oct-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IReportDAO;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;




/**
 * ECC client writers should consider using the MeasurementScheduler as a utility to
 * assist them in regularly taking metric measurements. A properly configured EDMAgent
 * is required for the scheduler to run, as it uses an instance of IReportDAO to store 
 * the measurements it receives from ITakeMeasurement instances.
 * 
 * @author Simon Crowle
 */
public class MeasurementScheduler
{
	private final Logger schedulerLogger = LoggerFactory.getLogger(getClass());
    private final Object taskLock            = new Object();
    private final Object edmLock             = new Object();
  
    private IReportDAO edmReportDAO;
    private boolean    initialisedOK = false;
    
    private Timer         scheduler;
    private HashSet<UUID> scheduledMeasurementsByID;
    private boolean       schedulerActive = false;
    
    public final static int MINIMUM_MEASUREMENT_INTERVAL_PERIOD = 100;
    
  
    public MeasurementScheduler()
    {}
    
    /**
     * The scheduler must be initialised with a valid EDMAgent instance before it is
     * able to operate correctly.
     * 
     * @param edmAgent   - EDMAgent to use for storing metric data.
     * @throws Exception - Will throw if the scheduler is already initialised or the EDM agent is null.
     */
    public void initialise( IMonitoringEDMAgent edmAgent ) throws Exception
    {
        initialisedOK = false;
        
        // Safety first
        if ( initialisedOK )    throw new Exception( "Already initialised" );
        if ( edmAgent == null ) throw new Exception( "EDM is NULL" );

        edmReportDAO              = edmAgent.getReportDAO();
        scheduler                 = new Timer();
        scheduledMeasurementsByID = new HashSet<UUID>();
        
        initialisedOK = true;
    }
    
    /**
     * Client writers should create new MeasurementTasks using this method.
     * 
     * @param measurementSet - The MeasurementSet instance associated with the measurement task.
     * @param listener       - An instance of ITakeMeasurement that actually generates the metric data on demand
     * @param repetitions    - The number of times this task should be carried out (-1 for infinite, 0 for just once, or a positive integer for finite)
     * @param intervalMS     - The elapsed time (in milliseconds) between each successive task execution
     * @return               - Returns an instance of MeasurementTask representing a scheduled task
     * @throws Exception     - Will throw if scheduler is not initialised properly; the MeasurementSet is null or the MeasurementSet already has an associated task.
     */
    public MeasurementTask createMeasurementTask( MeasurementSet   measurementSet,
                                                  ITakeMeasurement listener,
                                                  int repetitions,
                                                  long intervalMS ) throws Exception
    {
        // Safety first
        if ( !initialisedOK )         throw new Exception( "Have not initialised OK" );
        if ( measurementSet == null ) throw new Exception( "MeasurementSet is NULL" );
        
        if ( intervalMS < MINIMUM_MEASUREMENT_INTERVAL_PERIOD )
            throw new Exception( "Measurement interval period is too small" );
        
        if ( scheduledMeasurementsByID.contains(measurementSet.getID()) )
          throw new Exception( "MeasurementSet ID already has a task" );
        
        synchronized ( taskLock )
        { scheduledMeasurementsByID.add( measurementSet.getID() ); }
        
        return new MeasurementTask( this, listener, 
                                    measurementSet, repetitions,
                                    intervalMS );
    }
    
    // Protected methods -------------------------------------------------------
    protected void removeMeasurementTask( MeasurementTask task )
    {
        if ( task != null )
        {
            synchronized ( taskLock )
            { scheduledMeasurementsByID.remove( task.getMeasurementSetID() ); }
        }
    }
    
    protected void storeMeasurement( Report report )
    {
        if ( report != null )
        {
            synchronized( edmLock )
            {
              try { edmReportDAO.saveMeasurements(report); }
              catch ( Exception e )
              { schedulerLogger.error( "EDM save report problem: " + e.getMessage() ); }
            }
        }
    }
    
    protected void startMeasurementTask( MeasurementTask task )
    {
        if ( task != null )
        {
            scheduler.schedule( task.getTimerTask(),
                                0, task.getMeasurementInterval() );
        }
    }
}
