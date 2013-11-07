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
public class EDMProvFactory {
	
	public static final String FALLBACK_PREFIX = "experimedia";
	
	private static EDMProvFactory factory = null;
	public static String prefix = null;
	
	private HashMap<String, EDMProvBaseElement> allProvElements;
	
	//related to report
	private HashMap<UUID, EDMTriple> currentTriples;
	private HashMap<UUID, EDMTriple> sentTriples;
	
	private EDMProvFactory(String prefix) {
		this.setPrefix(prefix);
		init();
	}
	
	/**
	 * Returns a factory with either the prefix it already has or the fallback prefix.
	 * 
	 * @return the factory
	 */
	public static synchronized EDMProvFactory getInstance() {
		if (prefix!=null) {
			return getInstance(prefix);
		} else {
			return getInstance(FALLBACK_PREFIX);
		}
	}

	/**
	 * Returns a factory with the desired prefix. Will override the prefix if it exists.
	 * @param prefix the desired prefix
	 * @return
	 */
	public static synchronized EDMProvFactory getInstance(String prefix) {
		if (factory==null) {
			factory = new EDMProvFactory(prefix);
		} else {
			factory.setPrefix(prefix);
		}
    
        return factory;
    }
    
	private void init() {
		allProvElements = new HashMap<String, EDMProvBaseElement>();
		currentTriples = new HashMap<UUID, EDMTriple>();
		sentTriples = new HashMap<UUID, EDMTriple>();
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
		return (EDMAgent) this.getOrCreateElement(EDMProvFactory.prefix, uniqueIdentifier, label, EDMProvBaseElement.PROV_TYPE.ePROV_AGENT);
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
		return (EDMActivity) this.getOrCreateElement(EDMProvFactory.prefix, uniqueIdentifier, label, EDMProvBaseElement.PROV_TYPE.ePROV_ACTIVITY);
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
		return  (EDMEntity) this.getOrCreateElement(EDMProvFactory.prefix, uniqueIdentifier, label, EDMProvBaseElement.PROV_TYPE.ePROV_ENTITY);
	}

	private EDMProvBaseElement getOrCreateElement(String prefix, String uniqueIdentifier, String label, EDMProvBaseElement.PROV_TYPE type) throws DatatypeConfigurationException, DataFormatException {
		if (allProvElements.containsKey(prefix + ":" + uniqueIdentifier)) {
			return allProvElements.get(prefix + ":" + uniqueIdentifier);
		} else {
			EDMProvBaseElement element = factory.createElement(uniqueIdentifier, label, type);

			String owlClass = "";
			switch (type) {
				case ePROV_ACTIVITY:
					owlClass = "prov:Activity";
					break;
				case ePROV_AGENT:
					owlClass = "prov:Agent";
					break;
				case ePROV_ENTITY:
					owlClass = "prov:Entity";
					break;
				default:
					throw new DataFormatException("Please specify a PROV_TYPE!");
			}
			
			if (!element.contains(new EDMTriple(prefix + ":" + uniqueIdentifier,"rdf:type",owlClass))) {
				throw new DataFormatException("The prov element you tried to get already exists but is not a " + owlClass);
			}
      
			return element;
		}
	}
	
	private EDMProvBaseElement createElement(String uniqueIdentifier, String label, EDMProvBaseElement.PROV_TYPE type) throws DatatypeConfigurationException {
		
		EDMProvBaseElement element = null;
		
		switch (type) {
			case ePROV_AGENT:
				element = new EDMAgent(factory.getPrefix(), uniqueIdentifier, label);
				break;
			case ePROV_ACTIVITY:
				element = new EDMActivity(factory.getPrefix(), uniqueIdentifier, label);
				break;
			case ePROV_ENTITY:
				element = new EDMEntity(factory.getPrefix(), uniqueIdentifier, label);
				break;
			default:
				throw new DatatypeConfigurationException(type + " is not a correct provenance type. Please use ePROV_AGENT, ePROV_ACTIVITY or ePROV_ENTITY.");
		}
		
		if (element!=null) {
			allProvElements.put(prefix + ":" + uniqueIdentifier, element);
			
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
		EDMProvReport report = new EDMProvReport( currentTriples );
		currentTriples.clear();
    
		return report;
	}
	
	/**
	 * Clears the factory contents.
	 */
	public void clear() {
		factory.allProvElements.clear();
		factory.currentTriples.clear();
		factory.sentTriples.clear();
		
		factory = null;
		prefix = null;
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
					if (e.getValue().getObject().startsWith("prov:")) {
						String uniqueId = e.getValue().getSubject();
						if (uniqueId.contains(":")) {
							uniqueId = uniqueId.split(":")[1];
						}
						if (e.getValue().getObject().equals("prov:Agent")) {
							factory.getOrCreateAgent(uniqueId, null);
						} else if (e.getValue().getObject().equals("prov:Entity")) {
							factory.getOrCreateEntity(uniqueId, null);
						} else if (e.getValue().getObject().equals("prov:Activity")) {
							factory.getOrCreateActivity(uniqueId, null);
						}
					}
				}
			}
			
			//second run: add triples
			for (EDMTriple triple: element.values()) {
				String shortIri = triple.getSubject();
				if (shortIri.contains(":")) {
					String prefix = shortIri.split(":")[0];
					String uniqueId = shortIri.split(":")[1];
					//should only need prefix and unique identifier - can get both from triple
					factory.allProvElements.get(prefix + ":" + uniqueId).addTriple(
							triple.getPredicate(), triple.getObject(), triple.getType());
				}
			}
		}
	}
	
	public String toString() {
		String contents = "EDMProvFactory contents:\n########################\n";
		for (Entry<String, EDMProvBaseElement> e: this.allProvElements.entrySet()) {
			contents += e.getValue().toString();
		}
		return contents;
	}
	
	//GETTERS/SETTERS//////////////////////////////////////////////////////////////////////////////

	public String getPrefix() {
		if (prefix!=null) {
			return prefix;
		} else {
			return FALLBACK_PREFIX;
		}
	}

	public void setPrefix(String prefix) {
		EDMProvFactory.prefix = prefix;
	}

	public HashMap<UUID, EDMTriple> getSentTriples() {
		return sentTriples;
	}

	public HashMap<UUID, EDMTriple> getCurrentTriples() {
		return currentTriples;
	}
	
	public HashMap<String, EDMProvBaseElement> getAllElements() {
		return allProvElements;
	}

}
