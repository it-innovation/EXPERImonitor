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
//      Created By :            Vegard Engen
//      Created Date :          2012-08-13
//      Created for Project :   ROBUST
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IExperimentDataManager;

/**
 *
 * @author Vegard Engen
 */
public class ExperimentDataManager implements IExperimentDataManager
{

    @Override
    public void saveExperiment(Experiment exp) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Experiment getExperiment(UUID expUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Experiment> getExperiments() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveEntity(Entity ent) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Entity getEntity(UUID entityUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entity> getEntities() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entity> getEntitiesForExperiment(UUID expUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveAttribute(Attribute attrib) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Attribute getAttribute(UUID attribUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Attribute> getAttributesForEntity(UUID entityUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveMetricGenerator(MetricGenerator metricGen, UUID experimentUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MetricGenerator getMetricGenerator(UUID metricGenUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<MetricGenerator> getMetricGenerators() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<MetricGenerator> getMetricGeneratorsForExperiment(UUID expUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveMetricGroup(MetricGroup metricGroup) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MetricGroup getMetricGroup(UUID metricGroupUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<MetricGroup> getMetricGroupsForMetricGenerator(UUID metricGenUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveMeasurementSet(MeasurementSet measurementSet) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MeasurementSet getMeasurementSet(UUID measurementSetUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<MeasurementSet> getMeasurementSetForMetricGroup(UUID metricGroupUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveReport(Report report) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Report getReport(UUID reportUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Report getReportForMeasurementSet(UUID measurementSetUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveMetric(Metric metric) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Metric getMetric(UUID metricUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Metric getMetricForMeasurementSet(UUID measurementSetUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveMeasurement(Measurement measurement) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Measurement geMeasurement(UUID measurementUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Measurement getLatestMeasurementForMeasurementSet(UUID measurementSetUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Measurement> getAllMeasurementsForMeasurementSet(UUID measurementSetUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Measurement> getMeasurementsForMeasurementSetAfterDate(UUID measurementSetUUID, Date fromDate) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Measurement> getMeasurementsForMeasurementSetForTimePeriod(UUID measurementSetUUID, Date fromDate, Date toDate) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
