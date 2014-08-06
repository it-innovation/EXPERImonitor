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
	public boolean hasFinished = false;

	public static final int VARIANCE_PERCENTAGE = 10;
	public static final int FREQUENCY = 60; //Time between measurements in seconds

    public PeriodicMetricLogParserTool()
    {
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
		//first date so everything will be null until then
		getNextLog();
		currentDate = nextDate;
		currentResponseTime = nextResponseTime;
		getNextLog();
    }

	private void measure() {

		if (nextDate!=null) {
			//set current time to one second in the future
			currentDate.setTime(currentDate.getTime() + (FREQUENCY * 1000L));
			//if next checkpoint reached use new value
			if (currentDate.equals(nextDate) || currentDate.after(nextDate)) {
				currentResponseTime = nextResponseTime;
				//get next line from log
				getNextLog();
			}
			//modify value randomly
			boolean negative = random.nextBoolean();
			double rand = random.nextDouble() * 0.01 * VARIANCE_PERCENTAGE * currentResponseTime;
			if (negative) {
				currentRandomResponseTime = currentResponseTime - Integer.valueOf(String.valueOf(Math.round(rand)));
			} else {
				currentRandomResponseTime = currentResponseTime + Integer.valueOf(String.valueOf(Math.round(rand)));
			}

			logger.debug("Current random response time: " + String.valueOf(currentRandomResponseTime));
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
			logger.debug(nextDate.toString() + ", " + nextResponseTime);
		} else {
			nextDate = null;
		}
	}

    public Collection<Measurement> createReport( MeasurementSet ms, int sampleCount )
    {

		Collection<Measurement> samples = new LinkedList<Measurement>();


		while (nextDate!=null) {
			for (int i=0; i<sampleCount; i++) {
				measure();
				logger.info("Current date: " + currentDate.toString());

				// Create measurement instance
				Measurement m = new Measurement(currentRandomResponseTime + "");
				m.setTimeStamp( new Date(currentDate.getTime()) );
				m.setMeasurementSetUUID( ms.getID() );
				samples.add(m);

				if (nextDate==null) {
					hasFinished = true;
					break;
				}
			}
		}

		return samples;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

}
