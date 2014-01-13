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
//      Created Date :          06-Feb-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveData;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;

import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.mvc.IUFView;
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFAbstractEventManager;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UIPushManager;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.visualizers.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IReportDAO;


import java.io.*;
import java.util.*;






public class LiveMonitorController extends UFAbstractEventManager
                                   implements LiveMetricViewListener
{
  private final IECCLogger liveMonLogger        = Logger.getLogger( LiveMonitorController.class );
  private final Object     metricViewUpdateLock = new Object();
  private final Object     provViewUpdateLock   = new Object();
  private final int        liveUpdateRate       = 2500;

  private LiveDataView   liveDataView;
  private LiveMetricView liveMetricView;
  private LiveProvView   liveProvView;

  private transient HashMap<UUID, MSUpdateInfo> measurementUpdates;
  private transient HashSet<UUID>               activeMSVisuals;
  private transient IReportDAO                  expReportAccessor;
  private transient UIPushManager               pushManager;

  private transient PROVFileLogger provLogger;
  private transient boolean        provUpdatePending;
  
  private Timer liveUpdateTimer; 


  public LiveMonitorController( UIPushManager pushMgr )
  {
    super();

    pushManager        = pushMgr;
    measurementUpdates = new HashMap<UUID, MSUpdateInfo>();
    activeMSVisuals    = new HashSet<UUID>();
    provLogger         = new PROVFileLogger();

    createViews();
  }

  public void initialse( IReportDAO dao )
  { 
    expReportAccessor = dao;
    
    // Set up live update callback
    liveUpdateTimer = new Timer();
    
    LiveUpdateCallback luc = new LiveUpdateCallback();
    liveUpdateTimer.scheduleAtFixedRate( luc, liveUpdateRate, liveUpdateRate );
  }
  
  public void createPROVLog( UUID expID, String basePath )
  {
    // Close last log (if it exists)
    if ( provLogger.isLogging() ) provLogger.closeLog();
    
    // Create a new log
    if ( !provLogger.createLog(expID, basePath) )
    {
      String problem = "Could not create PROV log file for experiment " + expID.toString();
      liveMetricView.displayWarning( "PROV Logging error", problem );
      
      liveMonLogger.error( problem );
    }
  }
  
  public void closePROVLog()
  {
    provLogger.closeLog();
  }

  public void processLiveMetricData( EMClient client, Report report ) throws Exception
  {
    // Safety first
    if ( report == null || client == null ) throw new Exception( "Live monitoring metric parameters were null" );
    if ( expReportAccessor == null ) throw new Exception( "Live monitoring control has not been initialised" );

    // Check to see if we have anything useful store, and try store
    if ( sanitiseMetricReport(client, report) )
    {
      try
      { expReportAccessor.saveMeasurements( report ); }
      catch ( Exception e ) { throw e; }

      // Remove measurements we've already displayed 'live'
      removeOldMeasurements( report );

      // Display (if we have an active display and there is still data)
      if ( report.getNumberOfMeasurements() > 0 )
        synchronized ( metricViewUpdateLock )
        {
          UUID msID = report.getMeasurementSet().getID();
          if ( activeMSVisuals.contains(msID) )
          {
            MeasurementSet ms = report.getMeasurementSet();
            liveMetricView.appendMetricData( msID, ms );
          }
        }
    }
  }

  public void processLivePROVData( EMClient client, EDMProvReport report ) throws Exception
  {
    if ( client == null || report == null ) throw new Exception( "Live monitoring provenance parameters were null" );

    synchronized ( provViewUpdateLock )
    {
      if ( provLogger.isLogging() )
      {
        provLogger.writePROV( report );
				liveProvView.echoPROVData( report );
				
        provUpdatePending = true;
      }
      else
      {
        liveMonLogger.error( "Unable to log PROV data" );
      }
    }
  }

  public IUFView getLiveView()
  { return liveDataView; }

  public void reset()
  {
    synchronized( metricViewUpdateLock )
    {
      activeMSVisuals.clear();
      liveMetricView.resetView();
    }
    
    synchronized( provViewUpdateLock )
    {
      liveProvView.resetView();
      provLogger.closeLog();
      provUpdatePending = false;
    }
  }

  public void shutDown()
  {
    reset();
    
    liveUpdateTimer.cancel();
    liveUpdateTimer.purge();
    
    pushManager = null;
    
    provLogger.closeLog();
  }

  public void addliveView( EMClient client, Entity entity, Attribute attribute,
                           Collection<MeasurementSet> mSets )
  {
    if ( client != null && attribute != null && mSets != null )
    {
      Iterator<MeasurementSet> msIt = mSets.iterator();
      while ( msIt.hasNext() )
      {
        MeasurementSet ms       = msIt.next();
        UUID msID               = ms.getID();
        Metric metric           = ms.getMetric();
        BaseMetricVisual visual = null;

        if ( !activeMSVisuals.contains(msID) )
        {
          switch ( ms.getMetric().getMetricType() )
          {
            case NOMINAL :
              visual = new NominalValuesSnapshotVisual( attribute.getName(),
                                                        metric.getUnit().getName(),
                                                        metric.getMetricType().name(),
                                                        msID ); break;

            case ORDINAL :
            case INTERVAL:
              visual = new RawDataVisual( attribute.getName(),
                                          metric.getUnit().getName(),
                                          metric.getMetricType().name(),
                                          msID ); break;

            case RATIO   :
              visual = new NumericTimeSeriesVisual( attribute.getName(),
                                                    metric.getUnit().getName(),
                                                    metric.getMetricType().name(),
                                                    msID ); break;
          }

          if ( visual != null )
            synchronized ( metricViewUpdateLock )
            {
              liveMetricView.addMetricVisual( client.getName(),
                                               entity.getName(),
                                               attribute.getName(),
                                               msID, visual );
              activeMSVisuals.add( msID );
            }
        }
      }
    }
  }

  public void removeClientLiveView( EMClient client )
  {
    if ( client != null )
    {
      // Remove all known measurement sets from view
      Set<MetricGenerator> msGens = client.getCopyOfMetricGenerators();
      if ( !msGens.isEmpty() )
      {
        Map<UUID, MeasurementSet> mSets = MetricHelper.getAllMeasurementSets( msGens );
        Iterator<MeasurementSet> msIt = mSets.values().iterator();

        synchronized ( metricViewUpdateLock )
        {
          while ( msIt.hasNext() )
          {
            UUID msID = msIt.next().getID();

            activeMSVisuals.remove( msID );
            liveMetricView.removeMetricVisual( msID );
          }
        }
        
        // Removal could be the result of a client disconnection, to push an update
        pushManager.pushUIUpdates();
      }
    }
  }

  // LiveMonitorViewListener ---------------------------------------------------
  @Override
  public void onRemoveVisualClicked( UUID msID )
  {
    if ( msID != null )
    {
      synchronized ( metricViewUpdateLock )
      {
        activeMSVisuals.remove( msID );
        liveMetricView.removeMetricVisual( msID );
      }
      
      if ( pushManager !=null ) pushManager.pushUIUpdates();
    }
  }

  // Private methods -----------------------------------------------------------
  private void createViews()
  {
    liveDataView = new LiveDataView();

    liveMetricView = liveDataView.getLiveMetricView();
    liveMetricView.addListener( this );

    liveProvView = liveDataView.getLiveProvView();
    liveProvView.addListener( this );
  }

  private void removeOldMeasurements( Report report )
  {
    if ( report != null )
    {
      MeasurementSet ms = report.getMeasurementSet();

      if ( ms != null )
      {
        Set<Measurement> targetMeasurements = ms.getMeasurements();
        if ( targetMeasurements != null && !targetMeasurements.isEmpty() )
        {
          // Measurements we're going to ditch (because we've got them) to be recored here
          HashSet<Measurement> oldMeasurements = new HashSet<Measurement>();
          UUID msID = ms.getID();

          // Get latest measurement (if one does not exist, create a dummy measurement)
          MSUpdateInfo lastRecent = measurementUpdates.get( msID );
          if ( lastRecent == null )
          {
            Date date = new Date();
            date.setTime( 0 );

            lastRecent = new MSUpdateInfo( date, UUID.randomUUID() );
          }

          MSUpdateInfo mostRecentInfo = lastRecent;

          // Find old measurements (if any)
          Iterator<Measurement> mIt = targetMeasurements.iterator();
          while( mIt.hasNext() )
          {
            Measurement m = mIt.next();
            Date mDate    = m.getTimeStamp();

            if ( mostRecentInfo.lastUpdate.after(mDate) ||
                 mostRecentInfo.lastMeasurementID.equals(m.getUUID()) ) // If it is an old or repeated
              oldMeasurements.add( m );                                 // measurement, we don't want it
            else
              mostRecentInfo = new MSUpdateInfo( mDate, m.getUUID() );
          }

          // Remove old measurements
          mIt = oldMeasurements.iterator();
          while ( mIt.hasNext() )
            targetMeasurements.remove( mIt.next() );

          // Update measurement count and recency
          report.setNumberOfMeasurements( targetMeasurements.size() );
          measurementUpdates.remove( msID );
          measurementUpdates.put( msID, mostRecentInfo );
        }
        else report.setNumberOfMeasurements( 0 );
      }
      else report.setNumberOfMeasurements( 0 );
    }
  }

  private boolean sanitiseMetricReport( EMClient client, Report reportOUT )
  {
    // Check that we apparently have data
    if ( reportOUT.getNumberOfMeasurements() == 0 )
    {
      liveMonLogger.error( "Metric report error: measurement count = 0" );
      return false;
    }

    // Make sure we have a valid measurement set
    MeasurementSet clientMS = reportOUT.getMeasurementSet();
    if ( clientMS == null )
    {
      liveMonLogger.error( "Metric report error: Measurement set is null" );
      return false;
    }

    Metric metric = clientMS.getMetric();
    if ( metric == null )
    {
      liveMonLogger.error( "Metric report error: Metric is null" );
      return false;
    }

    MetricType mt = metric.getMetricType();

    // Sanitise data based on full semantic info
    MeasurementSet cleanSet = new MeasurementSet( clientMS, false );

    // Run through each measurement checking that it is sane
    for ( Measurement m : clientMS.getMeasurements() )
    {
      String val = m.getValue();

      switch ( mt )
      {
        case NOMINAL:
        case ORDINAL:
          if ( val != null && !val.isEmpty() ) cleanSet.addMeasurement( m ); break;

        case INTERVAL:
        case RATIO:
        {
          if ( val != null )
          {
            try
            {
              // Make sure we have a sensible number
              Double dVal = Double.parseDouble(val);

              if ( !dVal.isNaN() && !dVal.isInfinite() )
                cleanSet.addMeasurement( m );
            }
            catch( Exception ex ) { /*Not INTERVAL OR RATIO, so don't include*/ }
          }
        } break;
      }
    }

    // Use update report with clean measurement set
    reportOUT.setMeasurementSet( cleanSet );
    reportOUT.setNumberOfMeasurements( cleanSet.getMeasurements().size() );

    return true;
  }
  
  private void updateLiveViews()
  {
    boolean pushRequired = false;
    
    synchronized ( metricViewUpdateLock )
    {
      if ( !activeMSVisuals.isEmpty() )
      {
        liveMetricView.updateView();
        pushRequired = true;
      }
    }
    
    synchronized( provViewUpdateLock )
    {
      if ( provUpdatePending )
      {
        // TODO: Update tail of PROV reports to UI
        
        provUpdatePending = false;
      }
    }
    
    // Push UI updates, if required
    if ( pushManager !=null && pushRequired ) pushManager.pushUIUpdates();
  }
  
  // Private classes -----------------------------------------------------------
  private class MSUpdateInfo
  {
    final Date lastUpdate;
    final UUID lastMeasurementID;

    public MSUpdateInfo( Date update, UUID measurementID )
    {
      lastUpdate = update;
      lastMeasurementID = measurementID;
    }
  }
  
  public class LiveUpdateCallback extends TimerTask
  {
    @Override
    public void run()
    { updateLiveViews(); }
  }
  
  private class PROVFileLogger
  {
    private FileWriter     provFW;
    private BufferedWriter provBW;
    
    private boolean isLoggingPROV;
    
    
    public PROVFileLogger()
    {}
    
    public boolean createLog( UUID expID, String basePath )
    {
      isLoggingPROV = false;
      
      // An old log may exist, make sure we close it
      closeLog();
      
      if ( expID != null && basePath != null )
      {
        String provBasePath = basePath + "/provLogs";
        
        if ( createPROVDirectory(provBasePath) )
        {
          String provLog = provBasePath + "/" + expID.toString() + ".txt";
          
          if ( createPROVFile(provLog) )
            isLoggingPROV = true;
          else
            liveMonLogger.error( "Could not create PROV log: cannot create PROV file" );
        }
        else
          liveMonLogger.error( "Could not create PROV log: cannot create PROV directory" );
      }
      else
        liveMonLogger.error( "Could not create PROV log: experiment ID/path is null" );
      
      return isLoggingPROV;
    }
    
    public boolean isLogging()
    { return isLoggingPROV; }
    
    public void closeLog()
    {
      if ( isLoggingPROV )
      {
        try
        {
          provFW.flush();
          provBW.close();
          
          provBW   = null;
          provFW   = null;
          
          isLoggingPROV = false;
        }
        catch ( IOException ex )
        {
          liveMonLogger.error( "Could not close PROV log: " + ex.getMessage() );
        }
      }
    }
    
    public boolean writePROV( EDMProvReport report )
    {
      boolean result = false;
      
      if ( isLoggingPROV && report != null )
      {
        Collection<EDMTriple> triples = report.getTriples().values();
        
        try
        {
          for ( EDMTriple triple : triples )
          {
            provBW.write( triple.toString() );
            provBW.newLine();
          }
                    
          result = true;
          
        }
        catch ( IOException ex )
        {
          liveMonLogger.error( "Problems writing triple data: " + ex.getMessage() );
        }
      }
      else
        liveMonLogger.error( "Could not write PROV log: not ready to log" );
      
      return result;
    }
    
    // Private methods ---------------------------------------------------------
    private boolean createPROVDirectory( String basePath )
    {
      boolean result = false;
      
      // If PROV diretory does not exist, create it
      File provDir = new File( basePath );
      if ( provDir.exists() && provDir.isDirectory() )
        result = true;
      else
      {
        if ( provDir.mkdir() )
          result = true;
        else
        { liveMonLogger.error( "Could not create PROV logging directory" ); }
      }
        
      return result;
    }
    
    private boolean createPROVFile( String provPath )
    {
      boolean result = false;
      
      // Try creating a new PROV file
      File provFile = new File( provPath );
      
      // Do not overwrite old experiment data
      if ( !provFile.exists() )
      {
        try
        {
          provFile.createNewFile();

          provFW = new FileWriter( provFile.getAbsoluteFile() );
          provBW = new BufferedWriter( provFW );

          result = true;
        }
        catch ( IOException ex )
        {
          liveMonLogger.error( "Could not create new PROV log: " + ex.getMessage() );
        }
      }
      else // Do not overwrite old experiment log data!
        liveMonLogger.error( "Will not overwrite old PROV log - please create new experiment" );
      
      return result;
    }
  }
}
