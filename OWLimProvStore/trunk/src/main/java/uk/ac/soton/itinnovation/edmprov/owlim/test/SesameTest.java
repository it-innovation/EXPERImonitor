package uk.ac.soton.itinnovation.edmprov.owlim.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;

import uk.ac.soton.itinnovation.edmprov.owlim.common.*;
import uk.ac.soton.itinnovation.edmprov.sesame.ASesameConnector;
import uk.ac.soton.itinnovation.edmprov.sesame.RemoteSesameConnector;

public class SesameTest 
{
    private static final String sesameServerURL = "http://localhost:8080/openrdf-sesame";
    private static final String repositoryID = "owlimTest";
	private static final String repositoryName = "OWLim Test Repository";
	
	private static final String bfprovPath = "src/main/resources/bonfire-prov.owl";
	private static final String benchPath = "src/main/resources/benchmark.owl";
    private static ASesameConnector sCon;
    
	private static final Logger logger = LoggerFactory.getLogger(SesameTest.class);
    
    public static void main( String[] args ) throws Exception
    {
        logger.info("Starting Sesame Test App");
		try
		{
			logger.info("Connecting to Sesame server");
			sCon = new RemoteSesameConnector(sesameServerURL);
			
			logger.info("Clearing repository");
			sCon.clearRepository(repositoryID);
			
			logger.info("Deleting repository");
			try {
				sCon.deleteRepository(repositoryID);
			} catch (NoSuchRepositoryException ex) { }
			
			logger.info("Creating owlim repository");
			try {
				sCon.createNewRepository(repositoryID, repositoryName);
			} catch (RepositoryExistsException ex) {
				logger.info("Repository already existed - fine, will continue...");
			}
			
			logger.info("Adding ontology(ies)");
			addOntology(repositoryID);
			
			logger.info("Adding some good test triples");
			try {
				addTriples(repositoryID);
			} catch (Exception ex) {
				logger.warn("Exeption caught when adding triples: " + ex, ex);
			}
			
			logger.info("Adding some bad test triples");
			try {
				addBadTriples(repositoryID);
			} catch (Exception ex) {
				logger.warn("Exeption caught when adding triples: " + ex, ex);
			}
			
			logger.info("Running some test queries");
			queryAllTriples(repositoryID);
			entityQuery(repositoryID);
			agentQuery(repositoryID);
			computeQuery(repositoryID);
			
		} catch (Exception t) {
			logger.error("Exception caught: " + t, t);
		} finally {
			if ((sCon != null) && sCon.isConnected()) {
				logger.warn("SesameConnector still got an open connection - disconnecting now");
				sCon.disconnect();
			}
		}
    }
	
	public static void addOntology(String reponame) throws Exception
	{
		logger.info(" - adding BFPROV");
		OntologyDetails bfprov = new OntologyDetails();
		File bfprovFile = new File(bfprovPath);
		bfprov.setURL(bfprovFile.toURI().toURL());
		bfprov.setBaseURI("http://www.it-innovation.soton.ac.uk/ontologies/bonfire-prov#");
		bfprov.setPrefix("bfprov");
		sCon.addOntology(repositoryID, bfprov);
		
		logger.info(" - adding BENCH");
		OntologyDetails bench = new OntologyDetails();
		File benchFile = new File(benchPath);
		bench.setURL(benchFile.toURI().toURL());
		bench.setBaseURI("http://www.it-innovation.soton.ac.uk/ontologies/benchmark#");
		bench.setPrefix("bench");
		sCon.addOntology(repositoryID, bench);
		
		logger.info(" - adding FOAF");
		OntologyDetails foaf = new OntologyDetails();
		foaf.setURL(new URL("http://xmlns.com/foaf/spec/index.rdf"));
		foaf.setBaseURI("http://xmlns.com/foaf/0.1/");
		foaf.setPrefix("foaf");
		sCon.addOntology(repositoryID, foaf);
	}
	
