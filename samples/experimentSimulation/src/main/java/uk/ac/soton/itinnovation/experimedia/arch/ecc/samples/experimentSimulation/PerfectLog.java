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
//      Created By :            Stefanie Wiegand
//      Created Date :          2014-07-25
//      Created for Project :   projectName
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.experimentSimulation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerfectLog implements ILog {

	private static final Logger logger = LoggerFactory.getLogger(PerfectLog.class);

	public String line;
	public String date;
	public String time;
	public Long timestamp;

	public String speed;

	PerfectLog(String line) {

		this.line = line;

		//date/time
		this.date = line.substring(0,10);
		this.time = line.substring(11,19);

		//timestamp
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			Date fullDate = formatter.parse(this.date + " " + this.time);
			this.timestamp = fullDate.getTime()/1000L;
		} catch (ParseException e) {
			logger.error("Error parsing date to create timestamp", e);
		}

		//speed
		this.speed = "0";
	}

	@Override
	public String toString() {
		return this.line;
	}

	@Override
	public String getDate() {
		return date;
	}

	@Override
	public String getTime() {
		return time;
	}

	@Override
	public Long getTimestamp() {
		return timestamp;
	}

	@Override
	public String getSpeed() {
		return speed;
	}
}