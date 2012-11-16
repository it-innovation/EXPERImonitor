/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2012
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
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
//	Created By :			Maxim Bashevoy
//	Created Date :			2012-09-03
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////

package eu.experimedia.itinnovation.ecc.web.services;

import eu.experimedia.itinnovation.ecc.web.helpers.ExperimentMonitorHelper;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;

@Service("databaseAccessService")
public class DatabaseAccessService {  
    
    @Autowired
    @Qualifier("experimentMonitorHelper")
    private transient ExperimentMonitorHelper emHelper;
    
    public Experiment[] getExperiments() throws Throwable {
        return emHelper.getExperimentMonitor().getExpDataMgr().getExperimentDAO().getExperiments(false).toArray(new Experiment[0]);
    }
    
    public MetricGenerator[] getMetricGeneratorsForExperiment(String experimentUUID) throws Throwable {
        Experiment e = emHelper.getExperimentMonitor().getExpDataMgr().getExperimentDAO().getExperiment(UUID.fromString(experimentUUID), true);
        return e.getMetricGenerators().toArray(new MetricGenerator[0]);
    }
    
    public MetricGroup[] getMetricGroupsForMetricGenerator(String metricGeneratorUUID) throws Throwable {
        Set<MetricGroup> mg = emHelper.getExperimentMonitor().getExpDataMgr().getMetricGroupDAO().getMetricGroupsForMetricGenerator(UUID.fromString(metricGeneratorUUID), true);
        
        return mg.toArray(new MetricGroup[0]);
    }
    
    public Entity[] getAllEntities() throws Throwable {
        Set<Entity> entities = emHelper.getExperimentMonitor().getExpDataMgr().getEntityDAO().getEntities(false);
        return entities.toArray(new Entity[0]);
    }
    
    public Attribute[] getAllAttributes() throws Throwable {
        Set<Entity> entities = emHelper.getExperimentMonitor().getExpDataMgr().getEntityDAO().getEntities(true);
        Set<Attribute> allAttributes = new HashSet<Attribute>();
        for (Entity entity : entities) {
            allAttributes.addAll(entity.getAttributes());
        }
        return allAttributes.toArray(new Attribute[0]);
    }

}
