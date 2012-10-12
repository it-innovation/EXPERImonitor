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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDMLight;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;
import org.apache.log4j.Logger;




public class MeasurementScheduler
{
    private final Logger schedulerLogger = Logger.getLogger( MeasurementScheduler.class );
    private final Object taskLock        = new Object();
    private final Object edmLock         = new Object();
  
    private IReportDAO edmReportDAO;
    private boolean    initialisedOK = false;
    
    private Timer         scheduler;
    private HashSet<UUID> scheduledMeasurementsByID;
    private boolean       schedulerActive = false;
    
    public final static int MINIMUM_MEASUREMENT_INTERVAL_PERIOD = 100;
    
  
    public MeasurementScheduler()
    {}
    
    public void initialise( IMonitoringEDMLight edmLight ) throws Exception
    {
        // Safety first
        if ( initialisedOK )    throw new Exception( "Already initialised" );
        if ( edmLight == null ) throw new Exception( "EDM is NULL" );

        edmReportDAO              = edmLight.getReportDAO();
        scheduler                 = new Timer();
        scheduledMeasurementsByID = new HashSet<UUID>();
        
        initialisedOK = true;
    }
    
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
        
        if ( scheduledMeasurementsByID.contains(measurementSet.getUUID()) )
          throw new Exception( "MeasurementSet ID already has a task" );
        
        synchronized ( taskLock )
        { scheduledMeasurementsByID.add( measurementSet.getUUID() ); }
        
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
              schedulerLogger.info( "Saving MS: " + report.getMeasurementSet().getUUID().toString() + 
                                    " at time " + report.getToDate().toString() );
              
              //try { edmReportDAO.saveReport(report); }
              //catch ( Exception e )
              //{ schedulerLogger.error( "EDM save report problem: " + e.getMessage() ); }
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
