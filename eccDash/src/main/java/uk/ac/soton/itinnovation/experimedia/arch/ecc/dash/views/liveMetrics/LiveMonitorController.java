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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers.BaseMetricVisual;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers.NominalValuesSnapshotVisual;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers.NumericTimeSeriesVisual;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers.RawDataVisual;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.mvc.IUFView;
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFAbstractEventManager;




public class LiveMonitorController extends UFAbstractEventManager
                                   implements LiveMonitorViewListener
{
  private LiveMonitorView liveMonitorView;
  
  private transient HashMap<UUID, Date> measurementDateStamps;
  private transient HashSet<UUID>       activeMSVisuals;
  private transient IReportDAO          expReportAccessor;

  
  public LiveMonitorController()
  {
    super();
    
    measurementDateStamps = new HashMap<UUID, Date>();
    activeMSVisuals       = new HashSet<UUID>();
    
    createView();
  }
  
  public void initialse( IReportDAO dao )
  { expReportAccessor = dao; }
  
  public void processLiveData( EMClient client, Report report ) throws Exception
  {
    // Safety first
    if ( report == null || client == null ) throw new Exception( "Live monitoring parameters were null" );
    if ( expReportAccessor == null ) throw new Exception( "Live monitoring control has not been initialised" );
    
    // Remove all measurements that we already have
    removeOldMeasurements( report );
    
    // Check to see if we have anything to store, and try store
    if ( report.getNumberOfMeasurements() > 0 )
    {
      try
      { expReportAccessor.saveMeasurements( report ); }
      catch ( Exception e )
      { throw e; }
    }
    
    // Display (if we have an active display)
    UUID msID = report.getMeasurementSet().getID();
    if ( activeMSVisuals.contains(msID) )
    {
      MeasurementSet ms = report.getMeasurementSet();
      liveMonitorView.updateMetricVisual( msID, ms );
    }
  }
  
  public IUFView getLiveView()
  { return liveMonitorView; }
  
  public void reset()
  {
    activeMSVisuals.clear();
    liveMonitorView.resetView();
  }
  
  public void shutDown()
  {
    
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
              visual = new RawDataVisual( attribute.getName(),
                                          metric.getUnit().getName(),
                                          metric.getMetricType().name(),
                                          msID ); break;               
             
            
            case INTERVAL:
            case RATIO   :
              visual = new NumericTimeSeriesVisual( attribute.getName(),
                                                    metric.getUnit().getName(),
                                                    metric.getMetricType().name(),
                                                    msID ); break;
          }
          
          if ( visual != null )
          {
            liveMonitorView.addMetricVisual( client.getName(),
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
        while ( msIt.hasNext() )
        {
          UUID msID = msIt.next().getID();
          
          activeMSVisuals.remove( msID );
          liveMonitorView.removeMetricVisual( msID ); 
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
      activeMSVisuals.remove( msID );
      liveMonitorView.removeMetricVisual( msID ); 
    }
  }
  
  // Private methods -----------------------------------------------------------
  private void createView()
  {
    liveMonitorView = new LiveMonitorView();
    liveMonitorView.addListener( this );
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

          // Get latest measurement (if one does not exist, create an old start date)
          Date lastRecent = measurementDateStamps.get( msID );
          if ( lastRecent == null )
          {
            lastRecent = new Date();
            lastRecent.setTime( 0 );
          }
          
          Date mostRecent = lastRecent;
          
          // Find old measurements (if any)
          Iterator<Measurement> mIt = targetMeasurements.iterator();
          while( mIt.hasNext() )
          {
            Measurement m = mIt.next();
            Date mDate    = m.getTimeStamp();
            
            if ( mDate.before(mostRecent) ) // It's an old measurement
              oldMeasurements.add( m );
            else
              if ( mDate.after(mostRecent) ) // It's the most recent (of all) measurements
                mostRecent = mDate;
          }
          
          // Remove old measurements
          mIt = oldMeasurements.iterator();
          while ( mIt.hasNext() )
            targetMeasurements.remove( mIt.next() );
          
          // Update measurement count and recency
          report.setNumberOfMeasurements( targetMeasurements.size() );
          measurementDateStamps.remove( msID );
          measurementDateStamps.put( msID, mostRecent );
        }
        else report.setNumberOfMeasurements( 0 );
      }
      else report.setNumberOfMeasurements( 0 );
    }
  }
}
