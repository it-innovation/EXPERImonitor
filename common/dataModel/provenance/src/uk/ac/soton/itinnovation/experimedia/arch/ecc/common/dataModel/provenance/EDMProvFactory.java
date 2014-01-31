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
//      Created Date :          16-Oct-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance;

import java.rmi.AlreadyBoundException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.zip.DataFormatException;
import javax.xml.datatype.DatatypeConfigurationException;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvBaseElement.PROV_TYPE;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple.TRIPLE_TYPE;

/**
 * The EDMProvFactory is a singleton factory which helps create provenance.
 */
public class EDMProvFactory {
	
	public static final String FALLBACK_PREFIX = "experimedia";
	public static final String FALLBACK_URI = "http://it-innovation.soton.ac.uk/ontologies/experimedia#";
	
	private static EDMProvFactory factory;
	public EDMProvDataContainer container;

	//related to report
	private HashMap<UUID, EDMTriple> currentTriples;
	private HashMap<UUID, EDMTriple> sentTriples;
	
	private EDMProvFactory(String prefix, String baseURI) {}
	
	/**
	 * Returns a factory with either the prefix/baseURI it already has or the fallback prefix/baseURI.
	 * 
	 * @return the factory
	 */
	public static synchronized EDMProvFactory getInstance() {

		if (EDMProvDataContainer.prefix==null) {
			EDMProvDataContainer.prefix = FALLBACK_PREFIX;
		}
		if (EDMProvDataContainer.baseURI==null) {
			EDMProvDataContainer.baseURI = FALLBACK_URI;
		}
		
		return getInstance(EDMProvDataContainer.prefix, EDMProvDataContainer.baseURI);
		
	}

	/**
	 * Returns a factory with the desired prefix/baseURI. Will override if exists.
	 * @param prefix the desired prefix
	 * @param baseURI the desired base URI for this factory
	 * @return
	 */
	public static synchronized EDMProvFactory getInstance(String prefix, String baseURI) {
		if (factory==null) {
			factory = new EDMProvFactory(prefix, baseURI);
			factory.container = new EDMProvDataContainer(prefix, baseURI);
			factory.init();
		} else {
			factory.container.setPrefix(prefix);
			factory.container.setBaseURI(baseURI);
		}
        return factory;
    }
    
