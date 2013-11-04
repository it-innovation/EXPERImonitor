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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.visualizers.metrics.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.visualizers.prov.PROVDOTGraphBuilder;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.PROVToolBoxUtil;

import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.mvc.IUFView;
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFAbstractEventManager;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import org.vaadin.artur.icepush.ICEPush;


import java.io.PrintWriter;
import java.util.*;




public class LiveMonitorController extends UFAbstractEventManager
                                   implements LiveMetricViewListener
{
  private final IECCLogger liveMonLogger  = Logger.getLogger( LiveMonitorController.class );
  private final Object     updateViewLock = new Object();

  private LiveDataView   liveDataView;
  private LiveMetricView liveMetricView;
  private LiveProvView   liveProvView;

  private transient HashMap<UUID, MSUpdateInfo> measurementUpdates;
  private transient HashSet<UUID>               activeMSVisuals;
  private transient IReportDAO                  expReportAccessor;
  private transient ICEPush                     icePusher;

  private transient EDMProvReport aggregatedPROVReport;


  public LiveMonitorController( ICEPush pusher )
  {
    super();

    icePusher            = pusher;
    measurementUpdates   = new HashMap<UUID, MSUpdateInfo>();
    activeMSVisuals      = new HashSet<UUID>();
    aggregatedPROVReport = new EDMProvReport();

    createViews();
  }

  public void initialse( IReportDAO dao )
  { expReportAccessor = dao; }

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
        synchronized ( updateViewLock )
        {
          UUID msID = report.getMeasurementSet().getID();
          if ( activeMSVisuals.contains(msID) )
          {
            MeasurementSet ms = report.getMeasurementSet();
            liveMetricView.updateMetricVisual( msID, ms );

            if ( icePusher !=null ) icePusher.push();
          }
        }
    }
  }

  public void processLivePROVData( EMClient client, EDMProvReport report ) throws Exception
  {
    if ( client == null || report == null ) throw new Exception( "Live monitoring provenance parameters were null" );

    aggregatedPROVReport = PROVToolBoxUtil.aggregateReport( aggregatedPROVReport, report );

    liveProvView.echoPROVData( aggregatedPROVReport );

    // TO REMOVE LATER WITH JUNG VISUALISATION ---------------------------------
    try
    {
      Component comp      = (Component) liveProvView.getImplContainer();
      Application thisApp = comp.getApplication();
      String basePath     = thisApp.getContext().getBaseDirectory().getAbsolutePath();

      PROVDOTGraphBuilder gb = new PROVDOTGraphBuilder();
      String dotAsString = gb.createDOT(aggregatedPROVReport);

      PrintWriter out = new PrintWriter(basePath + "/" + "dotViz.dot");
      out.print(dotAsString);
      out.close();

      liveProvView.renderPROVVizFile( basePath, "dotViz" );
    }
    catch ( Exception ex )
    { liveMonLogger.error( "Could not create PROV visualisation", ex ); }
    // --------------------------------- TO REMOVE LATER WITH JUNG VISUALISATION


    if ( icePusher !=null ) icePusher.push();
  }

  public IUFView getLiveView()
  { return liveDataView; }

  public void reset()
  {
    activeMSVisuals.clear();
    liveMetricView.resetView();
  }

  public void shutDown()
  {
    icePusher = null;
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
            synchronized ( updateViewLock )
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

        synchronized ( updateViewLock )
        {
          while ( msIt.hasNext() )
          {
            UUID msID = msIt.next().getID();

            activeMSVisuals.remove( msID );
            liveMetricView.removeMetricVisual( msID );
          }

          if ( icePusher !=null ) icePusher.push();
        }
      }
    }
  }

  // LiveMonitorViewListener ---------------------------------------------------
  @Override
  public void onRemoveVisualClicked( UUID msID )
  {
    if ( msID != null )
    {
      synchronized ( updateViewLock )
      {
        activeMSVisuals.remove( msID );
        liveMetricView.removeMetricVisual( msID );

        if ( icePusher !=null ) icePusher.push();
      }
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
}
