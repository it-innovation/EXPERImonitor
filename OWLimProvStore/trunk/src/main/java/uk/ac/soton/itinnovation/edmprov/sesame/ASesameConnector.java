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
//      Created By :            Vegard Engen
//      Created Date :          2013-01-25
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.edmprov.sesame;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.*;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;

import uk.ac.soton.itinnovation.edmprov.owlim.common.*;

/**
 * An abstract connector class, implementing the general functionality that is
 * shared between local and remote Sesame Connectors.
 *
 * @author Vegard Engen
 */
public abstract class ASesameConnector
{
	protected RepositoryManager manager;
	protected String managerID;
	private final RDFFormat rdfFormat = RDFFormat.TURTLE;;
	protected String repositoryConfigTemplate;
	protected HashMap<String, HashMap<String, String>> repositoryNamespaces;
	
	protected static Logger logger = Logger.getLogger(ASesameConnector.class);
	
	/**
	 * Sets up the repository configuration template from a default one (pre-configured
	 * in this class).
	 */
	protected ASesameConnector()
	{
		this.repositoryNamespaces = new HashMap<String, HashMap<String,String>>();
		this.repositoryConfigTemplate = "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.\n" +
			"@prefix rep: <http://www.openrdf.org/config/repository#>.\n" +
			"@prefix sr: <http://www.openrdf.org/config/repository/sail#>.\n" +
			"@prefix sail: <http://www.openrdf.org/config/sail#>.\n" +
			"@prefix owlim: <http://www.ontotext.com/trree/owlim#>.\n" +
			"@prefix prov: <http://www.w3.org/ns/prov#>.\n" +
			"\n" +
			"[] a rep:Repository ;\n" +
			"   rep:repositoryID \"TEMPLATE_REPO_ID\" ;\n" +
			"   rdfs:label \"TEMPLATE_REPO_NAME\" ;\n" +
			"   rep:repositoryImpl [\n" +
			"     rep:repositoryType \"openrdf:SailRepository\" ;\n" +
			"     sr:sailImpl [\n" +
			"       sail:sailType \"swiftowlim:Sail\" ; \n" +
			"       owlim:entity-index-size \"5000000\" ;\n" +
			"       owlim:repository-type \"in-memory-repository\" ;\n" +
			"       owlim:ruleset \"owl-horst-optimized\" ;\n" +
			"       owlim:storage-folder \"storage\" ;\n" +
			"       owlim:base-URL \"http://www.w3.org/ns/prov#\" ;\n" +
			"       owlim:defaultNS \"http://www.w3.org/ns/prov#\" ;\n" +
			"       owlim:imports \"http://www.w3.org/ns/prov.owl\" ;\n" +
			"       owlim:noPersist \"false\" ;\n" +
			"      ]\n" +
			"   ].";
	}
	
	/**
	 * Sets up the repository configuration template, either from a default one
	 * or from file, if the 'owlim.repoTemplate.path' property is specified.
	 * If a template is provided, it is assumed to be in TURTLE format and MUST 
	 * contain placeholders for 'TEMPLATE_REPO_ID' and 'TEMPLATE_REPO_NAME', 
	 * to set the ID of the repository and the name, respectively.
	 * 
	 * For example: 
	 *    rep:repositoryID \"TEMPLATE_REPO_ID\" ;
	 *    rdfs:label \"TEMPLATE_REPO_NAME\" ;
	 * 
	 * @param props Configuration properties; will look for 'owlim.repoTemplate.path'
	 *        if a repository configuration template is provided
	 * @throws Exception 
	 */
	protected ASesameConnector(Properties props) throws Exception
	{
		this();
		
		if (props != null)
		{
			if (props.getProperty("owlim.repoTemplate.path") != null)
			{
				try {
					String tmpRepoTemplate = readRepositoryConfigTemplate(props.getProperty("owlim.repoTemplate.path"));
					if (!tmpRepoTemplate.contains("TEMPLATE_REPO_ID") || !tmpRepoTemplate.contains("TEMPLATE_REPO_NAME")) {
						throw new RuntimeException("The provided repository template does not have placeholders for 'TEMPLATE_REPO_ID' or 'TEMPLATE_REPO_NAME'");
					}
					
					this.repositoryConfigTemplate = tmpRepoTemplate;
				} catch (Exception ex) {
					throw new RuntimeException("Unable to initialise the Sesame Connector because of an error with setting up the template for new repositories: " + ex, ex);
				}
			}
		}
	}
	
