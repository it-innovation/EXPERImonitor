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
import java.util.zip.DataFormatException;

import javax.xml.datatype.DatatypeConfigurationException;

public class EDMProvFactory {
	
	public static final String FALLBACK_PREFIX = "experimedia";
	
	private static EDMProvFactory factory = null;
	public static String prefix = null;
	private HashMap<String, EDMProvBaseElement> allProvElements;
	private HashMap<String, EDMProvBaseElement> currentProvReportElements;
	
	private EDMProvFactory(String prefix) {
		this.setPrefix(prefix);
		init();
	}
	
	public static synchronized EDMProvFactory getInstance() {
		if (prefix!=null) {
			return getInstance(prefix);
		} else {
			return getInstance(FALLBACK_PREFIX);
		}
	}

	public static synchronized EDMProvFactory getInstance(String prefix) {
		if (factory==null) {
			factory = new EDMProvFactory(prefix);
		}
    
        return factory;
    }
    
	private void init() {
		allProvElements = new HashMap<String, EDMProvBaseElement>();
		currentProvReportElements = new HashMap<String, EDMProvBaseElement>();
	}
	
	public EDMAgent getOrCreateAgent(String uniqueIdentifier, String label) throws DataFormatException, DatatypeConfigurationException {
		return (EDMAgent) this.getOrCreateElement(EDMProvFactory.prefix, uniqueIdentifier, label, EDMProvBaseElement.PROV_TYPE.ePROV_AGENT);
	}
	
	public EDMActivity getOrCreateActivity(String uniqueIdentifier, String label) throws DataFormatException, DatatypeConfigurationException {
		return (EDMActivity) this.getOrCreateElement(EDMProvFactory.prefix, uniqueIdentifier, label, EDMProvBaseElement.PROV_TYPE.ePROV_ACTIVITY);
	}
	
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
			
			if (!element.contains(new EDMProvTriple(prefix + ":" + uniqueIdentifier,"rdf:type",owlClass))) {
				throw new DataFormatException("The prov element you tried to get already exists but is not a " + owlClass);
			}
      
			return element;
		}
	}
	
	public EDMProvBaseElement createElement(String uniqueIdentifier, String label, EDMProvBaseElement.PROV_TYPE type) throws DatatypeConfigurationException {
		
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
			currentProvReportElements.put(prefix + ":" + uniqueIdentifier, element);
		}
		return element;
	}
	
	public void elementUpdated(EDMProvBaseElement element) {
		if (element != null) {
			String iri = element.getIri();
          
			if ( !currentProvReportElements.containsKey(iri) )
				currentProvReportElements.put(iri, element);
		}
	}
  
	public EDMProvReport createProvReport()
	{
		EDMProvReport report = new EDMProvReport( currentProvReportElements );
    
		currentProvReportElements.clear();
    
		return report;
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

}
