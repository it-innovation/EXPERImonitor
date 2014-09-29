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

import java.util.ArrayList;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.owlimstore.common.RelationshipType;
import uk.ac.soton.itinnovation.owlimstore.common.SesameException;
import uk.ac.soton.itinnovation.owlimstore.common.Triple;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.db.EDMProvStoreWrapper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao.IEDMProvWriter;

public class EDMProvWriterImpl implements IEDMProvWriter {

	private final Properties props;
	private final Logger logger;

	private EDMProvStoreWrapper edmProvStoreWrapper;

    public EDMProvWriterImpl(Properties props) throws SesameException {
		logger = LoggerFactory.getLogger(getClass());
		this.props = props;

		connect();
    }
    
    public EDMProvWriterImpl( EDMProvStoreWrapper psw ) {
        
        if (psw == null) throw new NullPointerException("Could not construct prov writer impl with null store wrapper");
        
        logger = LoggerFactory.getLogger(getClass());
        props = psw.getStoreProperties();
        edmProvStoreWrapper = psw;
    }
	
	private void connect() throws SesameException {
		try {
            edmProvStoreWrapper = new EDMProvStoreWrapper(props);
        } catch (Exception e) {
			logger.error("Error connecting to sesame server at " + props.getProperty("owlim.sesameServerURL"), e);
			throw new SesameException("Error connecting to store", e);
        }
	}

	@Override
	public void disconnect() {
		if (edmProvStoreWrapper != null && edmProvStoreWrapper.isConnected()) {
			logger.debug("EDMProvStoreWrapper has still got an open connection - disconnecting now");
			
            try
            {
                edmProvStoreWrapper.disconnect();
            }
            catch ( Exception ex )
            {
                logger.error( "Had problems trying to disconnect: " + ex.getMessage() );
            }
            
		} else {
			logger.debug("EDMProvStoreWrapper is already disconnected");
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
			edmProvStoreWrapper.addTriples(props.getProperty("owlim.repositoryID"), triples);
		} catch (Exception e) {
			logger.error("Error adding triples", e);
		}
    }

	@Override
	public void importOntology(String ontologypath, String baseURI, String prefix, Class resourcepathclass) {
		edmProvStoreWrapper.importOntologyToKnowledgeBase(ontologypath, baseURI, prefix, resourcepathclass);
	}

	@Override
	public void clearRepository(String repositoryID) {
		logger.debug("Clearing repository " + repositoryID + " by deleting all triples");
		
        try
        {
            edmProvStoreWrapper.clearRepository(repositoryID);
        }
        catch ( Exception ex )
        {
            logger.error( "Had problems trying to clear repository: " + ex.getMessage() );
        }
	}

	public EDMProvStoreWrapper getEDMProvStoreWrapper() {
		return edmProvStoreWrapper;
	}

}
