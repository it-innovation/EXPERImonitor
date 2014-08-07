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
//      Created By :            Stefanie Wiegand
//      Created Date :          05-Aug-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.lwtClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class PeriodicMetricLogParserTool
{
    private final Random random = new Random();
	private static final Logger logger = LoggerFactory.getLogger(PeriodicMetricLogParserTool.class);
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final LinkedList<String> log = new LinkedList<String>();

    private Date currentDate;
    private Date nextDate;

    private int  currentResponseTime;
	private int  nextResponseTime;
	private int currentRandomResponseTime;

	private int  currentMem;
	private int  nextMem;
	private int currentRandomMem;

	private int  currentCPU;
	private int  nextCPU;
	private int currentRandomCPU;

	public boolean hasFinished = false;

	public static final int VARIANCE_PERCENTAGE = 10;
	public static final int FREQUENCY = 60; //Time between measurements in seconds

    public PeriodicMetricLogParserTool()
    {
       init();
    }

	private void init() {
		 //read log
		String logfilePath = PeriodicMetricLogParserTool.class.getClassLoader().getResource("lwt.txt").getPath();
		try {
			FileReader fr = new FileReader(new File(logfilePath));
			BufferedReader br = new BufferedReader(fr);

			String line;
			while((line = br.readLine()) != null) {
				log.add(line.trim());
			}
		} catch (IOException e) {
			logger.error("Error reading from logfile " + logfilePath, e);
		}
		//first date; everything will be null until then
		getNextLog();
		currentDate = nextDate;
		rollForward();
		getNextLog();
	}

	private void rollForward() {
		currentResponseTime = nextResponseTime;
		currentCPU = nextCPU;
		currentMem = nextMem;
	}

	private void measure() {

		if (nextDate!=null) {
			//set current time to next step in the future (amount = frequency)
			currentDate.setTime(currentDate.getTime() + (FREQUENCY * 1000L));
			//if next checkpoint reached use new value
			if (currentDate.equals(nextDate) || currentDate.after(nextDate)) {
				rollForward();
				//get next line from log
				getNextLog();
			}

			//modify value randomly
			currentRandomResponseTime = currentResponseTime + createRandomVarianceOf(currentResponseTime);
			currentRandomCPU = currentCPU + createRandomVarianceOf(currentCPU);
			currentRandomMem = currentMem + createRandomVarianceOf(currentMem);

			//logger.debug("Current random response time: " + String.valueOf(currentRandomResponseTime));
			//logger.debug("Current random CPU: " + String.valueOf(currentRandomCPU));
			//logger.debug("Current random Mem: " + String.valueOf(currentRandomMem));
		}
	}

	private int createRandomVarianceOf(int value) {
		//randomise value
		double rand = random.nextDouble() * 0.01 * VARIANCE_PERCENTAGE * value;
		int randy = (int) Math.round(rand);
		//positive or negative?
		if (random.nextBoolean()) {
			return -randy;
		} else {
			return randy;
		}
	}

	private void getNextLog() {
		if (!log.isEmpty()) {
			logger.debug("log not empty: " + log.peekFirst());
			String l = log.pollFirst();
			String date = l.substring(0,19);
			try {
				nextDate = formatter.parse(date);
			} catch (ParseException e) {
				logger.error("Error formatting date: " + date, e);
			}
			nextResponseTime = Integer.valueOf(l.split(",")[1].split(":")[1]);
			nextMem = Integer.valueOf(l.split(",")[2].split(":")[1]);
			nextCPU = Integer.valueOf(l.split(",")[3].split(":")[1]);

			//logger.debug(nextDate.toString() + ", " + nextResponseTime);
			//logger.debug("Response time: " + nextResponseTime);
			//logger.debug("Mem: " + nextMem);
			//logger.debug("CPU: " + nextCPU);

		} else {
			nextDate = null;
		}
	}

    public Collection<Measurement> createReport( MeasurementSet ms, MetricGenerator metGen, int sampleCount )
    {

		Collection<Measurement> samples = new LinkedList<Measurement>();

		//get attribute for measurement
		Entity entity = MetricHelper.getEntityFromName("LWTService", metGen);
		UUID aID = ms.getAttributeID();
		Attribute a = MetricHelper.getAttributeByID(aID, entity);
		String aName = a.getName();

		logger.debug(ms.getAttributeID() + ", " + aName);

		while (nextDate!=null) {
			for (int i=0; i<sampleCount; i++) {
				measure();
				logger.debug("Current date: " + currentDate.toString());

				// Create measurement instances
				Measurement m = null;

				if (aName.equals("Response time")) {
					m = new Measurement(currentRandomResponseTime + "");
				} else if (aName.equals("CPU usage")) {
					m = new Measurement(currentRandomCPU + "");
				} else if (aName.equals("Memory usage")) {
					m = new Measurement(currentRandomMem + "");
				} else {
					logger.warn("Unknown attribute for measurement: " + aName + ", skipping");
					break;
				}

				m.setTimeStamp( new Date(currentDate.getTime()) );
				m.setMeasurementSetUUID( ms.getID() );
				samples.add(m);

				logger.debug("Added " + aName + " measurement");

				if (nextDate==null) {
					hasFinished = true;
					break;
				}
			}
		}

		//reset log
		init();

		return samples;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

}