	/**
	 * Creates a new OWLim repository
	 *
	 * @param repositoryID ID of the repository.
	 * @param repositoryName Name of the repository.
	 * @throws IllegalArgumentException Thrown if the repositoryID or
	 * repositoryName parameters are not given
	 * @throws RepositoryExistsException Thrown if a repository with the given
	 * ID already exists
	 * @throws SesameException Thrown for any exceptions with interacting with
	 * Sesame
	 * @throws Exception Thrown for any errors not covered by the other checked
	 * exceptions
	 */
	public void createNewRepository(String repositoryID, String repositoryName) throws IllegalArgumentException, RepositoryExistsException, SesameException, Exception
	{
		logger.debug("Creating new repository");

		if (repositoryID == null) {
			throw new IllegalArgumentException("The provided Repository ID is NULL");
		}

		if (repositoryName == null) {
			throw new IllegalArgumentException("The provided Repository Name is NULL");
		}

		if (!isConnected()) {
			throw new SesameException("Repository Manager not connected, so cannot create new repository");
		}

		logger.debug("Checking if the repository already exists");
		try {
			if (this.getRepository(repositoryID) != null) {
				throw new RepositoryExistsException("The repository '" + repositoryID + "' already exists on the server - so won't be re-creating it");
			}
		} catch (NoSuchRepositoryException ex) {
			// OK, this is a good thing - let's continue to create it! ;)
		} catch (SesameException ex) {
			logger.error("Failed to check if the repository IDalready existed on the server: " + ex, ex);
			throw new SesameException("Failed to check if the repository ID already existed on the server: " + ex, ex);
		}

		String repository = this.repositoryConfigTemplate;
		repository = repository.replace("TEMPLATE_REPO_ID", repositoryID);
		repository = repository.replace("TEMPLATE_REPO_NAME", repositoryName);
		
		logger.debug("Repository: \n" + repository);

		createRepository(repository, this.rdfFormat, "http://www.w3.org/ns/prov#");
	}
	
