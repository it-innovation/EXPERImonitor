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
 * The ExperimediaFactory is a factory to create experimedia prov patterns
 * comfortably
 */
public class ExperimediaFactory {

    private final EDMProvFactory provFactory;
    private static final Logger logger = LoggerFactory.getLogger(ExperimediaFactory.class);

    public ExperimediaFactory(String prefix, String baseURI) {
        provFactory = EDMProvFactory.getInstance(prefix, baseURI);
    }

    /**
     * Creates a participant along with all necessary triples. 
     * The participant's IRI is the factory's baseIRI plus a random element.
     *
     * @param name the (human readable) name of the participant
     * @return the participant
     */
    public Participant createParticipant(String name) {
        Participant participant = new Participant();
        try {
            participant.agent = provFactory.createAgent("participant_agent_" + UUID.randomUUID(), name);
            participant.agent.addOwlClass(provFactory.getNamespaceForPrefix("foaf") + "Person");
            participant.agent.addOwlClass(provFactory.getNamespaceForPrefix("eee") + "Participant");
        } catch (DatatypeConfigurationException | AlreadyBoundException | NoSuchFieldException e) {
            logger.error("Error creating participant", e);
            participant = null;
        }
        return participant;
    }

    /**
     * Creates a participant.
     *
     * @param iri the IRI for the participant
     * @param name the (human readable) name of the participant
     * @return the participant
     */
    public Participant createParticipant(String iri, String name) {
        Participant participant = new Participant();
        try {
            participant.agent = provFactory.createAgentWithIRI(iri, name);
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
     * The service's IRI is the factory's baseIRI plus a random element.
     *
     * @param label a human readable label
     * @return the service object
     */
    public Service createService(String label) {
        Service service = new Service();
        try {
            service.entity = provFactory.createEntity("service_entity_" + UUID.randomUUID(), label);
            service.entity.addOwlClass(provFactory.getNamespaceForPrefix("eee") + "Service");
        } catch (DatatypeConfigurationException | AlreadyBoundException | NoSuchFieldException e) {
            logger.error("Could not create service", e);
            service = null;
        }
        return service;
    }

    /**
     * Creates an experimedia service including all required triples.
     * The service's IRI is the provided URI.
     *
     * @param uri the service's uri
     * @param label a human readable label
     * @return the service object
     */
    public Service createService(String uri, String label) {
        Service service = new Service();
        try {
            service.entity = provFactory.createEntityWithIRI(uri, label);
            service.entity.addOwlClass(provFactory.getNamespaceForPrefix("eee") + "Service");
        } catch (DatatypeConfigurationException | AlreadyBoundException | NoSuchFieldException e) {
            logger.error("Could not create service", e);
            service = null;
        }
        return service;
    }

    /**
     * Creates an application including all the required EDMProv elements:
     * agent, entity and the activity of running the app, started by the
     * participant running it.
     *
     * @param participant the participant who runs the application
     * @param name the human readable name of the application
     * @param timestamp the time the application is started
     * @return the application
     */
    public Application createApplication(Participant participant, String name, String timestamp) {
        Application app = new Application();
        String appName = name.trim().substring(0, 1).toUpperCase() + name.replace(" ", "").substring(1).toLowerCase();
        try {
            app.activity = participant.agent.startActivity("Using" + appName + "Activity_"
                    + UUID.randomUUID(), "Using " + name + " activity", timestamp);
            app.entity = app.activity.generateEntity("app_entity_" + UUID.randomUUID(), name, timestamp);
            app.entity.addOwlClass(provFactory.getNamespaceForPrefix("eee") + "Application");
            app.agent = provFactory.createAgent("app_agent_" + UUID.randomUUID(), name);
            app.agent.addTriple(provFactory.getNamespaceForPrefix("owl") + "sameAs",
                    app.entity.getIri(), EDMTriple.TRIPLE_TYPE.OBJECT_PROPERTY);
            app.agent.actOnBehalfOf(participant.agent);
            app.participant = participant;
        } catch (DataFormatException | DatatypeConfigurationException | AlreadyBoundException | NoSuchFieldException e) {
            logger.error("Error creating application", e);
            app = null;
        }
        return app;
    }

    /**
     * Destroys the application instance. Stops the application activity and
     * invalidates the entity. We assume that the same Participant stops the app
     * as starts it.
     *
     * @param app the Application to be destroyed
     * @param timestamp the time the application is destroyed
     * @throws java.util.zip.DataFormatException
     */
    public void destroyApplication(Application app, String timestamp) throws DataFormatException {
        app.participant.agent.stopActivity(app.activity, timestamp);
        app.activity.invalidateEntity(app.entity);
    }

    /**
     *
     * Creates an application including all the required EDMProv elements:
     * agent, entity and the activity of running the app, started by the
     * participant running it.
     *
     * @param participant the participant who runs the application
     * @param name the human readable name of the application
     * @param timestamp the time the application is started
     * @param duration the number of seconds the app exists for
     * @return the Application
     * @throws DataFormatException
     */
    public Application createAndDestroyApplication(Participant participant, String name, String timestamp, String duration) throws DataFormatException {
        Application app = createApplication(participant, name, timestamp);
        destroyApplication(app, getEndTimestamp(timestamp, duration));
        return app;
    }

    /**
     * Make the participant create data on the client without sending it to the
     * service
     *
     * @param participant the participant
     * @param client the client which the participant is using
     * @param dataIRI the IRI for the Content (if null then one is generated)
     * @param dataName the name for the Content to be used as the label
     * @param timestamp the unix timestamp of this discrete useData (start=end)
     * @param duration in seconds (can be null)
     *
     * @return the data created on the client
     */
    public Content createDataOnClient(Participant participant, Application client, String dataIRI, String dataName, String timestamp, String duration) {

        Content data = new Content();
        try {
            String dName = dataName.substring(0, 1).toUpperCase() + dataName.replaceAll("\\s+", "").substring(1);
            EDMActivity a;

            if (duration == null) {
                a = participant.agent.doDiscreteActivity("Create" + dName + "Activity_" + UUID.randomUUID(), "Create " + dataName, timestamp);
            } else {
                a = participant.agent.startActivity("Create" + dName + "Activity_" + UUID.randomUUID(), "Create " + dataName, timestamp);
                participant.agent.stopActivity(a, getEndTimestamp(timestamp, duration));
            }

            a.useEntity(client.entity);
            if (dataIRI == null) {
                data.entity = a.generateEntity("Content_" + UUID.randomUUID(), dataName, timestamp);
            } else {
                data.entity = a.generateEntityWithIRI(dataIRI, dataName, timestamp);
            }
            data.entity.addOwlClass(provFactory.getNamespaceForPrefix("eee") + "Content");  //TODO: move to Content constructor!
        } catch (DataFormatException | DatatypeConfigurationException | AlreadyBoundException | NoSuchFieldException e) {
            logger.error("Error creating \"create data on client\" pattern", e);
            data = null;
        }
        return data;
    }

    public Content createDataOnClient(Participant participant, Application client, String dataName, String timestamp) {
        return createDataOnClient(participant, client, null, dataName, timestamp, null);
    }

    /**
     * Creates a (real world) entity. Note that this should not be used to
     * create Content; use the appropriate methods for this (e.g.
     * createDataAtService, ...).
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
            String dName = dataName.substring(0, 1).toUpperCase() + dataName.replaceAll(" ", "").substring(1);

            if (duration == null) {
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
     * @return the useData Activity of the agent using the entity
     */
    public Activity useRealWorldEntity(Participant participant, Entity entity, String timestamp, String duration) {

        Activity useEntity = new Activity();
        try {

            if (duration == null) {
                useEntity.activity = participant.agent.doDiscreteActivity("UseActivity_"
                        + UUID.randomUUID(), "Use " + entity.entity.getFriendlyName(), timestamp);
            } else {
                useEntity.activity = participant.agent.startActivity("UseActivity_"
                        + UUID.randomUUID(), "Use " + entity.entity.getFriendlyName(), timestamp);
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

            if (duration == null) {
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
     * Creates the "Application Service Interaction (retrieve data from
     * service)" pattern.
     *
     * @param participant the participant
     * @param app agent representation of the client
     * @param service the service
     * @param dataIRI the IRI for the Content (if null then one is created)
     * @param dataName the name for the Content to be used as the label
     * @param timestamp the unix timestamp of this discrete useData (start=end)
     * @param duration in seconds (can be null)
     *
     * @return the data retrieved from the service
     */
    public Content retrieveDataFromService(Participant participant, Application app,
            Service service, String dataIRI, String dataName, String timestamp, String duration) {

        Content data = new Content();
        try {
            String dName = dataName.substring(0, 1).toUpperCase() + dataName.replaceAll("\\s+", "").substring(1);

            EDMActivity a;
            EDMActivity b;

            if (duration == null) {
                a = participant.agent.doDiscreteActivity("Receive" + dName + "Activity_" + UUID.randomUUID(), "Receive " + dataName, timestamp);
                b = app.agent.doDiscreteActivity("Retrieve" + dName + "Activity_" + UUID.randomUUID(), "Retrieve " + dataName, timestamp);
            } else {
                a = participant.agent.startActivity("Receive" + dName + "Activity_" + UUID.randomUUID(), "Receive " + dataName, timestamp);
                participant.agent.stopActivity(a, getEndTimestamp(timestamp, duration));
                b = app.agent.startActivity("Retrieve" + dName + "Activity_" + UUID.randomUUID(), "Retrieve " + dataName, timestamp);
                app.agent.stopActivity(b, getEndTimestamp(timestamp, duration));
            }

            a.useEntity(app.entity);
            a.informActivity(b);
            b.useEntity(service.entity);
            if (dataIRI == null) {
                data.entity = b.generateEntity("Content_" + UUID.randomUUID(), dataName, timestamp);
            } else {
                data.entity = b.generateEntityWithIRI(dataIRI, dataName, timestamp);
            }
            data.entity.addOwlClass(provFactory.getNamespaceForPrefix("eee") + "Content");
            a.useEntity(data.entity);
        } catch (DataFormatException | DatatypeConfigurationException | AlreadyBoundException | NoSuchFieldException e) {
            logger.error("Error creating \"retrieve data from service\" pattern", e);
        }
        return data;
    }

    public Content retrieveDataFromService(Participant participant, Application app,
            Service service, String dataIRI, String dataName, String timestamp) {
        return retrieveDataFromService(participant, app, service, dataIRI, dataName, timestamp, null);
    }

    /**
     * Creates the "Application Service Interaction (create data at service)"
     * pattern.
     *
     * @param participant the participant
     * @param app the application
     * @param service the service
     * @param dataIRI the IRI for the Content (if null then one is generated)
     * @param dataName the name for the Content to be used as the label
     * @param timestamp the unix timestamp of this discrete useData (start=end)
     * @param duration in seconds (can be null)
     *
     * @return the data created in the process
     */
    public Content createDataAtService(Participant participant, Application app,
            Service service, String dataIRI, String dataName, String timestamp, String duration) {

        Content data = new Content();

        try {
            String dName = dataName.substring(0, 1).toUpperCase() + dataName.replaceAll("\\s+", "").substring(1);

            EDMActivity a;
            EDMActivity b;

            if (duration == null) {
                a = participant.agent.doDiscreteActivity("Create" + dName + "Activity_" + UUID.randomUUID(), "Create " + dataName, timestamp);
                b = app.agent.doDiscreteActivity("Send" + dName + "Activity_" + UUID.randomUUID(), "Send " + dataName + " to server", timestamp);
            } else {
                a = participant.agent.startActivity("Create" + dName + "Activity_" + UUID.randomUUID(), "Create " + dataName, timestamp);
                participant.agent.stopActivity(a, getEndTimestamp(timestamp, duration));
                b = app.agent.startActivity("Send" + dName + "Activity_" + UUID.randomUUID(), "Send " + dataName + " to server", timestamp);
                app.agent.stopActivity(b, getEndTimestamp(timestamp, duration));
            }

            a.useEntity(app.entity);
            if (dataIRI == null) {
                data.entity = a.generateEntity("Content_" + UUID.randomUUID(), dataName, timestamp);
            } else {
                data.entity = a.generateEntityWithIRI(dataIRI, dataName, timestamp);
            }
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
            Service service, String dataIRI, String dataName, String timestamp) {
        return createDataAtService(participant, app, service, dataIRI, dataName, timestamp, null);
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
