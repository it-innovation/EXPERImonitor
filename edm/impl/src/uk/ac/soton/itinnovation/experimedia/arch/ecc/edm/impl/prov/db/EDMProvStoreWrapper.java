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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import uk.ac.soton.itinnovation.edmprov.owlim.common.NoSuchRepositoryException;
import uk.ac.soton.itinnovation.edmprov.owlim.common.OntologyDetails;
import uk.ac.soton.itinnovation.edmprov.owlim.common.SesameException;
import uk.ac.soton.itinnovation.edmprov.sesame.RemoteSesameConnector;

public class EDMProvStoreWrapper extends RemoteSesameConnector {
	
	private final Properties props;
	private String prefixes;
	
	public EDMProvStoreWrapper(Properties props) throws Exception {
		
		super(props.getProperty("owlim.sesameServerURL"));
		logger = Logger.getLogger(EDMProvStoreWrapper.class);
		logger.setLevel(Level.INFO);	//TODO: remove
			
		this.props = props;
		this.prefixes = null;
	}

	public void loadPrefixes() {
		//get prefixes
		this.prefixes = "";
		if (this.getRepositoryNamespaces().get(props.getProperty("owlim.repositoryID"))!=null) {
			for (Map.Entry<String, String> e: this.getRepositoryNamespaces().get(props.getProperty("owlim.repositoryID")).entrySet()) {
				this.prefixes += "PREFIX " + e.getKey() + ":<" + e.getValue() + ">\n";
			}
		}
		try {
			super.setNamespacesFromRepository(props.getProperty("owlim.repositoryID"));
		} catch (SesameException e) {
		
		}
	}
	
	public boolean repositoryExists(String repositoryID) throws SesameException {
		try {
			getRepository(repositoryID);
			return true;
		} catch (NoSuchRepositoryException e) {
			return false;
		} catch (SesameException e) {
			throw new SesameException("Not connected to store", e);
		}
	}
	
	public void importOntologyToKnowledgeBase(String ontologypath, String baseURI, String prefix, Class resourcepathclass) {
		logger.debug(" - " + prefix + " (" + baseURI + ")");
		OntologyDetails od = new OntologyDetails();
		File ontfile = new File(ontologypath);
		if (!ontfile.exists()) {
			logger.debug("file " + ontologypath + " doesn't exist in working directory");
			//try URL
			if (ontologypath.startsWith("http://")) {
				logger.debug("Loading from URL " + ontologypath);
				try {
					URL remoteOntology = new URL(ontologypath);
					od.setURL(remoteOntology);
					        
				} catch (MalformedURLException e) {
					logger.error("Error loading ontology from URL " + ontologypath, e);
				}
			//try file from classpath
			} else {
				logger.debug("Trying to find ontology in classpath...");
				try {
					ontfile = new File(resourcepathclass.getClassLoader().getResource(ontologypath).getPath());
					logger.debug("Reading file from path " + resourcepathclass.getClassLoader().getResource(ontologypath).getPath());
					od.setURL(ontfile.toURI().toURL());
				} catch (MalformedURLException e) {
					logger.error("Error reading resource file", e);
				}
			}
			
		} else {
			logger.debug("Loading ontology from file " + ontologypath);
			try {
				od.setURL(ontfile.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.error("Ontology path invalid", e);
			}
		}
		od.setBaseURI(baseURI);
		od.setPrefix(prefix);
		try {
			super.addOntology(props.getProperty("owlim.repositoryID"), od);
		} catch (Exception e) {
			logger.error("Error importing ontology", e);
		}
	}
	
    public LinkedList<HashMap<String,String>> query(String sparql) {
		
		if (this.prefixes==null) {
			logger.debug("prefixes are null");
			loadPrefixes();
		}
		loadPrefixes();
		sparql = prefixes + sparql;
		
		logger.debug(sparql);
				
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
			logger.debug(" - Got " + counter + " result(s) in " + (queryEnd - queryBegin) / 1000000 + "ms.");
			
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
	
	public HashMap<String, HashMap<String, String>> getRepositoryNamespaces() {
		return super.repositoryNamespaces;
	}
	
	public void setRepositoryNamespaces(HashMap<String, HashMap<String, String>> ns) {
		//TODO: necessary? filter out standard namespace (":"), otherwise it would be overwritten with every ontology import
		//ns.get(props.getProperty("owlim.repositoryID")).remove(""); => doesn't work
		super.repositoryNamespaces = ns;
		this.loadPrefixes();
	}
	
	public String getPrefixes() {
		return prefixes;
	}
	
}
