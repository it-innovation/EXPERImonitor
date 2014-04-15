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
//      Created Date :          2014-01-20
//      Created for Project :   Experimedia
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.dao;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMActivity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMEntity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvBaseElement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvDataContainer;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple;

/**
 * This class translates the results of a SPARQL query back into the EDMProv data model classes.
 */
public class SPARQLProvTranslator {
	
	private final Properties props;
	private EDMProvDataContainer container;
	private Logger logger;
	
	public SPARQLProvTranslator(Properties props) {
		logger = LoggerFactory.getLogger(getClass());

		this.props = props;
		container = new EDMProvDataContainer(props.getProperty("ont.Prefix"), props.getProperty("ont.BaseURI"));
	}
	
	private void clear() {
		this.container.clear();
	}
	
	private void init() {
		container.setPrefix(props.getProperty("ont.Prefix"));
		container.setBaseURI(props.getProperty("ont.BaseURI"));
	}
	
	/**
	 * Translate a resultset
	 * 
	 * @param sparqlResult the SPARQL resultset, representing rows as a HashMap each, where the key is the variable name
	 * @return the EDMProv elements in a container
	 */
	public EDMProvDataContainer translate(LinkedList<HashMap<String, String>> sparqlResult) {
		
		//start with fresh container
		clear();
		init();
		
		//first run: group by element
		HashMap<String, LinkedList<HashMap<String,String>>> elements = new HashMap<String,LinkedList<HashMap<String,String>>>();
		for (HashMap<String, String> row: sparqlResult) {
			String iri = row.get("s");
			//start element if this is the first triple for this element
			if (!elements.containsKey(iri)) {
				elements.put(iri, new LinkedList<HashMap<String, String>>());
			}
			//add triple
			elements.get(iri).add(row);
		}
		
		//second run: create prov elements
		for (Map.Entry<String, LinkedList<HashMap<String, String>>> element: elements.entrySet()) {

			EDMProvBaseElement newElement = new EDMProvBaseElement(EDMTriple.splitURI(element.getKey(),0),
				EDMTriple.splitURI(element.getKey(),1), null);
			for (HashMap<String, String> triple: element.getValue()) {
				
				//cast to class and set prov type - has to be done first, independently of the class assertion triple
				//reason: a new instance of the actual class is reated which would lose all previously added triples
				if (newElement.getClass().getName().equals(EDMProvBaseElement.class.getName())) {
					if (triple.get("c").equals("http://www.w3.org/ns/prov#Entity")) {
						newElement.setProvType(EDMProvBaseElement.PROV_TYPE.ePROV_ENTITY);
						newElement = new EDMEntity(newElement.getPrefix(), newElement.getUniqueIdentifier(), null);
					} else if (triple.get("c").equals("http://www.w3.org/ns/prov#Activity")) {
						newElement.setProvType(EDMProvBaseElement.PROV_TYPE.ePROV_ACTIVITY);
						newElement = new EDMActivity(newElement.getPrefix(), newElement.getUniqueIdentifier(), null);
					} else if (triple.get("c").equals("http://www.w3.org/ns/prov#Agent")) {
						newElement.setProvType(EDMProvBaseElement.PROV_TYPE.ePROV_AGENT);
						newElement = new EDMAgent(newElement.getPrefix(), newElement.getUniqueIdentifier(), null);
					}
				}
				
				//set label
				if (triple.containsKey("t")) {
					if (triple.get("p").equals("http://www.w3.org/2000/01/rdf-schema#label")) {
						//process string
						String tmp = triple.get("o");
						if (tmp.startsWith("\"")) {
							tmp = tmp.replaceFirst("\"", "");
						}
						if (tmp.contains("\"^^<http://www.w3.org/2001/XMLSchema#string>")) {
							tmp = tmp.replace("\"^^<http://www.w3.org/2001/XMLSchema#string>", "");
						}
						newElement.setLabel(tmp);
					//attach triple
					} else {
						String type = triple.get("t");
						EDMTriple.TRIPLE_TYPE tripletype = EDMTriple.TRIPLE_TYPE.UNKNOWN_TYPE;
						if (type.equals("http://www.w3.org/2002/07/owl#DatatypeProperty")) {
							tripletype = EDMTriple.TRIPLE_TYPE.DATA_PROPERTY;

							//get xsd:datatype and attach to datatype properties
							if (triple.get("o").endsWith("<http://www.w3.org/2001/XMLSchema#dateTime>")) {
								triple.put("o", triple.get("o").substring(0,triple.get("o").lastIndexOf("^")+1)+"xsd:dateTime");
							}

						} else if (type.equals("http://www.w3.org/2002/07/owl#ObjectProperty")) {
							tripletype = EDMTriple.TRIPLE_TYPE.OBJECT_PROPERTY;
						} else if (type.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property")) {
							tripletype = EDMTriple.TRIPLE_TYPE.CLASS_ASSERTION;
						} else {
							logger.warn("Unknown triple type: " + type);
						}
						newElement.addTriple(triple.get("p"), triple.get("o"), tripletype);
					}
				}

				//add to elements
				container.addElement(newElement);
			}
		}
		return container;
	}

	// GETTERS/SETTERS ////////////////////////////////////////////////////////////////////////////
	public EDMProvDataContainer getContainer() {
		return container;
	}
}
