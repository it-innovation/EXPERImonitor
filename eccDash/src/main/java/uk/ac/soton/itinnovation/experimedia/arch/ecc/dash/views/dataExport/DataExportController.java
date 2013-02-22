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
//      Created Date :          21-Feb-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.dataExport;

import com.vaadin.Application;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.servlet.ServletContext;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IExperimentMonitor;
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.mvc.IUFView;
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFAbstractEventManager;




public class DataExportController extends UFAbstractEventManager
                                  implements DataExportViewListener
{
  private DataExportView exportView;
  
  private transient Logger                          dataExportLogger = Logger.getLogger( DataExportController.class );
  private transient IExperimentMonitor              expMonitor;
  private transient IReportDAO                      reportDAO;
  private transient HashMap<UUID, MetricExportInfo> metricExportsByMSID;
  
  private transient Application  dashApplication;
  private transient String       dashFilePath;
  private transient FileResource exportMetaDataFile;
  
  public DataExportController()
  {
    super();
    
    metricExportsByMSID = new HashMap<UUID, MetricExportInfo>();
    
    createView();
  }
  
  public void initialise( IExperimentMonitor expMon, IReportDAO dao )
  {
    expMonitor = expMon;
    reportDAO  = dao;
    
    // Get export resources
    Component comp            = (Component) exportView.getImplContainer();
    dashApplication           = comp.getApplication();
    WebApplicationContext wac = (WebApplicationContext) dashApplication.getContext();
    ServletContext        sc  = wac.getHttpSession().getServletContext();
    
    dashFilePath = sc.getRealPath( "/WEB-INF" );
  }
  
  public void reset()
  {
    onClearAllExports();
  }
  
  public void shutDown()
  {
    
  }
  
  public IUFView getExportView()
  { return exportView; }
  
  // DataExportViewListener ----------------------------------------------------
  @Override
  public void onAddAllClientData()
  {
    String problem   = null;
   
    int addItemCount = addAllClientMetricsInfo();    
    
    if ( addItemCount > 0 )
    {
      exportView.clearExportItems();
      
      Iterator<MetricExportInfo> infoIt = metricExportsByMSID.values().iterator();
      while ( infoIt.hasNext() )
      {
        MetricExportInfo info = infoIt.next();
        
        exportView.addExportItem( info.clientName,    info.entityName,
                                  info.attributeName, info.msID,
                                  info.unit,          info.totalMetrics );
      }
      
      exportView.setDownloadEnabled( true );
      
      exportView.displayMessage( "Added client data for export",
                                 "Found " + addItemCount + " items" );
    }
    else exportView.displayMessage( "Could not add client data to export", problem );
  }
  
  @Override
  public void onClearAllExports()
  {
    metricExportsByMSID.clear();
    exportMetaDataFile = null;
    exportView.resetView();
  }
  
  @Override
  public void onExportData()
  {
    if ( !metricExportsByMSID.isEmpty() )
    {
      exportView.displayMessage( "Creating metric file", "Please wait... this make take a measurable period of time" );
      exportView.setDownloadEnabled( false );
      
      if ( generateMetaFile() )
      {
        exportView.setMetaInfoDownloadResource( exportMetaDataFile );
        exportView.displayMessage( "Ready to download metric meta-data", "Please click download link" );
      }
      else exportView.displayWarning( "Could not generate metric file", "Please check ECC configuration" );
    }
    else exportView.displayWarning( "Could not export metric data",
                                    "No metric data has been selected" );
    
    exportView.setDownloadEnabled( true );
  }
  
  // Private methods -----------------------------------------------------------
  private void createView()
  {
    exportView = new DataExportView();
    exportView.addListener( this );
  }
  
  private int addAllClientMetricsInfo()
  {
    Set<EMClient> clients = expMonitor.getAllConnectedClients();
    int metricCount = 0;
    
    if ( !clients.isEmpty() )
    {
      Iterator<EMClient> clientIt = clients.iterator();
      while ( clientIt.hasNext() )
      {
        EMClient client = clientIt.next();
        Set<MetricGenerator> mGens = client.getCopyOfMetricGenerators();

        Iterator<Entity> entIt = MetricHelper.getAllEntities( mGens ).values().iterator();
        while( entIt.hasNext() )
        {
          Entity entity = entIt.next();
          Iterator<Attribute> attIt = entity.getAttributes().iterator();
          while ( attIt.hasNext() )
          {
            Attribute attribute = attIt.next();
            
            Map<UUID, MeasurementSet> mSets = 
                MetricHelper.getMeasurementSetsForAttribute( attribute, mGens );
            
            Iterator<MeasurementSet> msIt = mSets.values().iterator();
            while ( msIt.hasNext() )
            {
              MeasurementSet ms = msIt.next();
              UUID msID = ms.getUUID();
              
              if ( !metricExportsByMSID.containsKey(msID) )
              {
                try
                {
                  Report rep = reportDAO.getReportForAllMeasurements( msID, false );
                  int numOfMeasures = rep.getNumberOfMeasurements();
                  
                  MetricExportInfo info = new MetricExportInfo( client.getName(),
                                                                entity.getName(),
                                                                attribute.getName(),
                                                                msID,
                                                                ms.getMetric().getUnit().getName(),
                                                                Integer.toString(numOfMeasures) );
                
                  metricExportsByMSID.put( msID, info );
                  metricCount++;
                }
                catch ( Exception e )
                { dataExportLogger.warn( "Data export problem: could not create report for MS: " + msID.toString() ); }
              }
            }
          }
        }        
      }
    }
    
    return metricCount;
  }
  
  private boolean generateMetaFile()
  {
    exportMetaDataFile = new FileResource( new File( dashFilePath + "/ExportMetaData.csv"),
                                           dashApplication );
    
    File expFile = exportMetaDataFile.getSourceFile();
    
    try
    {
      FileWriter     fw = new FileWriter( expFile );
      BufferedWriter bw = new BufferedWriter( fw );
      
      Iterator<MetricExportInfo> infoIt = metricExportsByMSID.values().iterator();
      while ( infoIt.hasNext() )
      {
        MetricExportInfo info = infoIt.next();
        bw.write( info.clientName + "," +
                  info.entityName + "," +
                  info.attributeName + "," +
                  info.unit + "," +
                  info.msID.toString() + "," +
                  info.totalMetrics );
      }
      
      bw.close();
      fw.close();
      
      return true;
    }
    catch ( Exception e )
    { exportView.displayWarning( "Could not generate metric file", e.getMessage() ); }
    
    return false;
  }
  
  private void streamMetricFile()
  {
    
  }
  
  // Private classes -----------------------------------------------------------
  private class MetricExportInfo
  {
    final String clientName;
    final String entityName;
    final String attributeName;
    final UUID   msID;
    final String unit;
    final String totalMetrics;
    
    public MetricExportInfo( String cN, String eN, String aN,
                             UUID   id, String u,  String tM )
    {
      clientName    = cN;
      entityName    = eN;
      attributeName = aN;
      msID          = id;
      unit          = u;
      totalMetrics  = tM;
    }
  }
}
