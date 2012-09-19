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
//	Created Date :			2012-09-19
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////
package eu.experimedia.itinnovation.ecc.web.adapters;

import java.util.Date;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMDataBatch;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPostReportSummary;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.MonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IEMLifecycleListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IExperimentMonitor;

public class EMHost implements IEMLifecycleListener {

    private final Logger logger = Logger.getLogger(EMHost.class);
    private IExperimentMonitor expMonitor;
    private boolean waitingToStartNextPhase = false;
    private IMonitoringEDM expDataMgr;
    private IMetricGeneratorDAO expMGAccessor;
    private IReportDAO expReportAccessor;
    private IMeasurementSetDAO expMSAccessor;
    private Experiment expInstance;

    public EMHost() {
        expMonitor = EMInterfaceFactory.createEM();
        expMonitor.addLifecyleListener(this);

        expDataMgr = new MonitoringEDM();
    }

    public void start(String rabbitIP, UUID emID) throws Exception {
        logger.info("Trying to connect to Rabbit server on " + rabbitIP);

        try {
            expMonitor.openEntryPoint(rabbitIP, emID);
        } catch (Exception e) {
            logger.error("Could not open entry point on Rabbit server");
            throw e;
        }

        boolean dmOK = createExperiment();

        if (!dmOK) {
            logger.error("Had problems setting up the EDM");
            throw new Exception("Could not set up EDM");
        }
    }

    private boolean createExperiment() {
        boolean result = false;

        try {
            expMGAccessor = expDataMgr.getMetricGeneratorDAO();
            expReportAccessor = expDataMgr.getReportDAO();
            expMSAccessor = expDataMgr.getMeasurementSetDAO();

            Date expDate = new Date();
            expInstance = new Experiment();
            expInstance.setName(UUID.randomUUID().toString());
            expInstance.setDescription("Sample ExperimentMonitor based experiment");
            expInstance.setStartTime(expDate);
            expInstance.setExperimentID(expDate.toString());

            IExperimentDAO expDAO = expDataMgr.getExperimentDAO();
            expDAO.saveExperiment(expInstance);
            result = true;
        } catch (Exception e) {
            logger.error("Could not initialise experiment");
        }

        return result;
    }

    public void onClientConnected(EMClient emc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onClientDisconnected(EMClient emc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onLifecyclePhaseStarted(EMPhase emp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onLifecyclePhaseCompleted(EMPhase emp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onFoundClientWithMetricGenerators(EMClient emc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onClientSetupResult(EMClient emc, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onGotMetricData(EMClient emc, Report report) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onGotSummaryReport(EMClient emc, EMPostReportSummary emprs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onGotDataBatch(EMClient emc, EMDataBatch emdb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onClientTearDownResult(EMClient emc, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