	/**
	 * Add triples to the given repository; no validation is done on the input
	 * parameters for the sake of avoiding overhead - exceptions will be thrown
	 * for any errors.
	 * 
	 * It is important that the triples use the full IRIs and not prefixes.
	 *
	 * @param repositoryID ID of the repository to add the triples to.
	 * @param triples The triples to add.
	 * @throws SesameException Thrown for any exceptions with interacting with
	 * Sesame
	 * @throws Exception Thrown for any errors not covered by the other checked
	 * exceptions
	 */
	public void addTriples(String repositoryID, List<Triple> triples) throws SesameException, Exception
	{
		if (!isConnected()) {
			throw new SesameException("Repository Manager not connected, so cannot add triples");
		}
		
		logger.debug("Adding " + triples.size() + " triples to the '" + repositoryID + "' repo");
		
		// First, need to process the triples to create RDF statements
		List<Statement> statements = new ArrayList<Statement>();
		for (Triple t : triples)
		{

			Resource s = new URIImpl(translatePrefixToFullName(repositoryID, t.getSubject()));
			URI p = new URIImpl(translatePrefixToFullName(repositoryID, t.getPredicate()));
			Value o;
			
			// check if the relationship type is given
			if ((t.getRelationshipType() == null) || (t.getRelationshipType().equals(RelationshipType.UNKNOWN))){
				throw new SesameException("No relationship type was set");
			}
			
			// if a data property, then get the literal
			if (t.getRelationshipType().equals(RelationshipType.DATA_PROPERTY) || t.getRelationshipType().equals(RelationshipType.ANNOTATION_PROPERTY)) {
				o = getLiteral(t.getObject());
			} else { // else, get URI
				o = new URIImpl(translatePrefixToFullName(repositoryID, t.getObject()));
			}
			
			statements.add(new StatementImpl(s, p, o));
		}
		
		// Now, time to add the triples to the repository
		Repository repo = null;
		RepositoryConnection con = null;

		try
		{
			repo = getRepository(repositoryID);
			if (repo == null) {
				throw new NullPointerException("Could not get repository with ID " + repositoryID);
			}
			
			con = repo.getConnection();
			if (con == null) {
				throw new NullPointerException("Could not get a connection to the repository with ID " + repositoryID);
			}
		} catch (Exception ex) {
			if (con != null) { try { con.close(); con = null; } catch (RepositoryException exx){} }
			if (repo != null) { try { repo.shutDown(); } catch (RepositoryException exx){} }

			throw new RuntimeException("Unable to connect to repository to add triples: " + ex, ex);
		}
		
		try {
			con.begin();
			con.add(statements);
			con.commit();
		} catch (RepositoryException ex) {
			con.rollback();
			throw new SesameException("Failed to add triples to repo: " + ex, ex);
		} finally {
			try { con.close(); } catch (RepositoryException ex) { }
			try { repo.shutDown(); } catch (RepositoryException ex) { }
		}
	}
	
	/**
	 * Checks and translates a triple string defined with a prefix into a full
	 * namespace URI.
	 * @param repositoryID The repository for which to get the namespace
	 * @param s The triple string to check and translate
	 * @return A triple string with the full namespace URI.
	 * @throws NoSuchElementException 
	 */
	private String translatePrefixToFullName(String repositoryID, String s) throws NoSuchElementException, SesameException
	{
		if (!repositoryNamespaces.containsKey(repositoryID)) {
			throw new NoSuchElementException("Could not find repository " + repositoryID + " to get full name");
		}
		
		//check against internal list of namespaces
		for (Entry<String, String> entry: repositoryNamespaces.get(repositoryID).entrySet()) {
			//if the string starts with any of these, it is already a full URI
			if (s.startsWith(entry.getValue())) {
				return s;
			}
			//if the string starts with the short prefix followed by a colon, create full URI:
			if (s.startsWith(entry.getKey() + ":")) {
				return s.replaceFirst(entry.getKey() + ":", entry.getValue());
			}
		}
		
		//check for unknown prefix 			TODO: correct?
		//contains exactly one : and no /
		if (s.contains(":") && s.lastIndexOf(":")==s.indexOf(":") && !s.contains("/")) {
			throw new SesameException("Unknown prefix, cannot translate to full URI");
		}
		
		//If we got here, it's either an unknown full URI (fine, if not best practice) or a malformed string (error!)
		//No need for exception handling here, as this is done in the addTriplesMethod already.
		//Just passing the original String back assumes it is an unknown namespace. If the URI is
		//malformed, the URIImpl constructor in the addTriples method will raise an exception.
		return s;
	}
	