	public static void addTriples(String repoName) throws Exception
	{
		List<Triple> triples = new ArrayList<Triple>();
		
		String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns";
		String prov = "http://www.w3.org/ns/prov";
		String bfprov = "http://www.it-innovation.soton.ac.uk/ontologies/bonfire-prov";
		String bench = "http://www.it-innovation.soton.ac.uk/ontologies/benchmark";
		
		triples.add(new Triple(prov+"#Activity_res-mng.compute.create_1382372021_a6e8cb", rdf+"#type", prov+"#Activity", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Activity_res-mng.compute.create_1382372021_a6e8cb", prov+"#startedAtTime", "\"2013-01-21T16:13:44\"^^xsd:dateTime", RelationshipType.DATA_PROPERTY));
		triples.add(new Triple(prov+"#Activity_res-mng.compute.create_1382372021_a6e8cb", prov+"#endedAtTime", "\"2013-01-21T16:13:44\"^^xsd:dateTime", RelationshipType.DATA_PROPERTY));
		triples.add(new Triple(prov+"#Compute_/locations/fr-inria/computes/47842", rdf+"#type", bfprov+"#Compute", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Compute_/locations/fr-inria/computes/47842", prov+"#wasGeneratedBy", prov+"#Activity_res-mng.compute.create_1382372021_a6e8cb", RelationshipType.OBJECT_PROPERTY));
		triples.add(new Triple(prov+"#Compute_/locations/fr-inria/computes/47842", prov+"#generatedAtTime", "\"2013-01-21T16:13:44\"^^xsd:dateTime", RelationshipType.DATA_PROPERTY));
		triples.add(new Triple(prov+"#Location_bonfire-blade-3.bonfire.grid5000.fr", rdf+"#type", prov+"#Location", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Compute_/locations/fr-inria/computes/47842", prov+"#atLocation", prov+"#Location_bonfire-blade-3.bonfire.grid5000.fr", RelationshipType.OBJECT_PROPERTY));
		triples.add(new Triple(prov+"#Experimenter_vengen", rdf+"#type", bfprov+"#Experimenter", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Experiment_/experiments/48765", rdf+"#type", bfprov+"#Experiment", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Experiment_/experiments/48765", prov+"#hadActivity", prov+"#Activity_res-mng.compute.create_1382372021_a6e8cb", RelationshipType.OBJECT_PROPERTY));
		triples.add(new Triple(prov+"#Activity_res-mng.compute.create_1382372021_a6e8cb", prov+"#wasAssociatedWith", prov+"#Experimenter_vengen", RelationshipType.OBJECT_PROPERTY));
		
		triples.add(new Triple(prov+"#Driver_47842", rdf+"#type", bench+"#Driver", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Driver_47842", rdf+"#type", prov+"#Entity", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Driver_47842", rdf+"#type", prov+"#SoftwareAgent", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Driver_47842", bfprov+"#wasRelatedTo", prov+"#Compute_/locations/fr-inria/computes/47842", RelationshipType.OBJECT_PROPERTY));
		
		triples.add(new Triple(prov+"#Benchmark_MemoryBDW", rdf+"#type", bench+"#Benchmark", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Benchmark_MemoryBDW", rdf+"#type", prov+"#Entity", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Benchmark_MemoryBDW", bench+"#name", "\"Memory BDW\"^^xsd:string", RelationshipType.DATA_PROPERTY));
		
		triples.add(new Triple("prov:Activity_ExecuteDriver_1384256354.71", "rdf:type", "prov:Activity", RelationshipType.fromValue("CLASS_ASSERTION")));
		triples.add(new Triple("prov:Activity_ExecuteDriver_1384256354.71", "prov:startedAtTime", "\"2013-11-12T11:39:14\"^^xsd:dateTime", RelationshipType.fromValue("DATA_PROPERTY")));
		triples.add(new Triple("prov:Activity_ExecuteDriver_1384256354.71", "prov:endedAtTime", "\"2013-11-12T11:42:39\"^^xsd:dateTime", RelationshipType.fromValue("DATA_PROPERTY")));
		triples.add(new Triple("prov:Activity_ExecuteDriver_1384256354.71", "prov:wasAssociatedWith", "prov:Driver_47842", RelationshipType.fromValue("OBJECT_PROPERTY")));
		triples.add(new Triple("prov:Activity_ExecuteDriver_1384256354.71", "prov:used", "prov:Compute_/locations/fr-inria/computes/48621", RelationshipType.fromValue("OBJECT_PROPERTY")));
		
		triples.add(new Triple(prov+"#Activity_ExecuteBenchmark_1382357493.43", rdf+"#type", prov+"#Activity", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Activity_ExecuteBenchmark_1382357493.43", prov+"#startedAtTime", "\"2013-01-21T16:15:33\"^^xsd:dateTime", RelationshipType.DATA_PROPERTY));
		triples.add(new Triple(prov+"#Activity_ExecuteBenchmark_1382357493.43", prov+"#endedAtTime", "\"2013-01-21T16:15:47\"^^xsd:dateTime", RelationshipType.DATA_PROPERTY));
		triples.add(new Triple(prov+"#Activity_ExecuteBenchmark_1382357493.43", prov+"#wasAssociatedWith", prov+"#Driver_47842", RelationshipType.OBJECT_PROPERTY));
		triples.add(new Triple(prov+"#Activity_ExecuteBenchmark_1382357493.43", prov+"#used", prov+"#Benchmark_MemoryBDW", RelationshipType.OBJECT_PROPERTY));
		triples.add(new Triple(prov+"#Activity_ExecuteBenchmark_1382357493.43", prov+"#used", prov+"#Compute_/locations/fr-inria/computes/47842", RelationshipType.OBJECT_PROPERTY));
		triples.add(new Triple(prov+"#Experiment_/experiments/48765", prov+"#hadActivity", prov+"#Activity_ExecuteBenchmark_1382357493.43", RelationshipType.OBJECT_PROPERTY));
		
		triples.add(new Triple("bfprov:I_HAVE_JUST_A_PREFIX", "rdf:type", "prov:Entity", RelationshipType.CLASS_ASSERTION));
		
		triples.add(new Triple("prov:BananaMan", "rdf:type", "prov:Person", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple("prov:BananaMan", "rdf:type", "foaf:Person", RelationshipType.CLASS_ASSERTION));
		
		sCon.addTriples(repoName, triples);
	}
	
	public static void addBadTriples(String repoName) throws Exception
	{
		List<Triple> triples = new ArrayList<Triple>();
		
		String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns";
		String prov = "http://www.w3.org/ns/prov";
		String bfprov = "http://www.it-innovation.soton.ac.uk/ontologies/bonfire-prov";
		String bench = "http://www.it-innovation.soton.ac.uk/ontologies/benchmark";
		
		triples.add(new Triple(prov+"#Literal_with_datasource", rdf+"#type", prov+"#Entity", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Literal_with_datasource", bench+"#name", "\"No data source\"^^xsd:string", RelationshipType.DATA_PROPERTY));
		
		triples.add(new Triple(prov+"#Literal_no_datasource", rdf+"#type", prov+"#Entity", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Literal_no_datasource", bench+"#name", "\"No data source\"", RelationshipType.DATA_PROPERTY));
		
		triples.add(new Triple(prov+"#Literal_cheeky_datasource", rdf+"#type", prov+"#Entity", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Literal_cheeky_datasource", bench+"#name", "\"Literal with ^^ in it...\"^^xsd:string", RelationshipType.DATA_PROPERTY));
		
		triples.add(new Triple(prov+"#Literal_no_quotes", rdf+"#type", prov+"#Entity", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Literal_no_quotes", bench+"#name", "Literal without quotes^^xsd:string", RelationshipType.DATA_PROPERTY));
		
		triples.add(new Triple(prov+"#Literal_no_closing_quote", rdf+"#type", prov+"#Entity", RelationshipType.CLASS_ASSERTION));
		triples.add(new Triple(prov+"#Literal_no_closing_quote", bench+"#name", "\"Literal without closing quote^^xsd:string", RelationshipType.DATA_PROPERTY));

		triples.add(new Triple("error:I_HAVE_AN_UNKNOWN_PREFIX", "error:type", "prov:Entity", RelationshipType.CLASS_ASSERTION));
		
		sCon.addTriples(repoName, triples);
	}
    
    public static void queryAllTriples(String repoName) throws Exception
    {
        logger.info("Querying the repo");
        String queryString = "SELECT * WHERE {?s ?p ?o} ORDER BY ?s ?p ?o";
        TupleQueryResult result = null;
		
		try {
			long queryBegin = System.nanoTime();
			result = sCon.query(repoName, queryString);

			FileWriter fstream = null;
			BufferedWriter out = null;

			try {
				File f = new File("sparqlResult.srx");
				fstream = new FileWriter(f);
				out = new BufferedWriter(fstream);
				logger.info(" - Writing query results to: " + f.getAbsolutePath());
			} catch (IOException ex) {
				out = null;
			}

			try {
//				logger.info(" - Results binding names: ");
//				for (String name : result.getBindingNames()) {
//					logger.info("   - " + name);
//				}
//
//				logger.info(" - Result data:");
				int counter = 0;
				while (result.hasNext())
				{
					counter++;
					BindingSet bindingSet = result.next();
					Value s = bindingSet.getValue("s");
					Value p = bindingSet.getValue("p");
					Value o = bindingSet.getValue("o");

					//logger.info("   - " + s.stringValue() + ", " + p.stringValue() + ", " + o.stringValue());
					if (out != null) {
						try {
							out.write(s.stringValue() + ", " + p.stringValue() + ", " + o.stringValue() + "\n");
						} catch (IOException ex) { }
					}
				}
				long queryEnd = System.nanoTime();
				logger.info(" - Got " + counter + " result(s) in " + (queryEnd - queryBegin) / 1000000 + "ms.");
			} finally {
				if (out != null) { try { out.close(); } catch (IOException ex) { } }
			}
		} catch (Exception ex) {
			logger.error("Exception caught when querying repository: " + ex, ex);
		} finally {
			if (result != null) { result.close(); }
		}
    }
	
	public static void entityQuery(String repoName) throws Exception
    {
		logger.info("Entity query:");
		String queryString = "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
						  "PREFIX prov: <http://www.w3.org/ns/prov#>" +
						  "SELECT ?ent WHERE {" +
						  "	?ent rdf:type prov:Entity" +
						  "}";
		TupleQueryResult result = null;
		
        try {
			result = sCon.query(repoName, queryString);
			
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				Value s = bindingSet.getValue("ent");

				logger.info("   - " + s.stringValue());
			}
			
		} catch (Exception ex) {
			logger.error("Exception caught when querying repository: " + ex, ex);
		} finally {
			if (result != null) { result.close(); }
		}
    }
	
	public static void computeQuery(String repoName) throws Exception
    {
		logger.info("Compute query:");
		String queryString = "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
						  "PREFIX prov: <http://www.w3.org/ns/prov#>" +
						  "PREFIX bfprov: <http://www.it-innovation.soton.ac.uk/ontologies/bonfire-prov#>" +
						  "SELECT ?comp WHERE {" +
						  "	?comp rdf:type bfprov:Compute ." +
						  "}";
		TupleQueryResult result = null;
		
        try {
			result = sCon.query(repoName, queryString);
			
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				Value s = bindingSet.getValue("comp");

				logger.info("   - " + s.stringValue());
			}
			
		} catch (Exception ex) {
			logger.error("Exception caught when querying repository: " + ex, ex);
		} finally {
			if (result != null) { result.close(); }
		}
    }
	
	public static void agentQuery(String repoName) throws Exception
    {
		logger.info("Compute query:");
		String queryString = "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
						  "PREFIX prov: <http://www.w3.org/ns/prov#>" +
						  "SELECT ?agent WHERE {" +
						  "	?agent rdf:type prov:Agent ." +
						  "}";
		TupleQueryResult result = null;
		
        try {
			result = sCon.query(repoName, queryString);
			
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				Value s = bindingSet.getValue("agent");

				logger.info("   - " + s.stringValue());
			}
			
		} catch (Exception ex) {
			logger.error("Exception caught when querying repository: " + ex, ex);
		} finally {
			if (result != null) { result.close(); }
		}
    }
	
	/*
    public static Repository accessRepoRemotely(String repoName)
    {
        //log.info("Accessing " + repoName + " repo remotely (" + sesameServerURL + ")");
        String sesameServer = sesameServerURL;
        String repositoryID = repoName;
        
        try {
            sCon.connectRemote(sesameServer);
        } catch (SesameException ex) {
            logger.error("Failed to connect to the Sesame server");
            return null;
        }
        
        if (!sCon.isConnected()) {
            logger.error("Not connected to the Sesame server");
        }

        Repository myRepository = null;
        try {
            myRepository = sCon.getRepository(repositoryID);
        } catch (SesameException ex) {
            logger.error("Failed to access the repository: " + ex, ex);
            return null;
        }
        
        if (myRepository == null) {
            logger.error("Repository returned from SesameConnector is NULL");
            return null;
        }
        
//        log.info(" - Is repo initialised: " + myRepository.isInitialized());
//        if (myRepository.getDataDir() == null) {
//            log.info(" - Repo data dir is NULL");
//        } else {
//            log.info(" - Repo data dir: " + myRepository.getDataDir().getAbsolutePath());
//        }
        return myRepository;
    }
*/
/*
    public static Repository accessRepoRemotelyAlternative()
    {
        logger.info("Accessing " + testRepoName + " repo remotely (" + sesameServerURL + ")");
        String sesameServer = sesameServerURL;
        String repositoryID = testRepoName;

        Repository myRepository = new HTTPRepository(sesameServer, repositoryID);
        try {
            logger.info(" - Initialising " + repositoryID + " repo (remotely)");
            myRepository.initialize();
        } catch (RepositoryException ex) {
            logger.error(" - Exception caught when trying to initialise repository: " + ex, ex);
            return null;
        }
        
        logger.info(" - Is repo initialised: " + myRepository.isInitialized());
        if (myRepository.getDataDir() == null) {
            logger.info(" - Repo data dir is NULL");
        } else {
            logger.info(" - Repo data dir: " + myRepository.getDataDir().getAbsolutePath());
        }
        return myRepository;
    }
*/   

}
