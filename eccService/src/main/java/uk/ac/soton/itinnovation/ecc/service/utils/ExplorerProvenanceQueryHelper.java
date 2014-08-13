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
//      Created Date :          08-Aug-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.utils;


import java.util.*;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.provenance.*;
import uk.ac.soton.itinnovation.owlimstore.sesame.ASesameConnector;
import uk.ac.soton.itinnovation.owlimstore.sesame.RemoteSesameConnector;





public class ExplorerProvenanceQueryHelper {

	private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
	private ASesameConnector store;

    public ExplorerProvenanceQueryHelper(Properties props) {
		try {
			store = new RemoteSesameConnector(props, props.getProperty("owlim.sesameServerURL"));
		} catch (Exception e) {
			logger.error("Error reading properties", e);
		}
    }

    /**
     * Use this method to initialise the query helper
     *
     * @throws Exception
     */
    public void initialise() throws Exception {
    }

    /**
     * Use this method to shut down the query helper. (Will close repositories if open)
     *
     */
    public void shutdown() {
        store.disconnect();
    }

    /**
     * Use this method to get a summary of an experiment in a properties file that
     * provides:
     *
     * participantCount         : number of participants in the experiment
     * activitiesPerformedCount : number of activities performed
     * applicationsUsedCount    : number of applications used
     * servicesUsedCount        : number of services used
     *
     * @param expID - Non-null UUID of experiment
     * @return      - Non-null property instance (with summary data if available)
     */
    public Properties getExperimentProvSummary( UUID expID ) {

        Properties result = new Properties();

        return result;
    }

    /**
     * Use this method to get the IRI for all Participants associated with an experiment.
     *
     * @param expID - Non-null experiment ID
     * @return      - Returns a (possibly empty) set of IRIs representing participants
     */
    public Set<String> getParticipantIRIs( UUID expID ) throws Exception {

		String sparql = "SELECT * WHERE { ?participant a http://experimedia.eu/ontologies/ExperimediaExperimentExplorer:Participant . }";
        TupleQueryResult tqr = store.query(expID.toString(), sparql);
        HashSet<String> result = new HashSet<>();
		while (tqr.hasNext()) {
			result.add(tqr.next().getBinding("participant").getValue().toString());
		}

        return result;
    }

    /**
     * Use this method to retrieve a summary of the activities associated with a participant
     * of a particular experiment
     *
     * @param expID     - Non-null experiment ID
     * @param partIRI   - Non-null participant IRI
     * @return          - A (possibly null) summary of activities
     */
    public EccParticipantActivitySummaryResultSet getParticipantActivitySummary( UUID   expID,
                                                                                 String partIRI ) {

        EccParticipantActivitySummaryResultSet result = null;


        return result;
    }

    /**
     * Use this method to retrieve a set of activity instances that relate to a participant in
     * an experiment.
     *
     * @param expID     - Non-null experiment ID
     * @param partIRI   - Non-null IRI of the participant in question
     * @return          - Returns a (possibly null) activity result set
     */
    public EccParticipantActivityResultSet getParticipantsActivityInstances( UUID   expID,
                                                                             String partIRI ) {

        EccParticipantActivityResultSet result = null;

        // Stefanie - don't worry if you can't derive a description of the activity,
        // just leave it as " " for now


        return result;
    }

    /**
     * Use this method to retrieve a set of activity instances that relate to a participant in
     * an experiment and that share the same activity label.
     *
     * @param expID     - Non-null experiment ID
     * @param partIRI   - Non-null IRI of the participant in question
     * @param actLabel  - Non-null String with the label of an activity
     * @return          - Returns a (possibly null) activity result set
     */
    public EccParticipantActivityResultSet getParticipantActivityInstances( UUID   expID,
                                                                            String partIRI,
                                                                            String actLabel ) {
        EccParticipantActivityResultSet result = null;

        // Stefanie - very similar to the above, only this time the result is filtered by an activity label


        return result;
    }

    // -------------------------------------------------------------------------
    // Stefanie: at this point I think you can guess the rest. Please add the JavaDoc
    // once you have implemented the call
    // -------------------------------------------------------------------------

    public EccActivityApplicationResultSet getApplicationsUsedByActivity( UUID   expID,
                                                                          String activityIRI ) {
        EccActivityApplicationResultSet result = null;

        return result;
    }

    public EccApplicationServiceResultSet getServicesUsedByAppplication( UUID expID,
                                                                         String appIRI ) {

        EccApplicationServiceResultSet result = null;

        return result;
    }
}
