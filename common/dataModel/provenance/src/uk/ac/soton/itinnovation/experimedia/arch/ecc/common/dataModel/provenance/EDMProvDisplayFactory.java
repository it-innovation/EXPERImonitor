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
//      Created Date :          27-Nov-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvBaseElement.PROV_TYPE;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple.TRIPLE_TYPE;

/**
 * This class is a prov factory made to handle provenance data which comes from a KB
 * @author sw
 *
 */
public class EDMProvDisplayFactory {
	
	private EDMProvDataContainer container;
	
	public EDMProvDisplayFactory(String prefix, String baseURI) {
		container = new EDMProvDataContainer(prefix, baseURI);
	}
	
	public void loadSPARQLResult(LinkedList<HashMap<String, String>> result) {
		
		//first run: group by element
		HashMap<String, LinkedList<HashMap<String,String>>> elements = new HashMap<String,LinkedList<HashMap<String,String>>>();
		for (HashMap<String, String> row: result) {
			String iri = row.get("s");
			//start element if this is the first triple for this element
			if (!elements.containsKey(iri)) {
				elements.put(iri, new LinkedList<HashMap<String, String>>());
			}
			//add triple
			elements.get(iri).add(row);
		}
		
		//second run: create prov elements
		for (Entry<String, LinkedList<HashMap<String, String>>> element: elements.entrySet()) {

			EDMProvBaseElement newElement = new EDMProvBaseElement(EDMTriple.splitURI(element.getKey(),0),
				EDMTriple.splitURI(element.getKey(),1), null);
			for (HashMap<String, String> triple: element.getValue()) {
				
				//cast to class and set prov type - has to be done first, independently of the class assertion triple
				//reason: a new instance of the actual class is reated which would lose all previously added triples
				if (newElement.getClass().getName().equals(EDMProvBaseElement.class.getName())) {
					if (triple.get("c").equals("http://www.w3.org/ns/prov#Entity")) {
						newElement.provType = PROV_TYPE.ePROV_ENTITY;
						newElement = new EDMEntity(newElement.prefix, newElement.uniqueIdentifier, null);
					} else if (triple.get("c").equals("http://www.w3.org/ns/prov#Activity")) {
						newElement.provType = PROV_TYPE.ePROV_ACTIVITY;
						newElement = new EDMActivity(newElement.prefix, newElement.uniqueIdentifier, null);
					} else if (triple.get("c").equals("http://www.w3.org/ns/prov#Agent")) {
						newElement.provType = PROV_TYPE.ePROV_AGENT;
						newElement = new EDMAgent(newElement.prefix, newElement.uniqueIdentifier, null);
					}
				}
				
				//set label
				if (triple.get("p").equals("http://www.w3.org/2000/01/rdf-schema#label")) {
					newElement.setLabel(triple.get("o"));
				//attach triple	TODO: get xsd:datatype and attach to datatype properties
				} else {
					String type = triple.get("t");
					EDMTriple.TRIPLE_TYPE tripletype = TRIPLE_TYPE.UNKNOWN_TYPE;
					if (type.equals("http://www.w3.org/2002/07/owl#DatatypeProperty")) {
						tripletype = TRIPLE_TYPE.DATA_PROPERTY;
					} else if (type.equals("http://www.w3.org/2002/07/owl#ObjectProperty")) {
						tripletype = TRIPLE_TYPE.OBJECT_PROPERTY;
					} else if (type.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property")) {
						tripletype = TRIPLE_TYPE.CLASS_ASSERTION;
					}
					newElement.addTriple(triple.get("p"), triple.get("o"), tripletype);
				}

				//add to elements
				container.allProvElements.put(newElement.iri, newElement);
			}
		}
	}

}
