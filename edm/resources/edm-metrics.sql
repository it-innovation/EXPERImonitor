CREATE TABLE Experiment
(
   expUUID uuid NOT NULL, 
   expID character varying(256), 
   name character varying(128) NOT NULL, 
   description text, 
   startTime bigint, 
   endTime bigint, 
   CONSTRAINT expUUID PRIMARY KEY (expUUID)
) 
WITH (
  OIDS = FALSE
)
;

CREATE TABLE Entity
(
   entityUUID uuid NOT NULL, 
   name character varying(128) NOT NULL, 
   description text, 
   CONSTRAINT entityUUID PRIMARY KEY (entityUUID)
) 
WITH (
  OIDS = FALSE
)
;

CREATE TABLE Attribute
(
   attribUUID uuid NOT NULL, 
   entityUUID uuid NOT NULL, 
   name character varying(128) NOT NULL, 
   description text, 
   CONSTRAINT attribUUID PRIMARY KEY (attribUUID), 
   CONSTRAINT entityUUID FOREIGN KEY (entityUUID) REFERENCES Entity (entityUUID) ON UPDATE CASCADE ON DELETE CASCADE
) 
WITH (
  OIDS = FALSE
)
;

CREATE TABLE MetricGenerator
(
   mGenUUID uuid NOT NULL, 
   expUUID uuid NOT NULL, 
   name character varying(128) NOT NULL, 
   description text, 
   CONSTRAINT mGenUUID PRIMARY KEY (mGenUUID), 
   CONSTRAINT expUUID FOREIGN KEY (expUUID) REFERENCES Experiment (expUUID) ON UPDATE CASCADE ON DELETE CASCADE
) 
WITH (
  OIDS = FALSE
)
;

CREATE TABLE MetricGenerator_Entity
(
   mGenUUID uuid NOT NULL, 
   entityUUID uuid NOT NULL, 
   CONSTRAINT mGenUUID FOREIGN KEY (mGenUUID) REFERENCES MetricGenerator (mGenUUID) ON UPDATE CASCADE ON DELETE CASCADE, 
   CONSTRAINT entityUUID FOREIGN KEY (entityUUID) REFERENCES Entity (entityUUID) ON UPDATE CASCADE ON DELETE CASCADE
) 
WITH (
  OIDS = FALSE
)
;

CREATE TABLE MetricGroup
(
   mGrpUUID uuid NOT NULL, 
   mGenUUID uuid NOT NULL, 
   name character varying(128) NOT NULL, 
   description text, 
   CONSTRAINT mGrpUUID PRIMARY KEY (mGrpUUID), 
   CONSTRAINT mGenUUID FOREIGN KEY (mGenUUID) REFERENCES MetricGenerator (mGenUUID) ON UPDATE CASCADE ON DELETE CASCADE
) 
WITH (
  OIDS = FALSE
)
;

CREATE TABLE MetricType
(
   name character varying(10) NOT NULL, 
   CONSTRAINT name PRIMARY KEY (name)
) 
WITH (
  OIDS = FALSE
)
;

CREATE TABLE Metric
(
   metricUUID uuid NOT NULL, 
   mType character varying(10) NOT NULL, 
   unit bytea NOT NULL, 
   CONSTRAINT metricUUID PRIMARY KEY (metricUUID), 
   CONSTRAINT mType FOREIGN KEY (mType) REFERENCES MetricType (name) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;

CREATE TABLE MeasurementSet
(
   mSetUUID uuid NOT NULL, 
   mGrpUUID uuid NOT NULL, 
   attribUUID uuid NOT NULL, 
   metricUUID uuid NOT NULL, 
   CONSTRAINT mSetUUID PRIMARY KEY (mSetUUID), 
   CONSTRAINT mGrpUUID FOREIGN KEY (mGrpUUID) REFERENCES MetricGroup (mGrpUUID) ON UPDATE CASCADE ON DELETE CASCADE, 
   CONSTRAINT attribUUID FOREIGN KEY (attribUUID) REFERENCES Attribute (attribUUID) ON UPDATE CASCADE ON DELETE CASCADE, 
   CONSTRAINT metricUUID FOREIGN KEY (metricUUID) REFERENCES Metric (metricUUID) ON UPDATE CASCADE ON DELETE CASCADE
) 
WITH (
  OIDS = FALSE
)
;

CREATE TABLE Measurement
(
   measurementUUID uuid NOT NULL, 
   mSetUUID uuid NOT NULL, 
   timeStamp bigint NOT NULL, 
   value character varying(56), 
   CONSTRAINT measurementUUID PRIMARY KEY (measurementUUID), 
   CONSTRAINT mSetUUID FOREIGN KEY (mSetUUID) REFERENCES MeasurementSet (mSetUUID) ON UPDATE CASCADE ON DELETE CASCADE
) 
WITH (
  OIDS = FALSE
)
;

CREATE TABLE Report
(
   reportUUID uuid NOT NULL, 
   mSetUUID uuid NOT NULL, 
   reportTimeStamp bigint NOT NULL, 
   fromDateTimeStamp bigint NOT NULL, 
   toDateTimeStamp bigint NOT NULL, 
   numMeasurements integer NOT NULL, 
   CONSTRAINT reportUUID PRIMARY KEY (reportUUID), 
   CONSTRAINT mSetUUID FOREIGN KEY (mSetUUID) REFERENCES MeasurementSet (mSetUUID) ON UPDATE CASCADE ON DELETE CASCADE
) 
WITH (
  OIDS = FALSE
)
;

INSERT INTO MetricType VALUES ('NOMINAL');
INSERT INTO MetricType VALUES ('ORDINAL');
INSERT INTO MetricType VALUES ('INTERVAL');
INSERT INTO MetricType VALUES ('RATIO');