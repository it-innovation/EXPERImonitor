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
//      Created Date :          15-Oct-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.test.prov;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openprovenance.prov.dot.ProvToDot;
import org.openprovenance.prov.json.Converter;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasGeneratedBy;
import org.openprovenance.prov.xml.NamespacePrefixMapper;
import org.openprovenance.prov.xml.ProvFactory;
import org.openprovenance.prov.xml.ProvSerialiser;

public class ProvToolboxTest {
	
	public static final String PC1_NS = "http://our.address.org/experimedia/";
    public static final String PC1_PREFIX = "exp";

	public static void main(String[] args) {

		Hashtable<String, String> namespaces = new Hashtable<String, String>();
		// currently, no prefix used, all qnames map to PC1_NS
		namespaces.put("exp", PC1_NS);
		namespaces.put("xsd", NamespacePrefixMapper.XSD_NS);

		ProvFactory factory = new ProvFactory(namespaces);
	
		Agent peter = factory.newAgent("peter", "Peter Smith");
		Entity post = factory.newEntity("Peters_post", "ProvToolbox documentation is rubbish");
		Activity posted = factory.newActivity("posted", "Peter posts post");
		WasAssociatedWith waw = factory.newWasAssociatedWith("peterPosted", posted, peter);
		WasGeneratedBy wgb = factory.newWasGeneratedBy(post, "postByPeter", posted);
		
		GregorianCalendar gregory = new GregorianCalendar();
		gregory.setTime(new Date());
		XMLGregorianCalendar calendar;
		try {
			calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregory);
			posted.setStartTime(calendar);
			posted.setEndTime(calendar);
		} catch (DatatypeConfigurationException e) {
			System.out.println("Error getting date");
		}
		
		Document graph = factory.newDocument(
				new Activity[] { posted },
				new Entity[] { post },
				new Agent[] { peter },
				new Statement[] { waw, wgb }
			);
		
		//To DOT:
		ProvToDot toDot=new ProvToDot(true); // with roles
		if (graph==null) System.out.println("doToDot with null ");
		try {
			toDot.convert(graph,"/home/sw/Desktop/pc1-full.dot", "/home/sw/Desktop/pc1-full.pdf", "PC1 Full");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//To XML:
		try {
			ProvSerialiser ps = new ProvSerialiser();
			StringWriter sw = new StringWriter();
			ps.serialiseDocument(sw, graph);
			System.out.println(sw.toString());
		} catch (JAXBException e) {}
		
		//To JSON:
		Converter convert=new Converter();
		try {
			convert.writeDocument(graph, "/home/sw/Desktop/json.js");
		} catch (IOException e) {}
	
	}

}
