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
//      Created for Project :   ECC Dash
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers;

import com.invient.vaadin.charts.Color.RGB;
import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DateTimePoint;
import com.invient.vaadin.charts.InvientCharts.DateTimeSeries;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.DateTimeAxis;
import com.invient.vaadin.charts.InvientChartsConfig.LineConfig;
import com.invient.vaadin.charts.InvientChartsConfig.MarkerState;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.SymbolMarker;
import com.invient.vaadin.charts.InvientChartsConfig.SymbolMarker.Symbol;
import com.invient.vaadin.charts.InvientChartsConfig.Title;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxisDataLabel;
import com.vaadin.ui.VerticalLayout;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;




public class NumericTimeSeriesVisual extends BaseMetricVisual
{
  private InvientCharts                chart;
  private InvientChartsConfig          chartConfig;
  private DateTimeAxis                 timeAxis;
  private DateTimeSeries               dataSeries;
  private LinkedHashSet<DateTimePoint> plotPoints;
  
  private transient LinkedList<Measurement> measurementSeries;
  private transient int maxPoints = 20;
  

  public NumericTimeSeriesVisual( String title, String unit, String type )
  {
    super();
    
    setTitle( title );
    setMetricInfo( unit, type );
    
    measurementSeries = new LinkedList<Measurement>();
    
    createChartConfig();
    createComponents();
  }
  
  // BaseMetricVisual ----------------------------------------------------------
  @Override
  public void addMeasurementData( MeasurementSet ms )
  {
    if ( ms != null )
    {
      // Gather limited measurements internally
      Set<Measurement> measurements = ms.getMeasurements();
      if ( measurements != null && !measurements.isEmpty() )
      {
        Iterator<Measurement> mIt = ms.getMeasurements().iterator();
        while ( mIt.hasNext() )
        {
          Measurement m = mIt.next();
          measurementSeries.add( m );
          
          if ( measurementSeries.size() > maxPoints )
            measurementSeries.removeFirst();
        }
      }
      
      // Update graph series view
      if ( !measurementSeries.isEmpty() )
      {
        chart.removeSeries( dataSeries );
        dataSeries.removeAllPoints();
        plotPoints.clear();
        
        // Ranges for axes
        Measurement first = null, min = null, max = null;
        
        Iterator<Measurement> mIt = measurementSeries.iterator();
        while ( mIt.hasNext() )
        {
          Measurement m = mIt.next();
          
          // Update range data
          if ( first == null ) first = m;
          
          plotPoints.add( new DateTimePoint( dataSeries,
                                             m.getTimeStamp(),
                                             Double.parseDouble(m.getValue())) );
        }
        
        // Update axes
        timeAxis.setMin( first.getTimeStamp() );
        
        // Update series
        dataSeries.setSeriesPoints( plotPoints );
        chart.addSeries( dataSeries );
      }
    }
  }
  
  // Private methods -----------------------------------------------------------
  private void createChartConfig()
  {
    chartConfig = new InvientChartsConfig();
    chartConfig.getGeneralChartConfig().setType( SeriesType.LINE );
    chartConfig.getGeneralChartConfig().setReflow( false );
    chartConfig.getCredit().setEnabled( false );
    Title title = new Title(); // Base class handles this
    title.setText( "" );
    chartConfig.setTitle( title );
    
    // Time axis
    timeAxis = new DateTimeAxis();
    timeAxis.setTitle( new AxisTitle( "Time" ) ); 
    timeAxis.setMin( new Date() );
    LinkedHashSet<XAxis> xAxisData = new LinkedHashSet<InvientChartsConfig.XAxis>();
    xAxisData.add( timeAxis );
    
    // Numeric axis
    NumberYAxis yAxis = new NumberYAxis();
    yAxis.setTitle( new AxisTitle( visualUnit.getCaption()) );
    
    LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
    yAxesSet.add( yAxis );
    
    chartConfig.setXAxes( xAxisData );
    chartConfig.setYAxes( yAxesSet );
    
    chart = new InvientCharts( chartConfig );
    chart.setWidth( "425px" );
    chart.setHeight( "280px" );
    
    // Series & marker configuration
    SymbolMarker marker = new SymbolMarker();
    marker.setSymbol( Symbol.DIAMOND );
    marker.setHoverState( new MarkerState() );
    marker.getHoverState().setEnabled(true);
    marker.getHoverState().setRadius(3);
    
    LineConfig lineConfig = new LineConfig();
    lineConfig.setAnimation( false );
    lineConfig.setMarker( marker );
    lineConfig.setColor( new RGB(69,114,167) );
    
    dataSeries = new DateTimeSeries( visualTitle + " (" + visualUnit + ")",
                                     lineConfig,
                                     true );
    
    plotPoints = new LinkedHashSet<InvientCharts.DateTimePoint>(); 
    dataSeries.setSeriesPoints( plotPoints );
    
    chart.addSeries( dataSeries );
  }
  
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();    
    if ( chart != null ) vl.addComponent( chart );
  }
}
