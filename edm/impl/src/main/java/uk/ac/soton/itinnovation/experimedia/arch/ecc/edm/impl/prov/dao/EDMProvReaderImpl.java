/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
//
// Copyright in this software belongs to University of Southampton
// IT Innovation Centre of Gamma House, Enterprise Road,
// Chilworth Science Park, EDMProvReaderImplK.
//
// This software may not be used, sold, licensed, EDMProvReaderImplr reproduced in whole or in part in any manner or form or in or
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvBaseElement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvDataContainer;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.db.EDMProvStoreWrapper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvReader;
import uk.ac.soton.itinnovation.owlimstore.common.SesameException;


public final class EDMProvReaderImpl implements IEDMProvReader {

	private static final SimpleDateFormat format = new SimpleDateFormat("\"yyyy-MM-dd'T'HH:mm:ss'Z\"^^xsd:dateTime'");

	private final Properties props;
	private final Logger logger;

	private EDMProvStoreWrapper edmProvStoreWrapper;
	private SPARQLProvTranslator translator;

	public EDMProvReaderImpl(Properties props) throws SesameException  {
		logger = LoggerFactory.getLogger(getClass());
		this.props = props;
		translator = new SPARQLProvTranslator(props);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));

		connect();
	}


	private void connect() throws SesameException {
		try {
            edmProvStoreWrapper = new EDMProvStoreWrapper(props);
			edmProvStoreWrapper.loadPrefixes();
        } catch (Exception e) {
			logger.error("Error connecting to sesame server at " + props.getProperty("owlim.sesameServerURL"), e);
			throw new SesameException("Could not connect to EDM Prov store", e);
        }
	}

	@Override
	public void disconnect() {
		if (edmProvStoreWrapper != null && edmProvStoreWrapper.isConnected()) {
			logger.warn("EDMProvStoreWrapper has still got an open connection - disconnecting now");
			edmProvStoreWrapper.disconnect();
		} else {
			logger.debug("EDMProvStoreWrapper is already disconnected");
		}
	}

	@Override
	public EDMProvBaseElement getElementCore(String IRI) {
		//important: don't change variable names as SPARQLProvTranslator depends on them!
		String query = "SELECT DISTINCT * WHERE { " +
			"?s ?p ?o ." +

			//filter IRI
			"FILTER(?s = <" + IRI + ">) . " +

			//filter by class: only prov classes
			"?s a ?c ." +
			"FILTER((?p = rdf:type && ?c in(prov:Agent, prov:Activity, prov:Entity) && ?c = ?o) || ?p = rdfs:label) .  " +

			//filter by property type
			"?p a ?t ." +
			"FILTER(?t in(owl:ObjectProperty, owl:DatatypeProperty, owl:AnnotationProperty) " +
				"|| (?p=rdf:type && ?t=rdf:Property)) ." +

			"} ORDER BY ?c ?s ?t ?p ";
		LinkedList<HashMap<String, String>> rawresult = edmProvStoreWrapper.query(query);
		EDMProvDataContainer result = translator.translate(rawresult);
		return result.getAllElements().get(IRI);
	}

	@Override
	public EDMProvBaseElement getElement(String IRI) {
		return getElement(IRI, null, null);
	}

	@Override
	public EDMProvBaseElement getElement(String IRI, Date start, Date end) {

		String t1 = "\"0001-01-01T00:00:00Z\"^^xsd:dateTime";
		String t2 = "\"9999-12-31T23:59:59Z\"^^xsd:dateTime";

		boolean skip = true;
		if (start!=null) {
			t1 = format.format(start);
			skip = false;
		}
		if (end!=null) {
			t2 = format.format(end);
			skip = false;
		}

		//important: don't change variable names as SPARQLProvTranslator depends on them!
		String query = "SELECT DISTINCT * WHERE { " +
			"?s ?p ?o ." +

			//filter IRI
			"FILTER(?s = <" + IRI + ">) . " +

			//filter by class: only prov classes
			"?s a ?c ." +
			"FILTER(?c in(prov:Agent, prov:Activity, prov:Entity)) ." +

			//filter by property type
			"?p a ?t ." +
			"FILTER(?t in(owl:ObjectProperty, owl:DatatypeProperty, owl:AnnotationProperty) " +
				"|| (?p=rdf:type && ?t=rdf:Property)) .";


		if (!skip) {
			//time span
			//activity needs to have a start time
			query += "?s prov:startedAtTime ?t1 ." +
			//end time optional
			"OPTIONAL { ?s prov:endedAtTime ?t2 ." +
			"FILTER((?t1 <= " + t2 + ") && (?t2 >= " + t1 + ")) . } .";
		}

		query += "} ORDER BY ?c ?s ?t1 ?t2 ?t ?p ";

		LinkedList<HashMap<String, String>> rawresult = edmProvStoreWrapper.query(query);
		EDMProvDataContainer result = translator.translate(rawresult);
		return result.getAllElements().get(IRI);

	}

	@Override
	public EDMProvDataContainer getElements(Date start, Date end) {
		return getElements(null, start, end);
	}

	@Override
	public EDMProvDataContainer getElements(EDMProvBaseElement.PROV_TYPE type, Date start, Date end) {

		String t1 = "\"0001-01-01T00:00:00Z\"^^xsd:dateTime";
		String t2 = "\"9999-12-31T23:59:59Z\"^^xsd:dateTime";

		boolean skip = true;
		if (start!=null) {
			t1 = format.format(start);
			skip = false;
		}
		if (end!=null) {
			t2 = format.format(end);
			skip = false;
		}

		//first query: get all activities matching the timeframe
		//important: don't change variable names as SPARQLProvTranslator depends on them!
		String query = "SELECT DISTINCT * WHERE { " +
			"?s ?p ?o ." +

			//filter by class: only prov classes
			"?s a ?c ." +
			"FILTER(?c in(prov:Agent, prov:Activity, prov:Entity)) ." +

			//filter by property type
			"?p a ?t ." +
			"FILTER(?t in(owl:ObjectProperty, owl:DatatypeProperty, owl:AnnotationProperty) " +
				"|| (?p=rdf:type && ?t=rdf:Property)) .";



		if (!skip) {
			//time span:
			query += "?s prov:startedAtTime ?t1 ." +
			//start date exists always and needs to be before end date of requested timeframe
			"FILTER(?t1 <= " + t2 + ") ." +
			//end date is optional...
			"OPTIONAL { ?s prov:endedAtTime ?t2 ." +
			//but if it exists, it needs to be after the start date of the requested timeframe
			"FILTER(?t2 >= " + t1 + ") . } .";
		} else {
			//filter by type
			if (type!=null) {
				switch (type) {
					case ePROV_ACTIVITY:
						query += "FILTER(?c = prov:Activity) .";
						break;
					case ePROV_AGENT:
						query += "FILTER(?c = prov:Agent) .";
						break;
					case ePROV_ENTITY:
						query += "FILTER(?c = prov:Entity) .";
						break;
					default:
						break;
				}
			}
		}

		query += "} ORDER BY ?c ?s ?t1 ?t2 ?t ?p ";

		//System.out.println("query 1: " + query);

		EDMProvDataContainer result = translator.translate(edmProvStoreWrapper.query(query));

		//do a second query to get related prov elements. only necessary if filtered by time
		if (!skip) {
			LinkedList<String> activities = new LinkedList<String>();

			if (result!=null && result.getAllElements()!=null && !result.getAllElements().isEmpty()) {
				for (EDMProvBaseElement element: result.getAllElements().values()) {
					for (EDMTriple t: element.getTriples(EDMTriple.TRIPLE_TYPE.CLASS_ASSERTION, null).values()) {
						if (t.getObject().equals("http://www.w3.org/ns/prov#Activity")) {
							activities.add(t.getSubject());
						}
					}
				}
			}

			if (!activities.isEmpty()) {

				//second query: get all the related prov elements
				//important: don't change variable names as SPARQLProvTranslator depends on them!
				query = "SELECT DISTINCT * WHERE { " +
					"?s ?p ?o ." +

					//filter by class: only prov classes
					"?s a ?c ." +
					"FILTER(?c in(prov:Agent, prov:Activity, prov:Entity)) ." +

					//filter by property type
					"?p a ?t ." +
					"FILTER(?t in(owl:ObjectProperty, owl:DatatypeProperty, owl:AnnotationProperty) " +
						"|| (?p=rdf:type && ?t=rdf:Property)) .";

				//filter by type
				if (type!=null) {
					switch (type) {
						case ePROV_ACTIVITY:
							query += "FILTER(?c = prov:Activity) .";
							break;
						case ePROV_AGENT:
							query += "FILTER(?c = prov:Agent) .";
							break;
						case ePROV_ENTITY:
							query += "FILTER(?c = prov:Entity) .";
							break;
						default:
							break;
					}
				}

				for (String a: activities) {
					//needs to be connected to the current activity with an incoming or outgoing relationship
					query += "{ ?s ?p2 <" + a + "> . } UNION { <" + a + "> ?p2 ?s . } UNION ";
				}
				//cut the last UNION from the query
				query = query.substring(0, query.length()-6);

				query += "} ORDER BY ?c ?s ?t1 ?t2 ?t ?p ";

				//System.out.println("query 2: " + query);

				//workaround for deep copying resultsets - otherwise result is overwritten when result2 is retrieved
				translator = new SPARQLProvTranslator(props);
				EDMProvDataContainer result2 = translator.translate(edmProvStoreWrapper.query(query));

				if (result!=null && result.getAllElements()!=null && !result.getAllElements().isEmpty() &&
					result2!=null && result2.getAllElements()!=null && !result2.getAllElements().isEmpty()) {
					if (type==null) {
						//add to result
						result.mergeWith(result2);
					} else {
						//replace result
						result = result2;
					}
				}
			}
		}

		return result;
	}

	@Override
	public Set<EDMTriple> getTriples(String subjectIRI, String predicate, String objectIRI) {

		if (subjectIRI==null && predicate==null && objectIRI==null) {
			return null;
		}

		String query = "SELECT DISTINCT * WHERE { " +
			"?s ?p ?o ." +

			//filter by property type
			"?p a ?t ." +
			"FILTER(?t in(owl:ObjectProperty, owl:DatatypeProperty, owl:AnnotationProperty) " +
				"|| (?p=rdf:type && ?t=rdf:Property)) .";
		if (subjectIRI!=null) {
			//time span
			query += "FILTER( ?s = <" + subjectIRI + ">) .";
		}
		if (predicate!=null) {
			//time span
			query += "FILTER( ?p = <" + predicate + ">) .";
		}
		if (objectIRI!=null) {
			//time span
			query += "FILTER( ?o = <" + objectIRI + ">) .";
		}
		query += "} ORDER BY ?s ?t ?p ?o ";

		Set<EDMTriple> triples = new HashSet<EDMTriple>();
		for (HashMap<String, String> t: edmProvStoreWrapper.query(query)) {
			if (t.containsKey("s") && t.containsKey("p") && t.containsKey("o")
				&& t.get("s")!=null && t.get("p")!=null && t.get("o")!=null ) {

				String type = t.get("t");
				EDMTriple.TRIPLE_TYPE tripletype = EDMTriple.TRIPLE_TYPE.UNKNOWN_TYPE;
				if (type.equals("http://www.w3.org/2002/07/owl#DatatypeProperty")) {
					tripletype = EDMTriple.TRIPLE_TYPE.DATA_PROPERTY;

					//get xsd:datatype and attach to datatype properties
					if (t.get("o").endsWith("<http://www.w3.org/2001/XMLSchema#dateTime>")) {
						t.put("o", t.get("o").substring(0,t.get("o").lastIndexOf("^")+1)+"xsd:dateTime");
					}

				} else if (type.equals("http://www.w3.org/2002/07/owl#ObjectProperty")) {
					tripletype = EDMTriple.TRIPLE_TYPE.OBJECT_PROPERTY;
				} else if (type.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property")) {
					tripletype = EDMTriple.TRIPLE_TYPE.CLASS_ASSERTION;
				} else if (type.equals("http://www.w3.org/2002/07/owl#AnnotationProperty")) {
					tripletype = EDMTriple.TRIPLE_TYPE.ANNOTATION_PROPERTY;
				} else {
					logger.warn("Unknown triple type: " + type);
				}

				triples.add(new EDMTriple(t.get("s"), t.get("p"), t.get("o"), tripletype));
			}
		}

		return triples;
	}

	//GETTERS/SETTERS

	public EDMProvStoreWrapper getEDMProvStoreWrapper() {
		return edmProvStoreWrapper;
	}

}
