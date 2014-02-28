/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
//
// Copyright in this library belongs to the University of Southampton
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
//	Created By :			    Maxim Bashevoy
//  Updates :			        Simon Crowle
//	Created Date :			  2013-10-24
//	Created for Project : Experimedia
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.system;

import com.vaadin.ui.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.DashLogView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.NAGIOSView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.client.ClientConnectionsView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.client.ClientInfoView;


public class SystemView extends SimpleView {
		
		private TabSheet systemTabSheet;
	
    public SystemView( ClientConnectionsView connectionsView, ClientInfoView clientInfoView, 
											 NAGIOSView nagiosView, DashLogView logView ) {
        VerticalLayout vl = getViewContents();

        Panel panel = new Panel();
        panel.addStyleName("borderless");
        panel.setSizeFull();
        vl.addComponent(panel);

        systemTabSheet = new TabSheet();
        systemTabSheet.addStyleName("borderless");
        systemTabSheet.setSizeFull();
        panel.addComponent(systemTabSheet);

        HorizontalLayout hl = new HorizontalLayout();
        hl.addComponent((Component) connectionsView.getImplContainer());
        hl.addComponent((Component) clientInfoView.getImplContainer());

        systemTabSheet.addTab(hl, "Connected clients", null);

        systemTabSheet.addTab((Component) nagiosView.getImplContainer(), "NAGIOS", null);
        systemTabSheet.addTab((Component) logView.getImplContainer(), "Log", null);
    }
		
		public void switchViewFocus( int index )
		{
			if ( systemTabSheet != null )
    {
      TabSheet.Tab t = systemTabSheet.getTab( index );
      if ( t != null ) systemTabSheet.setSelectedTab( t );
    }
		}
}