	/**
	 * Get a literal from the string. The data type is assumed to be given in the
	 * form "string"^^datatype, e.g., "string"^^xsd:string. If no datatype is given,
	 * then it is assumed to be a string.
	 * 
	 * @param str The string to create a literal from
	 * @return
	 * @throws Exception 
	 */
	private Literal getLiteral(String str) throws Exception
	{
		if (!str.startsWith("\"")) {
			throw new RuntimeException("The literal is malformed; does not start with \": " + str);
		}
		
		// get the index of closing quote
		int idxClosingQuote = str.lastIndexOf("\"");
		
		if (idxClosingQuote <= 0) {
			throw new RuntimeException("The literal is malformed; no closing quote was found in: " + str);
		}
		
		String label = str.substring(1, idxClosingQuote);
		
		URI dataSource = null;
		// if no data source given, assume string
		if (idxClosingQuote == (str.length()-1))
		{
			dataSource = new URIImpl("http://www.w3.org/2001/XMLSchema#string");
		} 
		else
		{
			String dataSourceStr = str.substring(idxClosingQuote+1, str.length());
			
			String[] split = dataSourceStr.split("\\^\\^xsd:");
			if (split.length != 2) {
				throw new RuntimeException("Malformed data source (" + dataSourceStr + ") for literal (expected ^^xsd:...): " + str);
			}
			
			dataSource = new URIImpl("http://www.w3.org/2001/XMLSchema#" + split[split.length-1]);
		}
		
		Literal literal = new LiteralImpl(label, dataSource);
		//Literal literal = new LiteralImpl(label);
		
		return literal;
	}

	/**
	 * Add an ontology to the given repository.
	 *
	 * @param repositoryID The ID of the repository to add the ontology to.
	 * @param ontology The details of the ontology to be added (minimally need
	 * the URL)
	 * @throws SesameException Thrown for any exceptions with interacting with
	 * Sesame
	 * @throws Exception Thrown for any errors not covered by the other checked
	 * exceptions
	 */
	public void addOntology(String repositoryID, OntologyDetails ontology) throws SesameException, Exception
	{
		if (!isConnected()) {
			throw new SesameException("Repository Manager not connected, so cannot add ontology");
		}
		
		Repository repo = null;
		RepositoryConnection con = null;

		try
		{
			repo = getRepository(repositoryID);
			if (repo == null) {
				throw new NullPointerException("Could not get repository with ID " + repositoryID);
			}
			
			con = repo.getConnection();
			if (con == null) {
				throw new NullPointerException("Could not get a connection to the repository with ID " + repositoryID);
			}
		} catch (Exception ex) {
			if (con != null) { try { con.close(); con = null; } catch (RepositoryException exx){} }
			if (repo != null) { try { repo.shutDown(); } catch (RepositoryException exx){} }
			throw new RuntimeException("Unable to connect to repository to add ontology: " + ex, ex);
		}
		
		try {
			con.begin();
			con.add(ontology.getURL(), ontology.getBaseURI(), RDFFormat.RDFXML);
			if (ontology.getPrefix()!=null && con.getNamespace(ontology.getPrefix())==null) {
				con.setNamespace(ontology.getPrefix(), ontology.getBaseURI());
			}
			con.commit();
			
			//update namespaces map
			try {
				setNamespacesFromRepository(repositoryID, con);
			} catch (SesameException ex) { throw ex; }
		} catch (OpenRDFException e) {
			con.rollback();
			throw new SesameException("Failed to add ontology: " + e, e);
		} finally {
			con.close();
			repo.shutDown();
		}
	}
	
	/**
	 * Query (SPARQL) the given repository.
	 *
	 * @param repositoryID The ID of the repository to query.
	 * @param query The query (SPARQL)
	 * @return A TupleQueryResult object - MUST BE CLOSED AFTER USE.
	 * @throws SesameException Thrown for any exceptions with interacting with Sesame
	 * @throws Exception Thrown for any errors not covered by the other checked
	 * exceptions
	 */
	public TupleQueryResult query(String repositoryID, String query) throws SesameException, Exception
	{
		if (!isConnected()) {
			throw new SesameException("Repository Manager not connected, so cannot query the repository");
		}
		
		Repository repo = null;
		RepositoryConnection con = null;
		TupleQueryResult result = null;

		try
		{
			repo = getRepository(repositoryID);
			if (repo == null) {
				throw new NullPointerException("Could not get repository with ID " + repositoryID);
			}
			
			con = repo.getConnection();
			if (con == null) {
				throw new NullPointerException("Could not get a connection to the repository with ID " + repositoryID);
			}
		} catch (Exception ex) {
			if (con != null) { try { con.close(); con = null; } catch (RepositoryException exx){} }
			if (repo != null) { try { repo.shutDown(); } catch (RepositoryException exx){} }
			throw new RuntimeException("Unable to query the repository: " + ex, ex);
		}

		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
			result = tupleQuery.evaluate();
		} catch (OpenRDFException e) {
			logger.error("Exception when trying to query the repo: " + e, e);
		} finally {
			con.close();
		}

