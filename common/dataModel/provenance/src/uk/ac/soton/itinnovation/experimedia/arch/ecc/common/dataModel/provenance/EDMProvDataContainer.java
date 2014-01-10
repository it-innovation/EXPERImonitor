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
import java.util.logging.Logger;

public class EDMProvDataContainer {

	protected static String prefix = null;
	protected static String baseURI = null;
	
	protected HashMap<String, EDMProvBaseElement> allProvElements;
	protected HashMap<String, String> namespaces;
	
	protected Logger logger = Logger.getLogger(EDMProvDataContainer.class.getName());
	
	protected EDMProvDataContainer(String prefix, String baseURI) {
		this.setPrefix(prefix);
		this.setBaseURI(baseURI);
		init();
	}
    
	protected void init() {
		allProvElements = new HashMap<String, EDMProvBaseElement>();
		namespaces = new HashMap<String, String>();
	}
	
	/**
	 * Clears the factory contents.
	 */
	public void clear() {
		this.allProvElements.clear();
		prefix = null;
		baseURI = null;
	}
	
	public String toString() {
		String contents = "EDMProvFactory contents:\n########################\n";
		for (Entry<String, EDMProvBaseElement> e: this.allProvElements.entrySet()) {
			contents += e.getValue().toString();
		}
		return contents;
	}
	
	//GETTERS/SETTERS//////////////////////////////////////////////////////////////////////////////

	public void setPrefix(String prefix) {
		EDMProvDataContainer.prefix = prefix;
	}

	public void setBaseURI(String baseURI) {
		EDMProvDataContainer.baseURI = baseURI;
	}

	public HashMap<String, EDMProvBaseElement> getAllElements() {
		return allProvElements;
	}

	public HashMap<String, String> getNamespaces() {
		return namespaces;
	}
	
	public void addNamespaces(HashMap<String, String> namespaces) {
		for (Entry<String, String> e: namespaces.entrySet()) {
			this.namespaces.put(e.getKey(), e.getValue());
		}
	}

}
