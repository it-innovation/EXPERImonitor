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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import uk.ac.soton.itinnovation.edmprov.sesame.RemoteSesameConnector;

public class EDMProvStoreWrapper extends RemoteSesameConnector {
	
	private Properties props;
	
	public EDMProvStoreWrapper(Properties props) throws Exception {
            super(props, props.getProperty("owlim.sesameServerURL"));
			logger = Logger.getLogger(EDMProvStoreWrapper.class);
			logger.debug("Connecting to sesame server");
			
			this.props = props;
	}

    public LinkedList<HashMap<String,String>> query(String sparql) {
		
		//get prefixes
		String prefixes = "";
		if (this.getNamespacesForRepository(props.getProperty("owlim.repositoryID"))!=null) {
			for (Map.Entry<String, String> e: this.getNamespacesForRepository(props.getProperty("owlim.repositoryID")).entrySet()) {
				prefixes += "PREFIX " + e.getKey() + ":<" + e.getValue() + ">\n";
			}
		}
		sparql = prefixes + sparql;
				
        TupleQueryResult result = null;
        LinkedList<HashMap<String, String>> results = new LinkedList<HashMap<String,String>>();
		
		try {
			long queryBegin = System.nanoTime();
			result = this.query(props.getProperty("owlim.repositoryID"), sparql);
			if (result==null) {
				logger.error("SPARQL query result was null");
				return null;
			}

			int counter = 0;
			while (result.hasNext())
			{
				counter++;
				BindingSet bindingSet = result.next();

				//get all variables in the result set
				Iterator<Binding> i = bindingSet.iterator();
				HashMap<String, String> row = new HashMap<String, String>();
				while (i.hasNext()) {
					Binding b = i.next();
					row.put(b.getName(), b.getValue().toString());	//was: 	row.put(b.getName(), b.getValue().stringValue());
				}
				results.add(row);
			}
			long queryEnd = System.nanoTime();
			logger.info(" - Got " + counter + " result(s) in " + (queryEnd - queryBegin) / 1000000 + "ms.");
			
		} catch (Exception ex) {
			logger.error("Exception caught when querying repository: " + ex, ex);
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (QueryEvaluationException e) {
					logger.error("Error closing connection to KB", e);
				}
			}
			
		}
		return results;
	}
}
