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

public class EDMProvFactory {
	
	private static EDMProvFactory factory = null;
	private HashMap<String, EDMProvBaseElement> allProvElements;
	private HashMap<String, EDMProvBaseElement> currentProvReportElements;
	
	private EDMProvFactory() {
		init();
	}

  public static synchronized EDMProvFactory getInstance() {
    if (factory==null) {
        factory = new EDMProvFactory();
        }
    
        return factory;
    }
    
	private void init() {
		allProvElements = new HashMap<String, EDMProvBaseElement>();
		currentProvReportElements = new HashMap<String, EDMProvBaseElement>();
	}
	
	public EDMAgent getAgent(String iri) throws DataFormatException {
		EDMAgent agent = (EDMAgent) this.getElement(iri, EDMProvBaseElement.PROV_TYPE.ePROV_AGENT);
		if (!agent.contains(new EDMProvTriple(iri,"rdf:type","prov:Agent"))) {
			throw new DataFormatException("The prov element you tried to get already exists but is not an EDMAgent.");
		}
		return agent;
	}
	
	public EDMActivity getActivity(String iri) throws DataFormatException {
		EDMActivity activity = (EDMActivity) this.getElement(iri, EDMProvBaseElement.PROV_TYPE.ePROV_ACTIVITY);
		if (!activity.contains(new EDMProvTriple(iri,"rdf:type","prov:Activity"))) {
			throw new DataFormatException("The prov element you tried to get already exists but is not an EDMActivity.");
		}
		return activity;
	}
	
	public EDMEntity getEntity(String iri) throws DataFormatException {
		EDMEntity entity = (EDMEntity) this.getElement(iri, EDMProvBaseElement.PROV_TYPE.ePROV_ENTITY);
		if (!entity.contains(new EDMProvTriple(iri,"rdf:type","prov:Entity"))) {
			throw new DataFormatException("The prov element you tried to get already exists but is not an EDMEntity.");
		}
		return entity;
	}
  
  public void elementUpdated(EDMProvBaseElement element) {
      if (element != null) {
          String iri = element.getIri();
          
          if ( !currentProvReportElements.containsKey(iri) )
              currentProvReportElements.put(iri, element);
      }
  }

	public EDMProvBaseElement getElement(String iri, EDMProvBaseElement.PROV_TYPE type) {
		if (allProvElements.containsKey(iri)) {
			return allProvElements.get(iri);
		} else {
			EDMProvBaseElement element = null;
			switch (type) {
			case ePROV_AGENT:
				element = new EDMAgent(iri);
				break;
			case ePROV_ACTIVITY:
				element = new EDMActivity(iri);
				break;
			case ePROV_ENTITY:
				element = new EDMEntity(iri);
				break;
			default:
				break;
			}
      
			allProvElements.put(iri, element);
			currentProvReportElements.put(iri, element);
      
			return element;
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
}