		return result;
	}
	
	/**
	 * Clear a repository of the given ID, i.e. delete all the triples.
	 * @param repositoryID ID of the repository to be deleted
	 */	
	public void clearRepository(String repositoryID) {
		if (repositoryID == null) {
			throw new NullPointerException("Cannot clear repository because the repository ID given was NULL");
		}
		
		try
		{
			Repository repo = getRepository(repositoryID);
			if (repo == null) {
				throw new NullPointerException("Could not get repository with ID " + repositoryID);
			}
			
			RepositoryConnection con = repo.getConnection();
			if (con == null) {
				throw new NullPointerException("Could not get a connection to the repository with ID " + repositoryID);
			}
			
			con.clear();
			
		} catch (Exception e) {
			logger.error("Error clearing repository " + repositoryID, e);
		}
	}
	
	/**
	 * Delete a repository of the given ID.
	 * @param repositoryID ID of the repository to be deleted
	 * @throws NoSuchRepositoryException if no repository with the given ID was found on the server
	 * @throws SesameException Thrown for any exceptions with interacting with Sesame
	 * @throws Exception Thrown for any errors not covered by the other checked
	 */
	public void deleteRepository(String repositoryID) throws NoSuchRepositoryException, SesameException, Exception
	{
		if (repositoryID == null) {
			throw new NullPointerException("Cannot delete repository because the repository ID given was NULL");
		}
		
		if (!isConnected()) {
			throw new SesameException("Repository Manager not connected, so cannot delete repository");
		}
		
		Repository repository = null;
		
		try {
			repository = getRepository(repositoryID);
		} catch (NoSuchRepositoryException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new SesameException("Unable to get repository that was asked to be deleted: " + ex, ex);
		}
		
		try {
			repository.shutDown();
		} catch (RepositoryException ex) {
			throw new SesameException("Unable to shut down repository that should be deleted: " + ex, ex);
		}
		
		logger.debug("Deleting repository: " + repositoryID);
		int maxAttempts = 10;
		int attemptNr = 0;
		do {
			if (manager.isSafeToRemove(repositoryID)) {
				try { manager.removeRepository(repositoryID); } catch (Exception ex) {
					throw new SesameException("Failed to delete the repository: " + ex, ex);
				}
				logger.debug(" - repository deleted!");
				//update namespace map
				if (repositoryNamespaces.containsKey(repositoryID)) {
					repositoryNamespaces.remove(repositoryID);
				}
				break;
			} else {
				logger.debug(" - not safe to delete; sleeping for 500ms before trying again");
				Thread.currentThread().sleep(500); // sleeping for 500 ms
			}
						
			attemptNr++;
		} while (attemptNr < maxAttempts);
	}

	/**
	 * Check if a connection has been made to a repository server.
	 *
	 * @return True if connected; false otherwise.
	 */
	public boolean isConnected()
	{
		if (manager == null) {
			return false;
		}

		// TODO: need to test that this is OK
		return true;
	}

	/**
	 * Code from org.openrdf.console.Console
	 */
	public void disconnect()
	{
		if (manager != null)
		{
			logger.debug("Disconnecting from " + managerID);
			manager.shutDown();
			manager = null;
			managerID = null;
		}
	}

	/**
	 * Sets the RepositoryManager. Code from org.openrdf.console.Console
	 *
	 * @param newManager
	 * @param newManagerID
	 * @return
	 * @throws uk.ac.soton.itinnovation.edmprov.owlim.common.SesameException
	 */
	protected void installNewManager(RepositoryManager newManager, String newManagerID) throws SesameException
	{
		if (newManagerID.equals(managerID)) {
			return;
		}
		
		try
		{
			newManager.initialize();
			
			disconnect(); // in case there was an old manager
			manager = newManager;
			managerID = newManagerID;
		} catch (RepositoryException e) {
			logger.error("Failed to install new manager", e);
			throw new SesameException("Failed to install new manager", e);
		}
	}
	
	/**
	 * Get a repository given the ID provided in the method call.
	 *
	 * @param repositoryID ID of the repository.
	 * @return NULL if a connection has not been set up or if there is no
	 * repository with the given ID.
	 * @throws NoSuchRepositoryException if there was no repository with the given ID
	 * @throws SesameException if there are any errors with connecting to Sesame
	 */
	protected Repository getRepository(String repositoryID) throws NoSuchRepositoryException, SesameException
	{
		if (!isConnected()) {
			throw new SesameException("Repository Manager not connected, so cannot get repository");
		}

		Repository repo = null;
		try {
			 repo = manager.getRepository(repositoryID);
		} catch (RepositoryConfigException ex) {
			logger.error("Unable to get repository: " + ex, ex);
			throw new SesameException("Unable to get repository: " + ex, ex);
		} catch (RepositoryException ex) {
			logger.error("Unable to get repository: " + ex, ex);
			throw new SesameException("Unable to get repository: " + ex, ex);
		}
		
		if (repo == null) {
			throw new NoSuchRepositoryException();
		}
		
		// check if namespaces have been set for the repository
		if (!repositoryNamespaces.containsKey(repositoryID))
		{
			RepositoryConnection con = null;
			try {
				con = repo.getConnection();
			} catch (RepositoryException ex) { 
				try { repo.shutDown(); } catch (RepositoryException exx){}
				throw new SesameException("Unable to create a connection to the repository: " + ex, ex);
			} 
			
			try {
				setNamespacesFromRepository(repositoryID, con);
			} catch (SesameException ex) { // if we can't set the name spaces, then there's an issue with the repo, so throw an exception and shut it down
				if (con != null) { try { con.close(); con = null; } catch (RepositoryException exx){} }
				try { repo.shutDown(); } catch (RepositoryException exx){}
				throw new SesameException("Cannot return Repository because of a technical issue encountered when getting namespaces from the repository (required for future interactions when adding triples): " + ex, ex);
			} finally {
				if (con != null) { try { con.close(); } catch (RepositoryException ex){} }
			}
		}
		
		return repo;
	}
	
	/**
	 * Reads the content of the file into a string, which is returned.
	 * @param filepath The path to the repository configuration template.
	 * @return A string with the content of the file.
	 * @throws Exception For any errors, such as file not found or issues with processing the content.
	 */
	private String readRepositoryConfigTemplate(String filepath) throws Exception
	{
		String config = "";
		
		File f = new File(filepath);
		if (!f.exists()) {
			throw new FileNotFoundException("The given file path does not exist: " + f.getAbsolutePath());
		}
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			String line;
			while ((line = br.readLine()) != null) {
				config += line + "\n";
			}
		} catch (IOException ex) {
			throw new IOException("Unable to read file: " + f.getAbsolutePath(), ex);
		} finally {
			if (br != null) {
				try { br.close(); } catch (IOException ex2) {}
			}
		}
		
		return config;
	}

	/**
	 * Creates a repository according to settings in a configuration file.
	 *
	 * @param config The configuration for the repository.
	 * @param rdfFormat The RDF format of the configuration file.
	 * @param defaultNamespace The default namespace to use.
	 * @return The ID of the repository, which should be defined in the config
	 * file.
	 * @throws SesameException For any errors, such as: not connected to sesame
	 * server, config file not valid, repository exists, and other technical
	 * isues.
	 */
	private String createRepository(String config, RDFFormat rdfFormat, String defaultNamespace) throws SesameException
	{
		if (manager == null) {
			logger.error("createRepository() called when a connection has not been set up");
			throw new SesameException("createRepository() called when a connection has not been set up");
		}
		
		if ((config == null) || (rdfFormat == null) || (defaultNamespace == null)) {
			logger.error("createRepository() parameters not valid - one or more were NULL");
			throw new SesameException("createRepository() parameters not valid - one or more were NULL");
		}
		
		Reader configReader = new StringReader(config);

		// Parse the configuration file, assuming it is in Turtle format
		Graph repositoryRdfDescription = null;
		try {
			logger.debug("Parsing config file");
			repositoryRdfDescription = parseFile(configReader, rdfFormat, defaultNamespace);
		} catch (OpenRDFException e) {
			throw new SesameException("There was an error reading/parsing the " + rdfFormat + " repository configuration: " + e, e);
		} catch (IOException e) {
			throw new SesameException("An I/O error occurred while processing the repository configuration: " + e, e);
		}

		if (repositoryRdfDescription == null) {
			throw new SesameException("Failed to create new repository - unable to get the description graph from the repository configuration");
		}

		// Look for the subject of the first matching statement for "?s type Repository"
		final String repositoryUri = "http://www.openrdf.org/config/repository#Repository";
		final String repositoryIdUri = "http://www.openrdf.org/config/repository#repositoryID";
		Iterator<Statement> iter = repositoryRdfDescription.match(null, RDF.TYPE, new URIImpl(repositoryUri));
		Resource repositoryNode = null;
		if (iter.hasNext())
		{
			Statement st = iter.next();
			repositoryNode = st.getSubject();
		}

		if (repositoryNode == null)
		{
			throw new SesameException("The " + rdfFormat + " repository configuration does not contain a valid repository description, because it is missing a resource with rdf:type <"
			   + repositoryUri + ">");
		}

		// Get the repository ID (and ignore the one passed with the 'repository' parameter
		logger.debug("Getting the repository ID");
		String repositoryID = null;
		iter = repositoryRdfDescription.match(repositoryNode, new URIImpl(repositoryIdUri), null);
		if (iter.hasNext())
		{
			Statement st = iter.next();
			repositoryID = st.getObject().stringValue();
		}
		else
		{
			throw new SesameException("The " + rdfFormat + " repository configuration file does not contain a valid repository description, because it is missing a <"
			   + repositoryUri + "> with a property <" + repositoryIdUri + ">");
		}

		try {
			// Create a configuration object from the configuration file and  add  it to the Repository Manager
			logger.debug("Configuring the Repository Manager - repository ID is '" + repositoryID + "'");
			RepositoryConfig repositoryConfig = RepositoryConfig.create(repositoryRdfDescription, repositoryNode);
			manager.addRepositoryConfig(repositoryConfig);
		} catch (OpenRDFException e) {
			logger.error("Unable to process the repository configuration: " + e.getMessage(), e);
			throw new SesameException("Unable to process the repository configuration: " + e.getMessage(), e);
		}

		// Check that we can get the repository that should have been created
		Repository repository = null;
		RepositoryConnection con = null;
		try
		{
			logger.debug("Getting the repository");
			repository = manager.getRepository(repositoryID);

			if (repository == null) {
				logger.error("Unknown repository '" + repositoryID + "' when getting trying to get the repository just created from the manager");
				throw new SesameException("Unknown repository '" + repositoryID + "' when getting trying to get the repository just created from the manager");
			}
			
			//add repository namespaces to internal map
			con = repository.getConnection();
			setNamespacesFromRepository(repositoryID, con);
		} catch (OpenRDFException e) {
			logger.error("Unable to establish a connection to the repository '" + repositoryID + "': " + e.getMessage());
			throw new SesameException("Unable to establish a connection to the repository '" + repositoryID + "': " + e.getMessage(), e);
		} finally {
			if (con != null) { try { con.close(); } catch (RepositoryException ex) {} }
			if (repository != null) { try { repository.shutDown(); } catch (RepositoryException ex) {} }
		}
		
		return repositoryID;
	}
	
	/**
	 * Parse the given RDF file and return the contents as a Graph.
	 * Adapted from the owlim-lite GetingStarted.java file.
	 *
	 * @param configReader The file containing the RDF data
	 * @param format The RDF format of the configuration
	 * @param defaultNamespace The default namespace
	 * @return The contents of the file as an RDF graph
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 * @throws IOException
	 */
	protected Graph parseFile(Reader configReader, RDFFormat format, String defaultNamespace)
	   throws RDFParseException, RDFHandlerException, IOException
	{
		final Graph graph = new GraphImpl();
		RDFParser parser = Rio.createParser(format);
		RDFHandler handler = new RDFHandler()
		{
			@Override
			public void endRDF() throws RDFHandlerException
			{
			}

			@Override
			public void handleComment(String arg0) throws RDFHandlerException
			{
			}

			@Override
			public void handleNamespace(String arg0, String arg1) throws RDFHandlerException
			{
			}

			@Override
			public void handleStatement(Statement statement) throws RDFHandlerException
			{
				graph.add(statement);
			}

			@Override
			public void startRDF() throws RDFHandlerException
			{
			}
		};
		parser.setRDFHandler(handler);
		parser.parse(configReader, defaultNamespace);
		return graph;
	}
	
	/**
	 * Sets all namespaces from a repository and stores them in a HaspMap for each repository.
	 * 
	 * @param repositoryID Repository name
	 * @throws SesameException
	 */
	protected void setNamespacesFromRepository(String repositoryID) throws SesameException
	{	
		if (!isConnected()) {
			throw new SesameException("Repository Manager not connected, so cannot get namespaces");
		}
		
		Repository myRepository = null;
		RepositoryConnection con = null;
		
		try
		{
			myRepository = getRepository(repositoryID);
			con = myRepository.getConnection();
			if (con == null) {
				throw new NullPointerException("Connection is NULL");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Unable to connect to repository to get namespaces: " + ex, ex);
		}
		
		try {
			setNamespacesFromRepository(repositoryID, con);
		} catch (SesameException e) {
			throw e;
		} finally {
			try { con.close(); } catch (RepositoryException ex) { }
			try { myRepository.shutDown(); } catch (RepositoryException ex) { }
		}
	}
	
	/**
	 * Sets all namespaces from a repository and stores them in a HaspMap for each repository.
	 * PS: does not close the connection object.
	 * @param repositoryID The repository ID
	 * @param con Repository connection
	 * @throws SesameException
	 */
	protected void setNamespacesFromRepository(String repositoryID, RepositoryConnection con) throws SesameException
	{	
		if (con == null) {
			throw new SesameException("Repository Connection NULL or not active, so cannot set the name spaces");
		}
		
		logger.debug("Setting namespaces for repository: " + repositoryID);
		
		try {
			HashMap<String, String> repoNamespaces = new HashMap<String, String>();
			RepositoryResult<Namespace> ns = con.getNamespaces();
			while (ns.hasNext()) {
				Namespace n = ns.next();
				repoNamespaces.put(n.getPrefix(), n.getName());
			}
			//overwrite what's in there
			if (repositoryID!=null && repositoryNamespaces!=null) {
				repositoryNamespaces.put(repositoryID, repoNamespaces);	
			}
		} catch (RepositoryException e) {
			throw new SesameException ("Error getting namespaces from repository " + repositoryID, e);
		}
	}

	/**
	 * Retrieves all prefixes and their namespace for a specified repository.
	 * 
	 * @param repositoryID The repository for which to get the namespaces
	 * @return A HashMap containing the data
	 */
	public HashMap<String, String> getNamespacesForRepository(String repositoryID)
	{
		if (repositoryNamespaces.containsKey(repositoryID)) {
			return repositoryNamespaces.get(repositoryID);
		} else {
			return null;
		}
	}

	@Override
	protected void finalize()
	{
		this.disconnect();
		try {
			super.finalize();
		} catch (Throwable ex) { }
	}
}
