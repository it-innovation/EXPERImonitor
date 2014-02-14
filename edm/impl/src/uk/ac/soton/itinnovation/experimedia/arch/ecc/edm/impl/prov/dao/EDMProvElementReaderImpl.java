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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvBaseElement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvDataContainer;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.db.EDMProvStoreWrapper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvElementReader;

public final class EDMProvElementReaderImpl implements IEDMProvElementReader {
	
	private static final SimpleDateFormat format = new SimpleDateFormat("\"yyyy-MM-dd'T'HH:mm:ss'Z\"^^xsd:dateTime'");
	
	private final Properties props;
	private final Logger logger;
	
	private EDMProvStoreWrapper edmProvStoreWrapper;
	private SPARQLProvTranslator translator;
	
	public EDMProvElementReaderImpl(Properties props) {
		logger = Logger.getLogger(EDMProvElementReaderImpl.class);
		this.props = props;
		translator = new SPARQLProvTranslator(props);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		connect();
	}

	
	private void connect() {
		try {
            edmProvStoreWrapper = new EDMProvStoreWrapper(props);
        } catch (Exception e) {
			logger.error("Error connecting to sesame server at " + props.getProperty("owlim.sesameServerURL"), e);
        }
	}
	
	@Override
	public void disconnect() {
		if ((edmProvStoreWrapper != null) && edmProvStoreWrapper.isConnected()) {
			logger.warn("EDMProvStoreWrapper has still got an open connection - disconnecting now");
			edmProvStoreWrapper.disconnect();
		} else {
			logger.debug("EDMProvStoreWrapper is already disconnected");
		}
	}

	@Override
	public EDMProvBaseElement getElement(String IRI) {
		return getElement(IRI, null, null);
	}

	@Override
	public EDMProvBaseElement getElement(String IRI, Date start, Date end) {
		EDMProvBaseElement element = null;
		
		//first get all elements between the start- and end time
		EDMProvDataContainer elements = getElements(start, end);
		
		//then filter by iri (faster than using another filter in SPARQL query)
		if (IRI!=null && elements.getAllElements().containsKey(IRI)) {
			element = elements.getAllElements().get(IRI);
		}
		
		return element;
	}

	@Override
	public EDMProvDataContainer getElements(Date start, Date end) {
		return getElements(EDMProvBaseElement.PROV_TYPE.ePROV_UNKNOWN_TYPE, start, end);
	}

	@Override
	public EDMProvDataContainer getElements(EDMProvBaseElement.PROV_TYPE type, Date start, Date end) {
		
		String t1 = "\"0001-01-01T00:00:00Z\"^^xsd:dateTime";
		String t2 = "\"9999-12-31T23:59:59Z\"^^xsd:dateTime";
		
		if (start!=null) {
			t1 = format.format(start);
		}
		if (end!=null) {
			t2 = format.format(end);
		}
		
		//important: don't change variable names as SPARQLProvTranslator depends on them!
		String query = "SELECT DISTINCT * WHERE { " +
			"?s ?p ?o ." +
			"?s rdf:type prov:Activity ." +	//TODO: prov type - expected behaviour?
				
			//filter by class: only prov classes
			"?s a ?c ." +
			"FILTER(?c in(prov:Agent, prov:Activity, prov:Entity)) ." +
				
			//filter by property type
			"?p a ?t ." +
			"FILTER(?t in(owl:ObjectProperty, owl:DatatypeProperty, owl:AnnotationProperty) " +
				"|| (?p=rdf:type && ?t=rdf:Property)) ." +
				
			//time span
			"?s prov:startedAtTime ?t1 ." +
			"?s prov:endedAtTime ?t2 ." +
			"FILTER((?t1 <= " + t2 + ") && (?t2 >= " + t1 + ")) ." +
			
			"} ORDER BY ?c ?s ?t1 ?t2 ?t ?p ";
		System.out.println(query);
		translator.translate(edmProvStoreWrapper.query(query));

		return translator.getContainer();
	}

	@Override
	public EDMProvDataContainer getElements(Set<EDMTriple> rels) {
		logger.warn("Not yet implemented"); //TODO: expected behaviour?
		return null;
	}

	public EDMProvStoreWrapper getEDMProvStoreWrapper() {
		return edmProvStoreWrapper;
	}

}
