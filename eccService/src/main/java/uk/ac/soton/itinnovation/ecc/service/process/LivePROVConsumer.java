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
//      Created By :            Simon Crowle
//      Created Date :          17-Apr-2014
//      Created for Project :   EccService
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.ecc.service.process;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.dao.EDMProvWriterImpl;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.db.EDMProvStoreWrapper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.*;

import org.slf4j.*;
import java.util.*;

public class LivePROVConsumer {

    private final Logger lpcLog = LoggerFactory.getLogger(LivePROVConsumer.class);

    private boolean repoInitialised;
    private String repoID;
    private EDMProvWriterImpl provStoreWriter;
    private EDMProvStoreWrapper provStoreWrapper;

    private HashMap<String, String> nsBaseURIMap; // name x baseURI
    private HashMap<String, String> nsPrefixMap;  // name x prefix

    public LivePROVConsumer() {
        initialiseNamespaces();
    }

    public boolean isRepoInitialised() {
        return repoInitialised;
    }

    public void createExperimentRepository(UUID expID, String expTitle, Properties repoProps) throws Exception {
        // Safety first
        if (expID == null || expTitle == null || repoProps == null) {
            throw new Exception("Could not create experiment repository - parameter(s) null");
        }
        if (repoInitialised) {
            throw new Exception("Could not create experiment repository - repository already initialised");
        }

        // TO DO: Validate properties
        try {
            provStoreWrapper = null;
            String expIDVal = expID.toString();

            lpcLog.info("Attempting to create PROV repository for experiment: " + expTitle + ": " + expIDVal);

            repoProps.setProperty("owlim.repositoryID", expIDVal);
            repoProps.setProperty("owlim.repositoryName", expTitle);

            // Check to see if repository exists (throw it if it does)
            provStoreWrapper = new EDMProvStoreWrapper(repoProps);
            if (provStoreWrapper.repositoryExists(expIDVal)) {
                throw new Exception("Could not create repository: it already exists");
            }

            // Create the new repository
            repoID = expIDVal;
            provStoreWrapper.createNewRepository(repoID, expTitle);
            provStoreWriter = new EDMProvWriterImpl(provStoreWrapper);
            createDefaultNamespaces();

            repoInitialised = true;
            lpcLog.info("Repository created OK");
        } catch (Exception ex) {
            repoID = null;

            String msg = "Could not create experiment repository: " + ex.getMessage();
            lpcLog.error(msg);

            throw new Exception(msg, ex);
        }
    }

    public void closeCurrentExperimentRepository() throws Exception {
        // Safety first
        if (!repoInitialised) {
            throw new Exception("Could not close repository - it has not been created");
        }

        try {
            lpcLog.info("Trying to close current experiment repository");

            provStoreWrapper.disconnect();

            repoInitialised = false;

            lpcLog.info("Repository closed");
        } catch (Exception ex) {
            String msg = "Could not close current repository: " + ex.getMessage();
            lpcLog.error(msg);

            throw new Exception(msg, ex);
        }
    }

    public void addPROVReport(EDMProvReport report) throws Exception {
        if (!repoInitialised) {
            throw new Exception("Could not add PROV report to repository - repository has not been created");
        }
        if (report == null) {
            throw new Exception("Could not add PROV report to repository - Report is null");
        }

        Collection<EDMTriple> triples = report.getTriples().values();
        if (triples == null) {
            throw new Exception("Could not add PROV report to repository - Triple set is null");
        }

        // Add triples, if there are some
        if (!triples.isEmpty()) {
            provStoreWriter.storeReport(report);
        } else {
            lpcLog.warn("PROV report contained no triples - droppped");
        }
    }

    // Private methods ---------------------------------------------------------
    private void initialiseNamespaces() {
        nsPrefixMap = new HashMap<String, String>();
        nsBaseURIMap = new HashMap<String, String>();

//        nsBaseURIMap.put("foaf", "http://xmlns.com/foaf/0.1/");
        nsBaseURIMap.put("foaf", "foaf.rdf");
//        nsBaseURIMap.put("sioc", "http://rdfs.org/sioc/ns#");
        nsBaseURIMap.put("sioc", "sioc.rdf");
//        nsBaseURIMap.put("prov", "http://www.w3.org/ns/prov-o#/");
        nsBaseURIMap.put("prov", "prov.rdf");
        nsBaseURIMap.put("experimedia", "experimedia.rdf");

        nsPrefixMap.put("foaf", "http://xmlns.com/foaf/0.1/");
        nsPrefixMap.put("sioc", "http://rdfs.org/sioc/ns#");
        nsPrefixMap.put("prov", "http://www.w3.org/ns/prov#");
        nsPrefixMap.put("experimedia", "http://it-innovation.soton.ac.uk/ontologies/experimedia#");
    }

    private void createDefaultNamespaces() throws Exception {
        try {
            // FOAF
//            provStoreWrapper.importOntologyToKnowledgeBase(nsBaseURIMap.get("foaf"),
//                    nsPrefixMap.get("foaf"),
//                    "foaf", LivePROVConsumer.class);

            // SIOC
            provStoreWrapper.importOntologyToKnowledgeBase(nsBaseURIMap.get("sioc"),
                    nsPrefixMap.get("sioc"),
                    "sioc", LivePROVConsumer.class);

            // PROV
            provStoreWrapper.importOntologyToKnowledgeBase(nsBaseURIMap.get("prov"),
                    nsPrefixMap.get("prov"),
                    "prov", LivePROVConsumer.class);

            // EXPERIMEDIA
            provStoreWrapper.importOntologyToKnowledgeBase(nsBaseURIMap.get("experimedia"),
                    nsPrefixMap.get("experimedia"),
                    "experimedia", LivePROVConsumer.class);
        } catch (Exception ex) {
            String msg = "Could not create default namespaces: " + ex.getMessage();
            lpcLog.error(msg);

            throw new Exception(msg, ex);
        }
    }

}