	protected void init() {
		factory.container.init();
		currentTriples = new HashMap<UUID, EDMTriple>();
		sentTriples = new HashMap<UUID, EDMTriple>();
		
		//add primary ontology
		addOntology(EDMProvDataContainer.prefix, EDMProvDataContainer.baseURI);
		//add standard set on ontologies to use:
		addOntology("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		addOntology("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		addOntology("owl", "http://www.w3.org/2002/07/owl#");
		addOntology("xsd", "http://www.w3.org/2001/XMLSchema#");
		addOntology("prov", "http://www.w3.org/ns/prov#");
		addOntology("dc", "http://purl.org/dc/elements/1.1/");
		addOntology("dcterms", "http://purl.org/dc/terms/");
		addOntology("swrl", "http://www.w3.org/2003/11/swrl#");
		addOntology("swrlb", "http://www.w3.org/2003/11/swrlb#");	
	}
	
	public void addOntology(String prefix, String baseURI) {
		if (!container.namespaces.containsKey(prefix)) {
			container.namespaces.put(prefix, baseURI);
		} else {
			if (!container.namespaces.get(prefix).equals(baseURI)) {
				container.namespaces.put(prefix, baseURI);
				container.logger.warning("Prefix " + prefix + " was already in the factory.\n\tOld URI: "
						+ container.namespaces.get(prefix) + "\n\tnew URI: " + baseURI);
			}
		}
	}
	
	/**
	 * Create a new Agent
	 * 
	 * @param uniqueIdentifier
	 * @param label
	 * @return the agent
	 * @throws DatatypeConfigurationException
	 * @throws AlreadyBoundException 
	 */
	public EDMAgent createAgent(String uniqueIdentifier, String label) throws DatatypeConfigurationException, AlreadyBoundException {
		return (EDMAgent) this.createElement(uniqueIdentifier, label, PROV_TYPE.ePROV_AGENT);
	}
	
	/**
	 * Create a new Activity
	 * 
	 * @param uniqueIdentifier
	 * @param label
	 * @return the activity
	 * @throws DatatypeConfigurationException
	 * @throws AlreadyBoundException 
	 */
	public EDMActivity createActivity(String uniqueIdentifier, String label) throws DatatypeConfigurationException, AlreadyBoundException {
		return (EDMActivity) this.createElement(uniqueIdentifier, label, PROV_TYPE.ePROV_ACTIVITY);
	}
	
	/**
	 * Create a new Entity
	 * 
	 * @param uniqueIdentifier
	 * @param label
	 * @return the entity
	 * @throws DatatypeConfigurationException
	 * @throws AlreadyBoundException 
	 */
	public EDMEntity createEntity(String uniqueIdentifier, String label) throws DatatypeConfigurationException, AlreadyBoundException {
		return (EDMEntity) this.createElement(uniqueIdentifier, label, PROV_TYPE.ePROV_ENTITY);
	}
	
	/**
	 * Get an agent (existing) identified by its unique ID from the factory
	 * 
	 * @param uniqueIdentifier
	 * @param label
	 * @return the element
	 * @throws DataFormatException
	 * @throws DatatypeConfigurationException
	 */
	public EDMAgent getAgent(String uniqueIdentifier) throws DataFormatException, DatatypeConfigurationException {
		return (EDMAgent) this.getElement(EDMProvDataContainer.baseURI + uniqueIdentifier);
	}
	
	/**
	 * Get an activity (existing) identified by its unique ID from the factory
	 * 
	 * @param uniqueIdentifier
	 * @param label
	 * @return the element
	 * @throws DataFormatException
	 * @throws DatatypeConfigurationException
	 */
	public EDMActivity getActivity(String uniqueIdentifier) throws DataFormatException, DatatypeConfigurationException {
		return (EDMActivity) this.getElement(EDMProvDataContainer.baseURI + uniqueIdentifier);
	}
	
	/**
	 * Get an entity (existing) identified by its unique ID from the factory
	 * 
	 * @param uniqueIdentifier
	 * @param label
	 * @return the element
	 * @throws DataFormatException
	 * @throws DatatypeConfigurationException
	 */
	public EDMEntity getEntity(String uniqueIdentifier) throws DataFormatException, DatatypeConfigurationException {
		return (EDMEntity) this.getElement(EDMProvDataContainer.baseURI + uniqueIdentifier);
	}

	protected EDMProvBaseElement getElement(String iri) throws DatatypeConfigurationException, DataFormatException {
		//check if element exists in factory
		if (container.allProvElements.containsKey(iri)) {
			return container.allProvElements.get(iri);
		} else {
			return null;
		}
	}
	
	protected EDMProvBaseElement createElement(String uniqueIdentifier, String label, EDMProvBaseElement.PROV_TYPE type) throws DatatypeConfigurationException, AlreadyBoundException {
		
		//check whether it exists
		if (container.allProvElements.containsKey(EDMProvDataContainer.baseURI + uniqueIdentifier)) {
			throw new AlreadyBoundException("An element with the IRI \"" + EDMProvDataContainer.baseURI
					+ uniqueIdentifier + "\" already exists.");
		}
		
		EDMProvBaseElement element = null;
		
		switch (type) {
			case ePROV_AGENT:
				element = new EDMAgent(EDMProvDataContainer.baseURI, uniqueIdentifier, label);
				break;
			case ePROV_ACTIVITY:
				element = new EDMActivity(EDMProvDataContainer.baseURI, uniqueIdentifier, label);
				break;
			case ePROV_ENTITY:
				element = new EDMEntity(EDMProvDataContainer.baseURI, uniqueIdentifier, label);
				break;
			default:
				throw new DatatypeConfigurationException(type + " is not a correct provenance type. Please use ePROV_AGENT, ePROV_ACTIVITY or ePROV_ENTITY.");
		}
		
		if (element!=null) {
			container.allProvElements.put(element.getIri(), element);

			for (Entry<UUID, EDMTriple> e: element.getTriples().entrySet()) {
				currentTriples.put(e.getKey(), e.getValue());
			}
		}
		return element;
	}
	
	/**
	 * Update prov record after making changes to an element.
	 * 
	 * @param element the element that was updated
	 */
	public void elementUpdated(EDMProvBaseElement element) {
		if (element != null) {

			//add all the triples that haven't been processed yet into the current report
			for (Entry<UUID, EDMTriple> e: element.getTriples().entrySet()) {
				if (!currentTriples.containsKey(e.getKey()) && !sentTriples.containsKey(e.getKey())) {
					currentTriples.put(e.getKey(), e.getValue());
				}
			}
		}
	}
  
	/**
	 * Create a prov report which contains all the elements that have been changed since the last time a report was created.
	 * @return the report
	 */
	public EDMProvReport createProvReport()
	{
		//update all elements before creating prov report to ensure all triples are contained
		for (EDMProvBaseElement e: container.allProvElements.values()) {
			elementUpdated(e);
		}

		EDMProvReport report = new EDMProvReport( currentTriples );
		
		//System.out.println("Current: " + currentTriples.size() + ", Sent: " + sentTriples.size());
		sentTriples.putAll(currentTriples);
		currentTriples.clear();
		//System.out.println("Current: " + currentTriples.size() + ", Sent: " + sentTriples.size());
		
		return report;
	}
	
	/**
	 * Clears the factory contents.
	 */
	public void clear() {
		currentTriples.clear();
		sentTriples.clear();
    
		factory.container.clear();
	}
	
	public void loadReport(EDMProvReport report) throws DataFormatException, DatatypeConfigurationException {
		
		HashMap<String, HashMap<UUID, EDMTriple>> elements = new HashMap<String, HashMap<UUID,EDMTriple>>();
		
		//first group triples by element
		for (Entry<UUID, EDMTriple> e: report.getTriples().entrySet()) {
			if (!elements.containsKey(e.getValue().getSubject())) {
				elements.put(e.getValue().getSubject(), new HashMap<UUID, EDMTriple>());
			}
			elements.get(e.getValue().getSubject()).put(e.getKey(), e.getValue());
		}
		
		//look at each element identified in the factory
		for (HashMap<UUID, EDMTriple> element: elements.values()) {
			
			//first run: identify prov elements
			for (Entry<UUID, EDMTriple> e: element.entrySet()) {
				if (e.getValue().getType().equals(TRIPLE_TYPE.CLASS_ASSERTION)) {
					//look at prov classes only
					if ( e.getValue().getObject().startsWith(container.namespaces.get("prov"))) {
						//get local name
						String localName = EDMTriple.splitURI(e.getValue().getSubject(), 1);
						
						if (e.getValue().getObject().endsWith("#Agent")) {
							factory.getAgent(localName);
						} else if (e.getValue().getObject().endsWith("#Entity")) {
							factory.getEntity(localName);
						} else if (e.getValue().getObject().endsWith("#Activity")) {
							factory.getActivity(localName);
						}
					}
				}
			}
			
			//second run: add triples
			for (EDMTriple triple: element.values()) {
				String iri = triple.getSubject();

				//should only need prefix and unique identifier - can get both from triple
				if (factory.container.allProvElements.containsKey(iri)) {
					factory.container.allProvElements.get(iri).addTriple(
						triple.getPredicate(), triple.getObject(), triple.getType());
				}
			}
		}
	}
	
	//GETTERS/SETTERS//////////////////////////////////////////////////////////////////////////////

	public String getPrefix() {
		if (EDMProvDataContainer.prefix!=null) {
			return EDMProvDataContainer.prefix;
		} else {
			return FALLBACK_PREFIX;
		}
	}
	
	public String getNamespaceForPrefix(String prefix) throws NoSuchFieldException {
		if (container.namespaces.containsKey(prefix)) {
			return container.namespaces.get(prefix);
		} else {
			throw new NoSuchFieldException("The prefix '" + prefix + "' doesn't exist in this factory");
		}
	}
	
	public HashMap<UUID, EDMTriple> getSentTriples() {
		return sentTriples;
	}

	public HashMap<UUID, EDMTriple> getCurrentTriples() {
		return currentTriples;
	}

}
