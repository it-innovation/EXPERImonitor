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
//	Created Date :			2012-09-04
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////

package eu.experimedia.itinnovation.ecc.web.listeners;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMDataBatch;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPostReportSummary;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IEMLifecycleListener;


public class EMListener implements IEMLifecycleListener {
    
    private EMClient[] liveClientList = new EMClient[0];

    public void onClientConnected(EMClient client) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onClientDisconnected(EMClient client) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onLifecyclePhaseStarted(EMPhase phase) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onLifecyclePhaseCompleted(EMPhase phase) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onFoundClientWithMetricGenerators(EMClient client) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onClientSetupResult(EMClient client, boolean success) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onGotMetricData(EMClient client, Report report) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onGotSummaryReport(EMClient client, EMPostReportSummary summary) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onGotDataBatch(EMClient client, EMDataBatch batch) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void onDataBatchMeasurementSetCompleted( EMClient client, MeasurementSet ms ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
  
    public void onAllDataBatchesRequestComplete( EMClient client ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onClientTearDownResult(EMClient client, boolean success) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
