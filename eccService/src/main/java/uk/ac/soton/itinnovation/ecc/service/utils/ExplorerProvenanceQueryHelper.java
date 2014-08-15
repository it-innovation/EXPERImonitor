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
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
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
	 * @throws java.lang.Exception is any of the queries fail
     */
    public Properties getExperimentProvSummary( UUID expID ) throws Exception {

        Properties result = new Properties();

		//experimentID
		result.setProperty("experimentID", expID.toString());

		//participantCount
		Set<String> participants = getParticipantIRIs(expID);
		result.setProperty("participantCount", participants.size() + "");

		//activitiesPerformedCount	TODO
		result.setProperty("activitiesPerformedCount", "-1" );

		//applicationsUsedCount	TODO
		result.setProperty("applicationsUsedCount", "-1" );

		//servicesUsedCount	TODO
		result.setProperty("servicesUsedCount", "-1" );

        return result;
    }

	/**
	 * Get a single activity
	 *
	 * @param expID
	 * @param actIRI
	 * @return
	 * @throws java.lang.Exception
	 */
	public EccActivity getActivity( UUID	expID,
									String	actIRI) throws Exception {

		EccActivity activity = null;

		String sparql = "SELECT * WHERE { ?activity a <http://www.w3.org/ns/prov#Activity> . "
					  + "?activity <http://www.w3.org/ns/prov#startedAtTime> ?start . "
				      + "?activity <http://www.w3.org/ns/prov#endedAtTime> ?end . "
					  + "?activity rdfs:label ?label }";

		TupleQueryResult tqr = store.query(expID.toString(), sparql);

		HashMap<String, Integer> activities = new HashMap<>();
		if (tqr!=null) {
			while (tqr.hasNext()) {

				BindingSet tqrb = tqr.next();

				String s = tqrb.getBinding("start").getValue().toString();
				String e = tqrb.getBinding("end").getValue().toString();
				Calendar start = javax.xml.bind.DatatypeConverter.parseDateTime(s.substring(s.indexOf("\"")+1, s.lastIndexOf("\"")));
				Calendar end = javax.xml.bind.DatatypeConverter.parseDateTime(e.substring(e.indexOf("\"")+1, e.lastIndexOf("\"")));
				//TODO: not sure about timestamp format here...

				String l = tqrb.getBinding("label").getValue().toString();
				activity = new EccActivity(l.substring(l.indexOf("\"")+1, l.lastIndexOf("\"")), "TODO: description",
						actIRI, new Date(start.getTimeInMillis()), new Date(end.getTimeInMillis()));

				//this is intentional; there should only be one row returned since we're querying by iri.
				// if there were more, they would be ignored
				break;
			}
		}

		return activity;
	}

    /**
     * Use this method to get the IRI for all Participants associated with an experiment.
     *
     * @param expID - Non-null experiment ID
     * @return      - Returns a (possibly empty) set of IRIs representing participants
	 * @throws java.lang.Exception if the query fails
     */
    public Set<String> getParticipantIRIs( UUID expID ) throws Exception {

		String sparql = "SELECT * WHERE { ?participant a <http://experimedia.eu/ontologies/ExperimediaExperimentExplorer#Participant> . }";
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
     * @param expID		- Non-null experiment ID
     * @param partIRI	- Non-null participant IRI
	 * @param part		- Non-null participant
     * @return			- A (possibly null) summary of activities
	 * @throws java.lang.Exception if the query fails
     */
    public EccParticipantActivitySummaryResultSet getPartActivitySummary( UUID   expID,
                                                                          String partIRI,
																		  EccParticipant part ) throws Exception {

        EccParticipantActivitySummaryResultSet result = new EccParticipantActivitySummaryResultSet(part);

		String sparql = "SELECT * WHERE { ?activity a <http://www.w3.org/ns/prov#Activity> . "
					  + "?activity <http://www.w3.org/ns/prov#wasStartedBy> <" + part.getIRI() + "> . "
					  + "?activity rdfs:label ?label }";
		TupleQueryResult tqr = store.query(expID.toString(), sparql);

		HashMap<String, Integer> activities = new HashMap<>();
		if (tqr!=null) {
			while (tqr.hasNext()) {
				String label = tqr.next().getBinding("label").getValue().toString();
				label = label.substring(label.indexOf("\"")+1, label.lastIndexOf("\""));

				if (!activities.containsKey(label)) {
					activities.put(label, 1);
				} else {
					activities.put(label, activities.get(label)+1);
				}
			}
		}

		for (Map.Entry<String, Integer> e: activities.entrySet()) {
			logger.info("Activity count: " + e.getKey() + ": " + e.getValue());
			result.addActivitySummary(new EccActivitySummaryInfo(e.getKey(), e.getValue()));
		}

        return result;
    }

    /**
     * Use this method to retrieve a set of activity instances that relate to a participant in
     * an experiment.
     *
     * @param expID     - Non-null experiment ID
     * @param partIRI   - Non-null IRI of the participant in question
	 * @param part		- Non-null participant
     * @return          - Returns a (possibly null) activity result set
	 * @throws org.openrdf.query.QueryEvaluationException if query fails
     */
    public EccParticipantActivityResultSet getParticipantsActivityInstances( UUID   expID,
                                                                             String partIRI,
																			 EccParticipant part ) throws QueryEvaluationException, Exception {

		EccParticipantActivityResultSet result = null;

		String sparql = "SELECT * WHERE { ?activity a <http://www.w3.org/ns/prov#Activity> . "
					+ "?activity <http://www.w3.org/ns/prov#wasStartedBy> <" + partIRI + "> . "
					+ "?activity rdfs:label ?label . "
					+ "?activity <http://www.w3.org/ns/prov#startedAtTime> ?start . "
					+ "?activity <http://www.w3.org/ns/prov#endedAtTime> ?end . }";
		TupleQueryResult tqr = store.query(expID.toString(), sparql);


		if (tqr!=null) {

			result = new EccParticipantActivityResultSet(part);
			while (tqr.hasNext()) {

				BindingSet tqrb = tqr.next();
				String label = tqrb.getBinding("label").getValue().toString();

				String s = tqrb.getBinding("start").getValue().toString();
				String e = tqrb.getBinding("end").getValue().toString();
				Calendar start = javax.xml.bind.DatatypeConverter.parseDateTime(s.substring(s.indexOf("\"")+1, s.lastIndexOf("\"")));
				Calendar end = javax.xml.bind.DatatypeConverter.parseDateTime(e.substring(e.indexOf("\"")+1, e.lastIndexOf("\"")));
				//TODO: not sure about timestamp format here...
				
				String l = tqrb.getBinding("label").getValue().toString();
				EccActivity a = new EccActivity(l.substring(l.indexOf("\"")+1, l.lastIndexOf("\"")), "TODO: description",
						tqrb.getBinding("activity").getValue().toString(), new Date(start.getTimeInMillis()), new Date(end.getTimeInMillis()));

				result.addActivity(a);
			}
		}

        return result;
    }

    /**
     * Use this method to retrieve a set of activity instances that relate to a participant in
     * an experiment and that share the same activity label.
     *
     * @param expID     - Non-null experiment ID
     * @param partIRI   - Non-null IRI of the participant in question
     * @param actLabel  - Non-null String with the label of an activity
	 * @param part		- Non-null participant
     * @return          - Returns a (possibly null) activity result set
	 * @throws java.lang.Exception if the query fails
     */
    public EccParticipantActivityResultSet getParticipantActivityInstances( UUID   expID,
                                                                            String partIRI,
                                                                            String actLabel,
																			EccParticipant part ) throws Exception {

        EccParticipantActivityResultSet result = null;

		ArrayList<EccActivity> activities = getParticipantsActivityInstances(expID, partIRI, part).getActivities();
		if (!activities.isEmpty()) {
			result = new EccParticipantActivityResultSet(part);
			for (EccActivity a: activities) {
				//using label as name
				if (a.getName().equals(actLabel)) {
					result.addActivity(a);
				}
			}
		}

        return result;
    }


	/**
	 * Get all the applications used by one activity
	 *
	 * @param expID
	 * @param activityIRI
	 * @return
	 * @throws Exception
	 */
    public EccActivityApplicationResultSet getApplicationsUsedByActivity( UUID   expID,
                                                                          String activityIRI) throws Exception {

        EccActivityApplicationResultSet result = null;

		String sparql = "SELECT * WHERE { <" + activityIRI + "> <http://www.w3.org/ns/prov#used> ?app . "
				+ "?app a <http://experimedia.eu/ontologies/ExperimediaExperimentExplorer#Application> . "
				//currently just pulling the entity presentation of the Application
				+ "?app a <http://www.w3.org/ns/prov#Entity> . "
				+ "?app rdfs:label ?label . }";

        TupleQueryResult tqr = store.query(expID.toString(), sparql);

		if (tqr!=null) {

			EccActivity act = getActivity(expID, activityIRI);
			result = new EccActivityApplicationResultSet(act);

			while (tqr.hasNext()) {

				BindingSet tqrb = tqr.next();

				String l = tqrb.getBinding("label").getValue().toString();
				String app = tqrb.getBinding("app").getValue().toString();

				EccApplication application = new EccApplication(l.substring(l.indexOf("\"")+1, l.lastIndexOf("\"")), "TODO: description", app);

				result.addApplication(application);
			}
		}

        return result;
    }

    public EccApplicationServiceResultSet getServicesUsedByAppplication( UUID expID,
                                                                         String appIRI ) {

        EccApplicationServiceResultSet result = null;

        return result;
    }
}
