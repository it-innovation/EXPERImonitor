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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import org.joda.time.DateTime;

import java.io.*;
import java.util.*;




public class EDMMetricExporter
{
	private IMonitoringEDM      expDataManager;
	private IExperimentDAO      experimentDAO;
	private IMetricGeneratorDAO metricGenDAO;
	private IReportDAO          expReportDAO;
	private boolean				      edmInitialisedOK;
	
	private int experimentsExported;
	private int experimentsNotExported;
	private int entitiesExported;
	private int attributesExported;
	private int measurementSetsExported;
	private int measurementSetsNotExported;
	private int measurementsExported;
	
	public EDMMetricExporter()
	{}
	
	public void initialise( Properties config ) throws Exception
	{
		if ( config == null ) throw new Exception( "Could not initialise exporter: initialisation properties are null" );
		
		experimentsExported        = 0;
		experimentsNotExported     = 0;
		entitiesExported           = 0;
		attributesExported         = 0;
		measurementSetsExported    = 0;
		measurementSetsNotExported = 0;
		measurementsExported       = 0;
		
		// Throw EDM connection results upwards
		edmInitialisedOK = false;
		expDataManager = EDMInterfaceFactory.getMonitoringEDM( config );
		
		experimentDAO = expDataManager.getExperimentDAO();
		metricGenDAO  = expDataManager.getMetricGeneratorDAO();
		expReportDAO  = expDataManager.getReportDAO();
		
		if ( experimentDAO != null && metricGenDAO != null && expReportDAO != null )
			edmInitialisedOK = true;
		else
			throw new Exception( "Could not initialise exporter: could not get data access interfaces" );
	}
	
	public void runExportWorkflow() throws Exception
	{
		if ( !edmInitialisedOK ) throw new Exception( "Could not run export: edm not initalised correctly" );
		
		// Check export folder does not already exist
		if ( folderExists("exportedData") ) throw new Exception ("Export data folder already exists: please backup then delete before continuing" );
		
		// List known experiments
		listKnownExperiments();
		
		// Check before continuing
		System.out.println( "Do you wish to continue with export? Y/N) " );
		
		Scanner scanner = new Scanner( System.in );
		String choice   = scanner.next();
		
		if ( choice.equals( "Y") || choice.equals("y") )
		{
			runExport();
			
			summariseExport();
		}
		else
			System.out.println( "Exiting export process" );
	}
	
	// Private methods -----------------------------------------------------------
	private void listKnownExperiments() throws Exception
	{
		Set<Experiment> result = experimentDAO.getExperiments( false );
		
		if ( result == null ) throw new Exception( "Could not find any experiment data" );
		
		if ( !result.isEmpty() )
		{
			for ( Experiment exp : result )
			{
				String name = exp.getName();
				Date  start = exp.getStartTime();
				
				if ( name == null ) name = "<Unnamed experiment>";
				if ( start == null ) throw new Exception( "Found experiment with no start date" );
				
				System.out.println( "Found experiment: " + name + " started: " + start.toString() );
			}
			
			System.out.println("Found " + result.size() + " experiments \n" );
		}
		else
			System.out.println( "Found no experiments in database" );
	}
	
	private void runExport() throws Exception
	{
		System.out.println( "Starting to export experiments" );
		
		if ( createFolder( "exportedData") )
		{
			Set<Experiment> result = experimentDAO.getExperiments( true );
		
			for ( Experiment exp : result )
			{
				String name = exp.getName();
				Date  start = exp.getStartTime();

				// Get metric generators
				Set<MetricGenerator> metGens = exp.getMetricGenerators();

				// If we don't have any metric generators, there won't be any metrics
				if ( metGens == null || metGens.isEmpty() )
				{
					System.out.println( "Experiment " + name + " does not have any metric generators, so no metric data. Skipping this experiment" );
					experimentsNotExported++;
				}
				else
				{
					System.out.println( "Exporting experiment: " + exp.getName() + " {" + start.toString() + "}" );
					
					try
					{
						exportExperiment( exp );
						experimentsExported++;
					}
					catch ( Exception ex )
					{
						System.out.println( "Had problems exporting experiment : " + exp.getName() + ": " + ex.getMessage() );
						experimentsNotExported++;
					}
				}
			}
		}
		else throw new Exception ( "Could not create export data parent folder" );
	}
	
