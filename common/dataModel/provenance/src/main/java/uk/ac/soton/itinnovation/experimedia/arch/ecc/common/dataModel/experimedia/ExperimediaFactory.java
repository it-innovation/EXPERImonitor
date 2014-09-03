/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
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
//      Created Date :          30-07-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experimedia;

import java.rmi.AlreadyBoundException;
import java.util.UUID;
import java.util.zip.DataFormatException;
import javax.xml.datatype.DatatypeConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.*;

/**
 * The ExperimediaFactory is a factory to create experimedia prov patterns comfortably
 */
public class ExperimediaFactory {

	private final EDMProvFactory provFactory;
	private static final Logger logger = LoggerFactory.getLogger(ExperimediaFactory.class);

	public ExperimediaFactory(String prefix, String baseURI) {
		provFactory = EDMProvFactory.getInstance(prefix, baseURI);
	}

	/**
	 * Creates a participant
	 * @param name the (human readable) name of the participant
	 * @return the participant
	 */
	public Participant createParticipant(String name) {
		Participant participant = new Participant();
		try {
			participant.agent = provFactory.createAgent("agent_" + UUID.randomUUID(), name);
			participant.agent.addOwlClass(provFactory.getNamespaceForPrefix("foaf") + "Person");
			participant.agent.addOwlClass(provFactory.getNamespaceForPrefix("eee") + "Participant");
		} catch (DatatypeConfigurationException | AlreadyBoundException | NoSuchFieldException e) {
			logger.error("Error creating participant", e);
			participant = null;
		}
		return participant;
	}

	/**
	 * Creates an experimedia service including all required triples.
	 *
	 * @param uniqueIdentifier the local name of the service
	 * @param label a human readable label
	 * @return the service object
	 */
	public Service createService(String uniqueIdentifier, String label) {
		Service service = new Service();
		try {
			service.entity = provFactory.createEntity(uniqueIdentifier, label);
			service.entity.addOwlClass(provFactory.getNamespaceForPrefix("eee") + "Service");
		} catch (DatatypeConfigurationException | AlreadyBoundException | NoSuchFieldException e) {
			logger.error("Could not create service", e);
			service = null;
		}
		return service;
	}

	/**
	 * Creates an application including all the required EDMProv elements: agent, entity
	 * and the activity of running the app, started by the participant running it.
	 *
	 * @param participant the participant who runs the application
	 * @param name the human readable name of the application
	 * @param timestamp the time the application is started
	 * @param duration in seconds
	 * @return the application
	 */
	public Application createApplication(Participant participant, String name, String timestamp, String duration) {
		Application app = new Application();
		String appName = name.trim().substring(0,1).toUpperCase() + name.replace(" ", "").substring(1).toLowerCase();
		try {

			if (duration==null) {
				app.activity = participant.agent.doDiscreteActivity("Start" + appName + "Activity_"
						+ UUID.randomUUID(), "Start " + name + " activity", timestamp);
			} else {
				app.activity = participant.agent.startActivity("Start" + appName + "Activity_"
						+ UUID.randomUUID(), "Start " + name + " activity", timestamp);
				participant.agent.stopActivity(app.activity, getEndTimestamp(timestamp, duration));
			}

			app.entity = app.activity.generateEntity("entity_" + UUID.randomUUID(), name, timestamp);
			app.entity.addOwlClass(provFactory.getNamespaceForPrefix("eee") + "Application");
			app.agent = provFactory.createAgent("agent_" + UUID.randomUUID(), name);
			app.agent.addTriple(provFactory.getNamespaceForPrefix("owl") + "sameAs",
					app.entity.getIri(), EDMTriple.TRIPLE_TYPE.OBJECT_PROPERTY);
			app.agent.actOnBehalfOf(participant.agent);
		} catch (DataFormatException | DatatypeConfigurationException | AlreadyBoundException | NoSuchFieldException e) {
			logger.error("Error creating application", e);
			app = null;
		}
		return app;
	}

	public Application createApplication(Participant participant, String name, String timestamp) {
		return createApplication(participant, name, timestamp, null);
	}

	/**
	 * Make the participant create data on the client without sending it to the service
	 *
	 * @param participant the participant
	 * @param client the client which the participant is using
	 * @param dataName the name of the data to be used for labelling
	 * @param timestamp the unix timestamp of this discrete useData (start=end)
	 * @param duration in seconds
	 *
	 * @return the data created on the client
	 */
	public Content createDataOnClient(Participant participant, Application client, String dataName, String timestamp, String duration) {

		Content data = new Content();
		try {
			String dName = dataName.substring(0,1).toUpperCase() + dataName.replaceAll(" ", "").substring(1);
			EDMActivity a;

			if (duration==null) {
				a = participant.agent.doDiscreteActivity("Create" + dName + "Activity_" + UUID.randomUUID(), "Create " + dataName, timestamp);
			} else {
				a = participant.agent.startActivity("Create" + dName + "Activity_" + UUID.randomUUID(), "Create " + dataName, timestamp);
				participant.agent.stopActivity(a, getEndTimestamp(timestamp, duration));
			}

			a.useEntity(client.entity);
			data.entity = a.generateEntity("Content_" + UUID.randomUUID(), dataName);
			data.entity.addOwlClass(provFactory.getNamespaceForPrefix("eee") + "Content");
		} catch (DataFormatException | DatatypeConfigurationException | AlreadyBoundException | NoSuchFieldException e) {
			logger.error("Error creating \"create data on client\" pattern", e);
			data = null;
		}
		return data;
	}

