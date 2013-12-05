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

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.zip.DataFormatException;
import javax.xml.datatype.DatatypeConfigurationException;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple.TRIPLE_TYPE;

/**
 * The EDMProvFactory is a singleton factory which helps create provenance.
 */
public class EDMProvFactory extends AEDMProvFactory {
	
	public static final String FALLBACK_PREFIX = "experimedia";
	public static final String FALLBACK_URI = "http://it-innovation.soton.ac.uk/ontologies/experimedia#";
	
	private static EDMProvFactory factory;

	//related to report
	private HashMap<UUID, EDMTriple> currentTriples;
	private HashMap<UUID, EDMTriple> sentTriples;
	
	private EDMProvFactory(String prefix, String baseURI) {
		//prefix in this context is a short prefix (?)
		super(prefix, baseURI);
		EDMProvFactory.factory = (EDMProvFactory) EDMProvFactory.factory;
		init();
	}
	
	/**
	 * Returns a factory with either the prefix/baseURI it already has or the fallback prefix/baseURI.
	 * 
	 * @return the factory
	 */
	public static synchronized EDMProvFactory getInstance() {
		if (prefix==null) {
			prefix = FALLBACK_PREFIX;
		}
		if (baseURI==null) {
			baseURI = FALLBACK_URI;
		}
		
		return getInstance(prefix, baseURI);
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
		} else {
			factory.setPrefix(prefix);
			factory.setBaseURI(baseURI);
		}
        return (EDMProvFactory) factory;
    }
    
	protected void init() {
		super.init();
		currentTriples = new HashMap<UUID, EDMTriple>();
		sentTriples = new HashMap<UUID, EDMTriple>();
		
		//add primary ontology
		addOntology(prefix, baseURI);
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
		if (!namespaces.containsKey(prefix)) {
			namespaces.put(prefix, baseURI);
		} else {
			if (!namespaces.get(prefix).equals(baseURI)) {
				namespaces.put(prefix, baseURI);
				logger.warning("Prefix " + prefix + " was already in the factory.\n\tOld URI: "
						+ namespaces.get(prefix) + "\n\tnew URI: " + baseURI);
			}
		}
	}
	
	/**
	 * Get an agent (new or existing) identified by its unique ID from the factory
	 * 
	 * @param uniqueIdentifier
	 * @param label
	 * @return the element
	 * @throws DataFormatException
	 * @throws DatatypeConfigurationException
	 */
	public EDMAgent getOrCreateAgent(String uniqueIdentifier, String label) throws DataFormatException, DatatypeConfigurationException {
		return (EDMAgent) this.getOrCreateElement(EDMProvFactory.baseURI, uniqueIdentifier, label, EDMProvBaseElement.PROV_TYPE.ePROV_AGENT);
	}
	
	/**
	 * Get an activity (new or existing) identified by its unique ID from the factory
	 * 
	 * @param uniqueIdentifier
	 * @param label
	 * @return the element
	 * @throws DataFormatException
	 * @throws DatatypeConfigurationException
	 */
	public EDMActivity getOrCreateActivity(String uniqueIdentifier, String label) throws DataFormatException, DatatypeConfigurationException {
		return (EDMActivity) this.getOrCreateElement(EDMProvFactory.baseURI, uniqueIdentifier, label, EDMProvBaseElement.PROV_TYPE.ePROV_ACTIVITY);
	}
	
	/**
	 * Get an entity (new or existing) identified by its unique ID from the factory
	 * 
	 * @param uniqueIdentifier
	 * @param label
	 * @return the element
	 * @throws DataFormatException
	 * @throws DatatypeConfigurationException
	 */
	public EDMEntity getOrCreateEntity(String uniqueIdentifier, String label) throws DataFormatException, DatatypeConfigurationException {
		return  (EDMEntity) this.getOrCreateElement(EDMProvFactory.baseURI, uniqueIdentifier, label, EDMProvBaseElement.PROV_TYPE.ePROV_ENTITY);
	}

	protected EDMProvBaseElement getOrCreateElement(String prefix, String uniqueIdentifier, String label, EDMProvBaseElement.PROV_TYPE type) throws DatatypeConfigurationException, DataFormatException {
		//check if element exists in factory
		if (allProvElements.containsKey(prefix + uniqueIdentifier)) {
			return allProvElements.get(prefix + uniqueIdentifier);
		} else {
			EDMProvBaseElement element = factory.createElement(uniqueIdentifier, label, type);

			String owlClass = "";
			switch (type) {
				case ePROV_ACTIVITY:
					owlClass = "http://www.w3.org/ns/prov#Activity";
					break;
				case ePROV_AGENT:
					owlClass = "http://www.w3.org/ns/prov#Agent";
					break;
				case ePROV_ENTITY:
					owlClass = "http://www.w3.org/ns/prov#Entity";
					break;
				default:
					throw new DataFormatException("Please specify a PROV_TYPE!");
			}
			
			EDMTriple classAssertion = new EDMTriple(element.iri ,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type", owlClass);
			
			//special case: element exists but has additional prov class (agent=entity)
			if (!element.contains(classAssertion)) {
				element.addOwlClass(owlClass);
				logger.info("Element " + element.uniqueIdentifier + " already exists as a " + element.provType
						+ "\n      Additional class " + type + " was added to the element");
			}
      
			return element;
		}
	}
	
	protected EDMProvBaseElement createElement(String uniqueIdentifier, String label, EDMProvBaseElement.PROV_TYPE type) throws DatatypeConfigurationException {
		
		EDMProvBaseElement element = null;
		
		switch (type) {
			case ePROV_AGENT:
				element = new EDMAgent(baseURI, uniqueIdentifier, label);
				break;
			case ePROV_ACTIVITY:
				element = new EDMActivity(baseURI, uniqueIdentifier, label);
				break;
			case ePROV_ENTITY:
				element = new EDMEntity(baseURI, uniqueIdentifier, label);
				break;
			default:
				throw new DatatypeConfigurationException(type + " is not a correct provenance type. Please use ePROV_AGENT, ePROV_ACTIVITY or ePROV_ENTITY.");
		}
		
		if (element!=null) {
			allProvElements.put(element.iri, element);

			for (Entry<UUID, EDMTriple> e: element.triples.entrySet()) {
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
			for (Entry<UUID, EDMTriple> e: element.triples.entrySet()) {
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
		for (EDMProvBaseElement e: allProvElements.values()) {
			elementUpdated(e);
		}

		EDMProvReport report = new EDMProvReport( currentTriples );
		currentTriples.clear();
    
		return report;
	}
	
	/**
	 * Clears the factory contents.
	 */
	public void clear() {
		currentTriples.clear();
		sentTriples.clear();
    
    super.clear();
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
					if ( e.getValue().getObject().startsWith(namespaces.get("prov"))) {
						//get local name
						String localName = EDMTriple.splitURI(e.getValue().getSubject(), 1);
						
						if (e.getValue().getObject().endsWith("#Agent")) {
							factory.getOrCreateAgent(localName, null);
						} else if (e.getValue().getObject().endsWith("#Entity")) {
							factory.getOrCreateEntity(localName, null);
						} else if (e.getValue().getObject().endsWith("#Activity")) {
							factory.getOrCreateActivity(localName, null);
						}
					}
				}
			}
			
			//second run: add triples
			for (EDMTriple triple: element.values()) {
				String iri = triple.getSubject();

				//should only need prefix and unique identifier - can get both from triple
				if (factory.allProvElements.containsKey(iri)) {
					factory.allProvElements.get(iri).addTriple(
						triple.getPredicate(), triple.getObject(), triple.getType());
				}
			}
		}
	}
	
	//GETTERS/SETTERS//////////////////////////////////////////////////////////////////////////////

	public String getPrefix() {
		if (prefix!=null) {
			return prefix;
		} else {
			return FALLBACK_PREFIX;
		}
	}
	
	public String getNamespaceForPrefix(String prefix) throws NoSuchFieldException {
		if (namespaces.containsKey(prefix)) {
			return namespaces.get(prefix);
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
