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

import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvBaseElement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.db.EDMProvStoreWrapper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvElementReader;

public final class EDMProvElementReaderImpl implements IEDMProvElementReader {
	
	private final Properties props;
	private final Logger logger;
	
	private EDMProvStoreWrapper sCon;
	
	public EDMProvElementReaderImpl(Properties props) {
		logger = Logger.getLogger(EDMProvElementReaderImpl.class);
		this.props = props;
		init();
	}
	
	private void init() {
		connect();
	}
	
	private void connect() {
		try {
            sCon = new EDMProvStoreWrapper(props);
        } catch (Exception e) {
			logger.error("Error connecting to sesame server at " + props.getProperty("owlim.sesameServerURL"), e);
        }
	}

	@Override
	public EDMProvBaseElement getElement(String IRI) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public EDMProvBaseElement getElement(String IRI, Date start, Date end) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public HashMap<String, EDMProvBaseElement> getElements(Date start, Date end) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public HashMap<String, EDMProvBaseElement> getElements(EDMProvBaseElement.PROV_TYPE type, Date start, Date end) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public HashMap<String, EDMProvBaseElement> getElements(Set<EDMTriple> rels) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
