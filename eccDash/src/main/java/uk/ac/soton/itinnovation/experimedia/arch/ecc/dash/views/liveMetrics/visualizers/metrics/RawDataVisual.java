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
//      Created Date :          28-Feb-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers;

import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.Iterator;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;




public class RawDataVisual extends BaseMetricVisual
{
  private Table dataTable;
  
  private final String tTimeStamp = "Time";
  private final String tData      = "Data";
  
  
  public RawDataVisual( String title, String unit, String type, UUID msID )
  {
    super( msID );
    
    setTitle( title );
    setMetricInfo( unit, type );
    
    createComponents();
  }
  
  @Override
  public void updateView()
  {
    dataTable.removeAllItems();
    
    Iterator<Measurement> mIt = cachedMeasurements.iterator();
    while ( mIt.hasNext() )
    {
      Measurement m = mIt.next();
      
      dataTable.addItem(
              new Object[] { m.getTimeStamp().toString(),
                             m.getValue() }, m.getUUID() );
    }
    
    dataTable.requestRepaint();
  }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    dataTable = new Table();
    dataTable.setWidth( defaultChartWidth );
    dataTable.setHeight( defaultChartHeight );
    dataTable.addStyleName( "small striped borderless" );
    dataTable.addContainerProperty( tTimeStamp, String.class, null );
    dataTable.addContainerProperty( tData     , String.class, null );
    dataTable.setColumnWidth( tData, 300 );
    vl.addComponent( dataTable );
    
    // Space
    vl.addComponent( UILayoutUtil.createSpace( "5px", null ) );
  }
}
