/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
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
//      Created Date :          28-Mar-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.databaseExport;

import java.io.*;
import java.util.Properties;




public class EntryPoint
{
	public static void main( String[] args )
	{
		// Try loading local EDM properties file
		Properties edmProps = loadEDMProperties();
		
		if ( !edmProps.isEmpty() )
		{
			EDMMetricExporter eme = new EDMMetricExporter();
			
			try
			{
				System.out.println( "Trying to initialise EDM exporter" );
				
				eme.initialise( edmProps );
				
				System.out.println( "Starting export." );
				
				eme.runExportWorkflow();
				
				System.out.println( "Finished export" );
			}
			catch ( Exception ex )
			{
				System.out.println( "Could not export data: " + ex.getMessage() );
			}
		}
		else
			System.out.println( "Could not load EDM properties file" );
		
	}
	
	// Private methods -----------------------------------------------------------
	private static Properties loadEDMProperties()
	{
		Properties edmProps = new Properties();
		InputStream is;
		
		try
		{
			String path = System.getProperty( "user.dir" ) + "\\edm.properties";
			edmProps.load( new FileInputStream(path) );
		}
		catch ( IOException ioe )
		{
			System.out.println( "Could not load EDM properties" );
		}
		
		return edmProps;
	}
}
