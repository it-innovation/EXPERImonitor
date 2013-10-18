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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.visualizers.metrics;

import com.invient.vaadin.charts.Color.RGB;
import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DateTimePoint;
import com.invient.vaadin.charts.InvientCharts.DateTimeSeries;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.DateTimeAxis;
import com.invient.vaadin.charts.InvientChartsConfig.Legend;
import com.invient.vaadin.charts.InvientChartsConfig.LineConfig;
import com.invient.vaadin.charts.InvientChartsConfig.MarkerState;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.SymbolMarker;
import com.invient.vaadin.charts.InvientChartsConfig.SymbolMarker.Symbol;
import com.invient.vaadin.charts.InvientChartsConfig.Title;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.VerticalLayout;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;




public class NumericTimeSeriesVisual extends BaseMetricVisual
{
  private static boolean applyNonUTC = true;
  
  private InvientCharts                chart;
  private InvientChartsConfig          chartConfig;
  private DateTimeAxis                 timeAxis;
  private DateTimeSeries               dataSeries;
  private LinkedHashSet<DateTimePoint> plotPoints;
  

  public NumericTimeSeriesVisual( String title, String unit, String type, UUID msID )
  {
    super( msID );
    
    setTitle( title );
    setMetricInfo( unit, type );
    
    createChartConfig();
    createComponents();
  }
  
  @Override
  public void updateView()
  {
    // Update graph series view
    if ( !cachedMeasurements.isEmpty() )
    {
      chart.removeSeries( dataSeries );
      dataSeries.removeAllPoints();
      plotPoints.clear();

      // Ranges for axes
      Measurement first = null;

      Iterator<Measurement> mIt = cachedMeasurements.iterator();
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
  
  // Private methods -----------------------------------------------------------
  private void createChartConfig()
  {
    chartConfig = new InvientChartsConfig();
    chartConfig.getGeneralChartConfig().setAnimation( false );
    chartConfig.getGeneralChartConfig().setType( SeriesType.LINE );
    chartConfig.getGeneralChartConfig().setReflow( false );
    chartConfig.getGeneralChartConfig().setBackgroundColor( new RGB(242,242,242) );
    chartConfig.getCredit().setEnabled( false );
    
    // Remove unwanted visual components
    Title title = new Title();
    title.setText( "" );
    chartConfig.setTitle( title );
    
    Legend legend = new Legend();
    legend.setEnabled( false );
    chartConfig.setLegend( legend );
    
    // Time axis
    timeAxis = new DateTimeAxis();
    timeAxis.setTitle( new AxisTitle( "Time" ) ); 
    timeAxis.setMin( new Date() );
    LinkedHashSet<XAxis> xAxisSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
    xAxisSet.add( timeAxis );
    
    // Numeric axis
    NumberYAxis yAxis = new NumberYAxis();
    yAxis.setTitle( new AxisTitle( (String) visualUnit.getValue() ) );
    
    LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
    yAxesSet.add( yAxis );
    
    chartConfig.setXAxes( xAxisSet );
    chartConfig.setYAxes( yAxesSet );
    
    chart = new InvientCharts( chartConfig );
    chart.setWidth( defaultChartWidth );
    chart.setHeight( defaultChartHeight );
    
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
    
    // Apply a once-off UTC global setting for Inveint charts
    if ( applyNonUTC )
    {
      try
      {
        String script   = "<div><script>$wnd.Highcharts.setOptions({  global: {  useUTC: false }});</script></div>";
        CustomLayout cl = new CustomLayout( new ByteArrayInputStream(script.getBytes()) );  
        vl.addComponent(cl);
        applyNonUTC = false;
      } 
      catch (IOException ex)
      { /*Really nothing we can do here to fix this */ }

    }
    
    // Add in chart
    if ( chart != null ) vl.addComponent( chart );
  }
}