	private void exportExperiment( Experiment exp ) throws Exception
	{
		Set<MetricGenerator> metGens = exp.getMetricGenerators();
		
		// Create Windows friendly folder name for experiment
		String dateString = exp.getStartTime().toString();
		dateString = dateString.replace( ' ', '_' );
		dateString = dateString.replace( ':', '-' );
		
		String expPath = "exportedData\\" + dateString + "_" + exp.getUUID().toString();
		
		if ( createFolder(expPath) )
		{			
			Set<UUID> msIDS = exportMetaData( metGens, expPath );
			
			// If we extracted measurement set IDs from meta-data, export the data
			if ( !msIDS.isEmpty() )
				exportMetricData( msIDS, expPath );
			else
				System.out.println( "Did not export measurement data for this experiment: no measurement sets found" );
		}
		else throw new Exception( "Could not create export folder for experiment: " + exp.getUUID().toString() );	
	}
	
	private Set<UUID> exportMetaData( Set<MetricGenerator> metGens, String path ) throws Exception
	{
		Collection<Entity> entities = MetricHelper.getAllEntities( metGens ).values();
		HashSet<UUID>       mSetIDs = new HashSet<UUID>();
		
		if ( !entities.isEmpty() )
		{
			System.out.println( "Found " + entities.size() + " entities" );
			
			FileWriter     fw = null;
			BufferedWriter bw = null;

			try
			{
				// Create data file
				fw = new FileWriter( path + "\\metaData.csv" );
				bw = new BufferedWriter( fw );
				
				// Write CSV header
				bw.write( "Entity name, Attribute name, Unit, MeasurementSet ID\n" );

				// See if we have any valid entities to export
				for ( Entity entity : entities )
				{
					if ( entity == null ) throw new Exception( "Could not export meta-data: null Entity found" );
					System.out.println( "Entity: " + entity.getName() );
					
					// Check to see if the Entity has attributes
					Collection<Attribute> attrs = entity.getAttributes();
					if ( attrs != null && !attrs.isEmpty() )
					{
						for ( Attribute attr : attrs )
						{
							if ( attr == null ) throw new Exception( "Could not export meta-data: null Attribute found" );

							Collection<MeasurementSet> mSets = MetricHelper.getMeasurementSetsForAttribute( attr, metGens ).values();

							System.out.println( "Attribute: " + attr.getName() + ": " + mSets.size() + " measurement sets" );
							
							// Write out measurement sets if they exist for this attribute
							if ( !mSets.isEmpty() )
							{
								// Now write the meta-data
								for ( MeasurementSet ms : mSets )
								{
									if ( ms == null ) throw new Exception( "Could not export meta-data: null Measurement Set found" );

									// Add ID to list for data export
									mSetIDs.add( ms.getID() );

									Metric msMetric = ms.getMetric();
									if ( msMetric == null ) throw new Exception ( "Could not export meta-data: null Metric info found" );

									Unit msUnit = msMetric.getUnit();
									if ( msUnit == null ) throw new Exception ( "Could not export meta-data: null Unit info found" );

									System.out.println( "Will export data for measurement set: " + ms.getID().toString() );
									
									// Write out the meta-data
									bw.write( entity.getName() + ", " +
														attr.getName() + ", " +
														msUnit.getName() + ", " +
														ms.getID() + "\n" );
								}
							}
							else
							{
								// Otherwise write out the meta-data but indicate no data
								bw.write( entity.getName() + ", " + attr.getName() + ", , NO MEASUREMENTS captured\n" );
								
								measurementSetsNotExported++;
							}
							
							attributesExported++;
						}
						entitiesExported++;
					}
					else System.out.println( "Entity " + entity.getName() + " has no attributes. Skipping this entity meta-data" );
				}
			}
			catch ( Exception ex )
			{ 
				throw ex; 
			}
			finally
			{
				try
				{
					if ( fw != null ) fw.close();
					if ( bw != null ) bw.close();
				}
				catch ( IOException ioex ) {}
			}
			
			System.out.println( "Meta-data written" );
		}
		else System.out.println( "Did not find any entities, so no metrics. Skipping meta-data for this experiment" );
		
		return mSetIDs;
	}
	