	public Content createDataOnClient(Participant participant, Application client, String dataName, String timestamp) {
		return createDataOnClient(participant, client, dataName, timestamp, null);
	}

	/**
	 * Creates a (real world) entity. Note that this should not be used to create Content;
	 * use the appropriate methods for this (e.g. createDataAtService, ...).
	 *
	 * @param uniqueIdentifier the local name of the entity
	 * @param label a human readable label
	 * @return the entity
	 */
	public Entity createEntity(String uniqueIdentifier, String label) {
		Entity entity = new Entity();
		try {
			entity.entity = provFactory.createEntity(uniqueIdentifier, label);
		} catch (DatatypeConfigurationException | AlreadyBoundException e) {
			logger.error("Error creating entity", e);
			entity = null;
		}
		return entity;
	}

	/**
	 * Make the participant use data on the client
	 *
	 * @param participant the participant
	 * @param client the application which the participant is using
	 * @param data the content which is used
	 * @param dataName the name of the data to be used for labelling
	 * @param timestamp the unix timestamp of this discrete useData (start=end)
	 * @param duration in seconds
	 * @return the useData of using the data
	 */
	public Activity useDataOnClient(Participant participant, Application client, Content data,
			String dataName, String timestamp, String duration) {

		Activity useData = new Activity();
		try {
			String dName = dataName.substring(0,1).toUpperCase() + dataName.replaceAll(" ", "").substring(1);

			if (duration==null) {
				useData.activity = participant.agent.doDiscreteActivity("Use" + dName + "Activity_" + UUID.randomUUID(), "Use " + dataName, timestamp);
			} else {
				useData.activity = participant.agent.startActivity("Use" + dName + "Activity_" + UUID.randomUUID(), "Use " + dataName, timestamp);
				participant.agent.stopActivity(useData.activity, getEndTimestamp(timestamp, duration));
			}

			useData.activity.useEntity(data.entity);
			useData.activity.useEntity(client.entity);
		} catch (DataFormatException | DatatypeConfigurationException | AlreadyBoundException e) {
			logger.error("Error creating \"use data on client\" pattern", e);
		}
		return useData;
	}

	public Activity useDataOnClient(Participant participant, Application client, Content data, String dataName, String timestamp) {
		return useDataOnClient(participant, client, data, dataName, timestamp, null);
	}

	/**
	 * Make a participant use a real world entity
	 *
	 * @param participant the participant
	 * @param entity the entity to be used
	 * @param timestamp the unix timestamp of this discrete useData (start=end)
	 * @param duration in seconds
	 *
	 * @return the useData of the agent using the entity
	 */
	public Activity useRealWorldEntity(Participant participant, Entity entity, String timestamp, String duration) {

		Activity useEntity = new Activity();
		try {

			if (duration==null) {
				useEntity.activity = participant.agent.doDiscreteActivity("useSkiliftActivity_" +
					UUID.randomUUID(), "Use " + entity.entity.getFriendlyName(), timestamp);
			} else {
				useEntity.activity = participant.agent.startActivity("useSkiliftActivity_" +
					UUID.randomUUID(), "Use " + entity.entity.getFriendlyName(), timestamp);
				participant.agent.stopActivity(useEntity.activity, getEndTimestamp(timestamp, duration));
			}

			useEntity.activity.useEntity(entity.entity);
		} catch (DataFormatException | DatatypeConfigurationException | AlreadyBoundException e) {
			logger.error("Error creating \"use entity\" pattern", e);
		}
		return useEntity;
	}

	public Activity useRealWorldEntity(Participant participant, Entity entity, String timestamp) {
		return useRealWorldEntity(participant, entity, timestamp, null);
	}

	/**
	 * Make the participant navigate the client
	 *
	 * @param participant the participant
	 * @param client the application which the participant is using
	 * @param timestamp the unix timestamp of this discrete activity (start=end)
	 * @param duration in seconds
	 * @return the activity of navigating
	 */
	public Activity navigateClient(Participant participant, Application client, String timestamp, String duration) {

		Activity navigate = new Activity();
		try {

			if (duration==null) {
				navigate.activity = participant.agent.doDiscreteActivity("NavigateClientActitivy_"
					+ UUID.randomUUID(), "Navigate " + client.entity.getFriendlyName(), timestamp);
			} else {
				navigate.activity = participant.agent.startActivity("NavigateClientActitivy_"
					+ UUID.randomUUID(), "Navigate " + client.entity.getFriendlyName(), timestamp);
				participant.agent.stopActivity(navigate.activity, getEndTimestamp(timestamp, duration));
			}

			navigate.activity.useEntity(client.entity);
		} catch (DataFormatException | DatatypeConfigurationException | AlreadyBoundException e) {
			logger.error("Error creating \"navigate client\" pattern", e);
		}
		return navigate;
	}

