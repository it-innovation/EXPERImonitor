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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a container for EDM prov elements. It has a default prefix and BaseURI and
 * contains a list of all the namespaces used in its contained elements.
 */
public class EDMProvDataContainer {

	protected static String prefix = null;
	protected static String baseURI = null;
	
	protected HashMap<String, EDMProvBaseElement> allProvElements;
	protected HashMap<String, String> namespaces;
	
	protected Logger logger = LoggerFactory.getLogger(EDMProvDataContainer.class);
	
	public EDMProvDataContainer(String prefix, String baseURI) {
		this.setPrefix(prefix);
		this.setBaseURI(baseURI);
		init();
	}
    
	protected final void init() {
		allProvElements = new HashMap<String, EDMProvBaseElement>();
		namespaces = new HashMap<String, String>();
	}
	
	/**
	 * Clears the factory contents.
	 */
	public void clear() {
		this.allProvElements.clear();
		namespaces.clear();
		prefix = null;
		baseURI = null;
	}
	
	@Override
	public String toString() {
		String contents = "EDMProvDataContainer contents:\n########################\n";
		for (Entry<String, EDMProvBaseElement> e: this.allProvElements.entrySet()) {
			contents += e.getValue().toString();
		}
		return contents;
	}
	
	public void addElement(EDMProvBaseElement element) {
		if (element!=null && element.getIri()!=null) {
			this.allProvElements.put(element.getIri(), element);
		} else {
			logger.warn("Skipping invalid EDMProvBaseElement");
		}
	}
	
	/**
	 * This method adds namespaces to the containers list. It needs to be used by classes that are
	 * using it to keep the list up to date as it is unaware of the short prefixes of contained elements.
	 * 
	 * @param namespaces 
	 */
	public void addNamespaces(HashMap<String, String> namespaces) {
		for (Entry<String, String> e: namespaces.entrySet()) {
			if (!this.namespaces.containsKey(e.getKey())) {
				this.namespaces.put(e.getKey(), e.getValue());
			}
		}
	}
	
	/**
	 * Merges two EDMProvDataContainers. The contents of the argument are written to the object
	 * calling this method. The namespace and baseURI remain unchanged.
	 * 
	 * @param c the EDMProvDataContainer to merge with
	 */
	public void mergeWith(EDMProvDataContainer c) {
		//add namespaces
		for (Entry<String, String> ns: c.getNamespaces().entrySet()) {
			this.namespaces.put(ns.getKey(), ns.getValue());
		}
		//add elements
		for (EDMProvBaseElement e: c.getAllElements().values()) {
			this.addElement(e);
		}
	}
	
	//GETTERS/SETTERS//////////////////////////////////////////////////////////////////////////////

	public final void setPrefix(String prefix) {
		EDMProvDataContainer.prefix = prefix;
	}

	public final void setBaseURI(String baseURI) {
		EDMProvDataContainer.baseURI = baseURI;
	}

	public HashMap<String, EDMProvBaseElement> getAllElements() {
		return allProvElements;
	}

	public HashMap<String, String> getNamespaces() {
		return namespaces;
	}
}
