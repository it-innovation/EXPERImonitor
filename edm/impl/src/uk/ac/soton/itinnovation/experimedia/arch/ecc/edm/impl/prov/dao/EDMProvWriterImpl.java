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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.edmprov.owlim.common.RelationshipType;
import uk.ac.soton.itinnovation.edmprov.owlim.common.Triple;
import uk.ac.soton.itinnovation.edmprov.sesame.ASesameConnector;
import uk.ac.soton.itinnovation.edmprov.sesame.RemoteSesameConnector;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.db.EDMProvStoreWrapper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvWriter;

public class EDMProvWriterImpl implements IEDMProvWriter {
	
	private final Properties props;
	private final Logger logger;
	
	private EDMProvStoreWrapper sCon;
    
    public EDMProvWriterImpl(Properties props) {
		logger = Logger.getLogger(EDMProvDataStoreImpl.class);
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
    public void storeReport(EDMProvReport report) {
		
       ArrayList<Triple> triples = new ArrayList<Triple>();
		if (report==null || report.getTriples()==null || report.getTriples().values()==null) {
			logger.error("Error adding triples: Invalid or empty report");
		} else {
			for (EDMTriple t: report.getTriples().values()) {

				triples.add(new Triple(t.getSubject(), t.getPredicate(), t.getObject(),
					RelationshipType.fromValue(t.getType().name())));
			}
		}
		
    	try {
			sCon.addTriples(props.getProperty("owlim.repositoryID"), triples);
		} catch (Exception e) {
			logger.error("Error adding triples", e);
		}
    }

}