	private void exportMetricData( Set<UUID> msIDS, String path ) throws Exception
	{
		FileWriter     fw = null;
		BufferedWriter bw = null;
		
		try
		{
			// Create data file
		  fw = new FileWriter( path + "\\metricData.csv" );
      bw = new BufferedWriter( fw );
			
			// Write CSV header
			bw.write( "MeasurementSet ID, Time, Value\n" );

			for ( UUID msID : msIDS )
			{
				if ( msID != null )
				{
					String msIDVal = msID.toString();

					Report report = null;
					try
					{
						report = expReportDAO.getReportForAllMeasurements( msID, true );
					}
					catch ( Exception ex )
					{ System.out.println( "Found 0 measurements for " + msIDVal + ", skipping"); }
					
					if ( report != null )
					{
						MeasurementSet ms = report.getMeasurementSet();
						if ( ms != null )
						{
							TreeMap<Date, Measurement> sortedM =
											MetricHelper.sortMeasurementsByDate( ms.getMeasurements() );

							Iterator<Date> ascDateIt = sortedM.keySet().iterator();
							while ( ascDateIt.hasNext() )
							{
								Measurement m = sortedM.get( ascDateIt.next() );

								// ISO-8601 stamping
								DateTime isoTime = new DateTime( m.getTimeStamp().getTime() );

								// Write out data for this measurement set
								bw.write( msIDVal + "," +
													isoTime.toString() + "," +
													m.getValue() + "\n" );
								
								measurementsExported++;
							}
							
							measurementSetsExported++;
						}
						else System.out.println( "Did not write any data for measurement set: " + msIDVal );
					}
					// NULL report already caught above
				}
				else System.out.println( "Did not write measurement data: measurement set ID is null" ); 
			}
		}
		catch ( Exception ex )
		{ 
			throw ex; 
		}
		finally
		{
			try
			{
				if ( fw != null ) fw.close();
				if ( bw != null ) bw.close();
			}
			catch ( IOException ioex ) {}
		}
	}
	
	private boolean folderExists( String path )
	{
		String localPath = System.getProperty( "user.dir" );
		
		File folder = new File( localPath+ "\\" + path );
		
		if ( folder.exists() ) return true;
		
		return false;
	}
	
	private boolean createFolder( String path )
	{
		String localPath = System.getProperty( "user.dir" );
		
		File folder = new File( localPath+ "\\" + path );
		
		if ( folder.mkdirs() ) return true;
		
		return false;
	}
	
	private void summariseExport()
	{
		System.out.println( "\n\n-----------------------------------------------");
		System.out.println( "Export summary ");
		System.out.println( "-----------------------------------------------");
		
		System.out.println( "Successfully exported experiments: " + experimentsExported );
		System.out.println( "Total skipped experiments (no generators): " + experimentsNotExported );
		System.out.println( "Total Entites written to disk: " + entitiesExported );
		System.out.println( "Total attributes written to disk: " + attributesExported );
		System.out.println( "Total measurement sets written to disk:" + measurementSetsExported );
		System.out.println( "Total measurement sets skipped (empty): " + measurementSetsNotExported );
		System.out.println( "Total measurements written to disk: " + measurementsExported );
	}
}
