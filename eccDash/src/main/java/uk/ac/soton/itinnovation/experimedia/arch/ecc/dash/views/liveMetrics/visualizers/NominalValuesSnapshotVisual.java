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
//      Created Date :          22-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers;

import com.invient.vaadin.charts.Color;
import com.invient.vaadin.charts.Color.RGB;
import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.CategoryAxis;
import com.invient.vaadin.charts.InvientChartsConfig.ColumnConfig;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.PieConfig;
import com.invient.vaadin.charts.InvientChartsConfig.PieDataLabel;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;

import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.UUID;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;




public class NominalValuesSnapshotVisual extends BaseMetricVisual
{
  // Barchart
  private InvientCharts        barchart;
  private InvientChartsConfig  barchartConfig;
  private LinkedHashSet<XAxis> barchartAxisSet;
  
  private InvientCharts       piechart;
  private InvientChartsConfig piechartConfig;
  
  // Internal data model
  private XYSeries                           nominalSeries;
  private XYSeries                           pieSeries;
  private transient TreeMap<String, Integer> nominalCounts;
  
  
  public NominalValuesSnapshotVisual( String title, String unit, String type, UUID msID )
  {
    super( msID );
    
    setTitle( title );
    setMetricInfo( unit, type );
    
    nominalCounts = new TreeMap<String, Integer>();
    
    createBarchartConfig();
    createPiechartConfig();
    createComponents();
  }
  
  @Override
  public void clearMeasurements()
  {
    nominalCounts.clear();
    
    super.clearMeasurements();
  }
  
  @Override
  public void addMeasurementData( MeasurementSet ms )
  {
    // Not going to visualise this as a time series, but as a current snapshot
    if ( ms != null )
    {
      // Run through nominals, updating totals as they are found
      for ( Measurement m : ms.getMeasurements() )
      {
        String nomValue = m.getValue();
        if ( nomValue != null )
        {
          // Update the count, if one already exists
          if ( nominalCounts.containsKey(nomValue) )
          {
            Integer count = nominalCounts.get( nomValue );
            nominalCounts.remove( nomValue );
            
            count++;
            nominalCounts.put( nomValue, count );
          }
          else
            nominalCounts.put( nomValue, 1 ); // First instance of this kind
        }
      }
      
      updateView();
    }
  }
  
  @Override
  public void setMaxMeasurements( int max )
  { /* Not very much we can do here, as nominal data is unordered */ }
  
  @Override
  public void updateView()
  {    
    if ( !nominalCounts.isEmpty() )
    {
      // Remove old series data
      if ( nominalSeries != null ) barchart.removeSeries( nominalSeries );
      if ( pieSeries != null ) piechart.removeSeries( pieSeries );
      
      // Create new nominal series
      nominalSeries = new XYSeries( (String) visualTitle.getValue() );
      for ( String nomVal : nominalCounts.keySet() )
        nominalSeries.addPoint( new DecimalPoint( nominalSeries, nominalCounts.get(nomVal) ) );
      
      // Create new pie series
      pieSeries = new XYSeries( (String) visualTitle.getValue() );
      for ( String nomVal : nominalCounts.keySet() )
        pieSeries.addPoint( new DecimalPoint( nominalSeries, nomVal, nominalCounts.get(nomVal)) );
    }
    
    // Re-create barchart
    if ( barchart != null )
    {
      // Re-create nominal axis
      ArrayList<String> cats = new ArrayList<String>();
      cats.addAll( nominalCounts.keySet() );
      
      CategoryAxis nomAxis = new CategoryAxis();
      nomAxis.setCategories( cats );
      
      // Revise axis set
      barchartAxisSet.clear();
      barchartAxisSet.add( nomAxis );
      barchartConfig.setXAxes( barchartAxisSet );
     
      barchart.addSeries( nominalSeries );
    }
    
    // Update piechart
    if ( piechart != null ) piechart.addSeries( pieSeries );
  }
  
  // Private methods -----------------------------------------------------------
  private void createBarchartConfig()
  {
    barchartConfig = new InvientChartsConfig();
    barchartConfig.getGeneralChartConfig().setType( SeriesType.COLUMN );
    barchartConfig.getGeneralChartConfig().setReflow( false );
    barchartConfig.getGeneralChartConfig().setBackgroundColor( new Color.RGB(242,242,242) );
    barchartConfig.getCredit().setEnabled( false );

    // Remove unwanted visual components
    InvientChartsConfig.Title title = new InvientChartsConfig.Title();
    title.setText( "" );
    barchartConfig.setTitle( title );
    
    InvientChartsConfig.Legend legend = new InvientChartsConfig.Legend();
    legend.setEnabled( false );
    barchartConfig.setLegend( legend );
    
    ColumnConfig colCfg = new ColumnConfig();
    colCfg.setPointPadding( 0.2 );
    colCfg.setBorderWidth( 0 );
    colCfg.setColor( new RGB(69,114,167) );
    colCfg.setAnimation( false );
    barchartConfig.addSeriesConfig( colCfg );
    
    barchartAxisSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
    CategoryAxis nomAxis = new CategoryAxis();
    barchartAxisSet.add(nomAxis);
    barchartConfig.setXAxes( barchartAxisSet );
    
    NumberYAxis yAxis = new NumberYAxis();
    yAxis.setMin(0.0);
    yAxis.setTitle(new AxisTitle( (String) visualTitle.getValue() ));
    LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
    yAxesSet.add(yAxis);
    barchartConfig.setYAxes( yAxesSet );
  }
  
  private void createPiechartConfig()
  {
    piechartConfig = new InvientChartsConfig();
    piechartConfig.getGeneralChartConfig().setType( SeriesType.PIE );
    piechartConfig.getGeneralChartConfig().setReflow( false );
    piechartConfig.getGeneralChartConfig().setBackgroundColor( new Color.RGB(242,242,242) );
    piechartConfig.getCredit().setEnabled( false );

    // Remove unwanted visual components
    InvientChartsConfig.Title title = new InvientChartsConfig.Title();
    title.setText( "" );
    piechartConfig.setTitle( title );
    
    InvientChartsConfig.Legend legend = new InvientChartsConfig.Legend();
    legend.setEnabled( false );
    piechartConfig.setLegend( legend );
    
    PieConfig pieCfg = new PieConfig();
    pieCfg.setAnimation( false );
    pieCfg.setAllowPointSelect( true );
    piechartConfig.addSeriesConfig( pieCfg );
  }
  
  private void createComponents()
  {    
    VerticalLayout vl = getViewContents();
    
    TabSheet ts = new TabSheet();
    ts.addStyleName( "borderless" );
    vl.addComponent( ts );
            
    if ( barchartConfig != null )
    {
      barchart = new InvientCharts( barchartConfig );
      barchart.setWidth( "600px" );
      barchart.setHeight( "400px" );
      
      ts.addTab( barchart, "bar" );
    }
    
    if ( piechartConfig != null )
    {
      piechart = new InvientCharts( piechartConfig );
      piechart.setWidth( "600px" );
      piechart.setHeight( "400px" );
      
      ts.addTab( piechart, "pie" );
    }
  }
  
}