	public Activity navigateClient(Participant participant, Application client, String timestamp) {
		return navigateClient(participant, client, timestamp, null);
	}

	/**
	 * Creates the "Application Service Interaction (retrieve data from service)" pattern.
	 *
	 * @param participant the participant
	 * @param app agent representation of the client
	 * @param service the service
	 * @param activityName the name of the useData to be used for labelling
	 * @param timestamp the unix timestamp of this discrete useData (start=end)
	 * @param duration in seconds
	 *
	 * @return the data retrieved from the service
	 */
	public Content retrieveDataFromService(Participant participant, Application app,
			Service service, String activityName, String timestamp, String duration) {

		Content data = new Content();
		try {
			String actName = activityName.substring(0,1).toUpperCase() + activityName.substring(1);
			EDMActivity a;
			EDMActivity b;

			if (duration==null) {
				a = participant.agent.doDiscreteActivity("Receive" + actName + "Activity_"
					+ UUID.randomUUID(), "Receive " + activityName, timestamp);
				b = app.agent.doDiscreteActivity("Retrieve" + actName + "Activity_"
					+ UUID.randomUUID(), "Retrieve " + activityName, timestamp);
			} else {
				a = participant.agent.startActivity("Receive" + actName + "Activity_"
					+ UUID.randomUUID(), "Receive " + activityName, timestamp);
				participant.agent.stopActivity(a, getEndTimestamp(timestamp, duration));
				b = app.agent.startActivity("Retrieve" + actName + "Activity_"
					+ UUID.randomUUID(), "Retrieve " + activityName, timestamp);
				app.agent.stopActivity(b, getEndTimestamp(timestamp, duration));
			}

			a.useEntity(app.entity);
			a.informActivity(b);
			b.useEntity(service.entity);
			data.entity = b.generateEntity(actName + "Data_" + UUID.randomUUID(), actName + " data", timestamp);
			data.entity.addOwlClass(provFactory.getNamespaceForPrefix("eee") + "Content");
			a.useEntity(data.entity);
		} catch (DataFormatException | DatatypeConfigurationException | AlreadyBoundException | NoSuchFieldException e) {
			logger.error("Error creating \"retrieve data from service\" pattern", e);
		}
		return data;
	}

	public Content retrieveDataFromService(Participant participant, Application app,
			Service service, String activityName, String timestamp) {
		return retrieveDataFromService(participant, app, service, activityName, timestamp, null);
	}

	/**
	 * Creates the "Application Service Interaction (create data at service)" pattern.
	 *
	 * @param participant the participant
	 * @param app the application
	 * @param service the service
	 * @param activityName the name of the useData to be used for labelling
	 * @param timestamp the unix timestamp of this discrete useData (start=end)
	 * @param duration in seconds
	 *
	 * @return the data created in the process
	 */
	public Content createDataAtService(Participant participant, Application app,
			 Service service, String activityName, String timestamp, String duration)	{

		Content data = new Content();

		try {
			String actName = activityName.substring(0,1).toUpperCase() + activityName.substring(1);

			EDMActivity a;
			EDMActivity b;

			if (duration==null) {
				a = participant.agent.doDiscreteActivity("Create" + actName + "Activity_"
					+ UUID.randomUUID(), participant.agent.getFriendlyName() + " tweets", timestamp);
				b = app.agent.doDiscreteActivity("Send" + actName + "Activity_"
					+ UUID.randomUUID(), "Send " + activityName + " to server", timestamp);
			} else {
				a = participant.agent.startActivity("Create" + actName + "Activity_"
					+ UUID.randomUUID(), participant.agent.getFriendlyName() + " tweets", timestamp);
				participant.agent.stopActivity(a, getEndTimestamp(timestamp, duration));
				b = app.agent.startActivity("Send" + actName + "Activity_"
					+ UUID.randomUUID(), "Send " + activityName + " to server", timestamp);
				app.agent.stopActivity(b, getEndTimestamp(timestamp, duration));
			}

			a.useEntity(app.entity);
			data.entity = a.generateEntity(actName + "_" + UUID.randomUUID(),
					participant.agent.getFriendlyName() + "'s " + activityName, timestamp);
			data.entity.addOwlClass(provFactory.getNamespaceForPrefix("eee") + "Content");
			b.useEntity(service.entity);
			b.useEntity(data.entity);
			a.informActivity(b);
		} catch (DataFormatException | DatatypeConfigurationException | AlreadyBoundException | NoSuchFieldException e) {
			logger.error("Error creating \"create data at service\" pattern", e);
		}

		return data;
	}

	public Content createDataAtService(Participant participant, Application app,
			 Service service, String activityName, String timestamp) {
		return  createDataAtService(participant, app, service, activityName, timestamp, null);
	}

	private String getEndTimestamp(String start, String duration) {

		Long startLong = Long.valueOf(start);
		Long durLong = Long.valueOf(duration);
		Long endLong = startLong + durLong;

		return endLong.toString();
	}

	public EDMProvFactory getProvFactory() {
		return this.provFactory;
	}
}
