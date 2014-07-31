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

public class Log implements ILog {

		private static final Logger logger = LoggerFactory.getLogger(Log.class);

		public String[] lines;
		public String date;
		public String time;
		public Long timestamp;

		public String longitude = null;
		public String latitude = null;
		public String altitude = null;

		public String speed = null;
		public String temperature = null;

		Log(String[] lines) {

			this.lines = lines;

			//date/time
			this.date = lines[0].substring(0,10);
			this.time = lines[0].substring(11,19);
			//logger.debug(this.date + ", " + this.time);

			//timestamp
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				Date fullDate = formatter.parse(this.date + " " + this.time);
				this.timestamp = fullDate.getTime()/1000L;
			} catch (ParseException e) {
				logger.error("Error parsing date to create timestamp", e);
			}

			//check for long/lat/alt
			if (lines[0].split(",").length>2) {
				this.latitude = lines[0].split(",")[2];
				if (!this.latitude.matches("\\d{1,2}\\.\\d{1,15}")) {
					this.latitude = null;
				}
			}
			if (lines[0].split(",").length>3) {
				this.longitude = lines[0].split(",")[3];
				if (!this.longitude.matches("\\d{1,2}\\.\\d{1,15}")) {
					this.longitude = null;
				}
			}
			if (lines[0].split(",").length>4) {
				this.altitude = lines[0].split(",")[4];
				if (!this.altitude.matches("\\d{1,4}\\.\\d{1,2}")) {
					this.altitude = null;
				}
			}
			//logger.debug("lat: " + this.latitude + ", long: " + this.longitude + ", alt: " + this.altitude);

			//speed/temp
			if (lines[1].split(",").length>2) {
				this.speed = lines[1].split(",")[2];
			}
			if (lines[2].split(",").length>2) {
				this.temperature = lines[2].split(",")[2];
			}
			//logger.debug("speed: " + this.speed + ", temperature: " + this.temperature);
		}

		@Override
		public String toString() {
			return this.lines[0] + "\n" + this.lines[1] + "\n" + this.lines[2];
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