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
//      Created Date :          04-Oct-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.UUID;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple.TRIPLE_TYPE;

/**
 * This is a class which implements the functionality shared by the three
 * Provenance elements (Agent, Activity and Entity). All the information for
 * prov individuals is stored in this class in triple form. This includes
 * outgoing as well as incoming relationships. While creating elements, only the
 * outgoing relationships are necessary but later when retrieving them from the
 * store, incoming relationships need to be included for visualisation purposes.
 */
public class EDMProvBaseElement {

    private UUID instanceID;
    private PROV_TYPE provType = PROV_TYPE.ePROV_UNKNOWN_TYPE;
    private String iri;
    private String prefix;
    private String uniqueIdentifier;
    private HashMap<UUID, EDMTriple> triples;

    protected static SimpleDateFormat format = new SimpleDateFormat("\"yyyy-MM-dd'T'HH:mm:ss'Z\"^^xsd:dateTime'");

    protected static final String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    protected static final String rdfsLabel = "http://www.w3.org/2000/01/rdf-schema#label";
    protected static final String prov = "http://www.w3.org/ns/prov#";

    public enum PROV_TYPE {

        ePROV_UNKNOWN_TYPE,
        ePROV_ENTITY,
        ePROV_AGENT,
        ePROV_ACTIVITY
    };

    /**
     * Creates an EDMProvBaseElement
     *
     * @param prefix the prefix of the element
     * @param uniqueIdentifier
     * @param a unique identifier. This could be something like domain_uniqueID,
     * e.g. facebook_56735762153. It needs to be unique across clients.
     * @param label a human readable name
     */
    public EDMProvBaseElement(String prefix, String uniqueIdentifier, String label) {
        this.instanceID = UUID.randomUUID();
        this.prefix = prefix;
        this.uniqueIdentifier = uniqueIdentifier;
        this.iri = prefix + uniqueIdentifier;
        this.triples = new HashMap<UUID, EDMTriple>();

        EDMProvBaseElement.format.setTimeZone(TimeZone.getTimeZone("UTC"));

        setLabel(label);
    }

    /**
     * Returns the label, if it exists. Otherwise falls back to IRI
     *
     * @return the label or IRI
     */
    public String getFriendlyName() {
        for (Entry<UUID, EDMTriple> e : this.getTriplesWithPredicate(rdfsLabel).entrySet()) {
            String friendlyName = e.getValue().getObject();
            //cut type in case of "proper" usage (e.g. "Label"^^xsd:string)
            if (friendlyName.indexOf("\"") >= 0) {
                friendlyName = friendlyName.substring(friendlyName.indexOf("\"") + 1, friendlyName.lastIndexOf("\""));
            }
            return friendlyName;
        }
        return this.iri;
    }

    @Override
    public String toString() {
        String contents = "[" + this.getProvType() + "] " + this.getFriendlyName() + " (" + this.iri + ")\n";
        for (Entry<UUID, EDMTriple> e : this.triples.entrySet()) {
            contents += "\t" + e.getValue().toString() + "\n";
        }
        return contents;
    }

    /**
     * Checks for the existence of a certain triple
     *
     * @param triple The triple for which it should be checked
     * @return Whether that triple is already in the element or not
     */
    public boolean contains(EDMTriple triple) {
        boolean contains = false;
        for (Entry<UUID, EDMTriple> e : this.triples.entrySet()) {
            if (e.getValue().equals(triple)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    /**
     * Get all the prefixes from triples in this element.
     *
     * @return the prefixes
     */
    public HashSet<String> getPrefixes() {
        HashSet<String> prefixes = new HashSet<String>();
        for (Entry<UUID, EDMTriple> e : this.triples.entrySet()) {
            if (!prefixes.contains(e.getValue().getPredicatePrefix())) {
                prefixes.add(e.getValue().getPredicatePrefix());
            }
        }
        return prefixes;
    }

    /**
     * Returns triples of a specific type and/or prefix. null means it isn't a
     * restriction, so getTriples(null, null); returns the exact same result as
     * getTriples();
     *
     * @param type the type of triples that should be returned
     * @param prefix the prefix of the predicate to look for
     * @return the triples of the specified type
     */
    public HashMap<UUID, EDMTriple> getTriples(EDMTriple.TRIPLE_TYPE type, String prefix) {
        HashMap<UUID, EDMTriple> result = new HashMap<UUID, EDMTriple>();
        //check all triples
        for (Entry<UUID, EDMTriple> e : triples.entrySet()) {
            //check for type if applicable
            if (type != null && e.getValue().getType() != type) {
                continue;
            }
            //check for prefix if applicable
            if (prefix != null && !e.getValue().getPredicatePrefix().equals(prefix)) {
                continue;
            }
            result.put(e.getKey(), e.getValue());
        }
        return result;
    }

    /**
     * Return specific triples
     *
     * @param pred The predicate of the triple
     * @return All the triples that match the given predicate
     */
    public HashMap<UUID, EDMTriple> getTriplesWithPredicate(String pred) {
        HashMap<UUID, EDMTriple> result = new HashMap<UUID, EDMTriple>();

        if (pred != null) {
            for (Entry<UUID, EDMTriple> e : triples.entrySet()) {
                if (e.getValue().hasPredicate(pred)) {
                    result.put(e.getKey(), e.getValue());
                }
            }
        }

        return result;
    }

    /**
     * Returns only the incoming triples, i.e. triples in which the element
     * itself is the object.
     *
     * @return the incoming triples
     */
    public HashMap<UUID, EDMTriple> getIncomingTriples() {
        HashMap<UUID, EDMTriple> result = new HashMap<UUID, EDMTriple>();
        for (EDMTriple t : triples.values()) {
            if (t.getObject().equals(iri)) {
                result.put(t.getID(), t);
            }
        }
        return result;
    }

    /**
     * Returns only the outgoing triples, i.e. triples in which the element
     * itself is the subject.
     *
     * @return the outgoing triples
     */
    public HashMap<UUID, EDMTriple> getOutgoingTriples() {
        HashMap<UUID, EDMTriple> result = new HashMap<UUID, EDMTriple>();
        for (EDMTriple t : triples.values()) {
            if (t.getSubject().equals(iri)) {
                result.put(t.getID(), t);
            }
        }
        return result;
    }

    /**
     * Adds a triple to the element with the element itself as subject
     *
     * @param predicate the predicate of the new triple
     * @param object the object of the new triple
     */
    public void addTriple(String predicate, String object) {
        this.addTriple(predicate, object, TRIPLE_TYPE.UNKNOWN_TYPE);
    }

    /**
     * Adds a triple of a specific typr to the element
     *
     * @param predicate the predicate of the new triple
     * @param object the object of the new triple
     * @param type the triple type
     */
    public void addTriple(String predicate, String object, TRIPLE_TYPE type) {
        this.addTriple(this.iri, predicate, object, type);
    }

    private void addTriple(String subject, String predicate, String object, TRIPLE_TYPE type) {
        EDMTriple newTriple = new EDMTriple(subject, predicate, object, type);
        boolean contained = false;
        //we have to iterate because UUIDs might be different for same triples (subject/predicate/object)
        for (EDMTriple t : this.triples.values()) {
            if (newTriple.equals(t)) {
                contained = true;
                break;
            }
        }
        if (!contained) {
            this.triples.put(newTriple.getID(), newTriple);
        }
    }

    /**
     * Remove a specified triple from the list of triples. The subject is always
     * the element itself.
     *
     * @param predicate the predicate of the triple to remove
     * @param object the object of the triple to remove
     */
    public void removeTriple(String predicate, String object) {
        this.removeTriple(this.iri, predicate, object);
    }

    private void removeTriple(String subject, String predicate, String object) {
        EDMTriple triple = new EDMTriple(subject, predicate, object);
        if (this.triples.containsValue(triple)) {
            this.triples.remove(triple.getID());
        }
    }

    /**
     * Adds a relationship to the element to connect it to a location
     *
     * @param location the element which represents the location
     */
    public void atLocation(EDMProvBaseElement location) {
    	//note that the EDMBaseElement doesn't need to be classified as a prov:Location;
        //this will be discovered by the reasoner automatically
        this.addTriple(prov + "atLocation", location.iri);
    }

    /**
     * Adds a class assertion making the element an individual of that class.
     *
     * @param c the class to which the element should belong
     */
    public void addOwlClass(String c) {
        this.addTriple(this.iri, rdfType, c, TRIPLE_TYPE.CLASS_ASSERTION);
    }

    /**
     * Removes the class assertion
     *
     * @param c The class of which the assertion should be removed
     */
    public void removeOwlClass(String c) {
        //TODO: what if this is called on an existing element (already in store)?
        this.removeTriple(this.iri, rdfType, c);
    }

    //GETTERS/SETTERS//////////////////////////////////////////////////////////////////////////////
    /**
     * Get the element's IRI
     *
     * @see org.semanticweb.owlapi.model.IRI
     * @return a String representation of the IRI
     */
    public String getIri() {
        return iri;
    }

    /**
     * This returns the instance ID, a UUID which links the prov model to the
     * metric model.
     *
     * @return the instance id
     */
    public UUID getInstanceID() {
        return instanceID;
    }

    /**
     * This returns the prov type, which can be one of the following values.
     *
     * ePROV_UNKNOWN_TYPE, ePROV_ENTITY, ePROV_AGENT, ePROV_ACTIVITY
     *
     * @return
     */
    public PROV_TYPE getProvType() {
        return provType;
    }

    /**
     * Returns all the triples related to the element. This can be both,
     * incoming and outgoing relationships, i.e. the element itself can be the
     * subject or object of the triple.
     *
     * @return the triples
     */
    public HashMap<UUID, EDMTriple> getTriples() {
        return triples;
    }

    /**
     * This returns the prefix of the element. The prefix is internally kept as
     * the full prefix, even when only the short prefix is used for the creation
     * of the element.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the local name of the element. This is the IRI without the
     * prefix.
     *
     * @return the local name
     */
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    /**
     * Sets an element's prov type. Though the unknown type exists as a
     * fallback, it is not recommended to use it. Possible values are:
     *
     * ePROV_UNKNOWN_TYPE, ePROV_ENTITY, ePROV_AGENT, ePROV_ACTIVITY
     *
     * @param provType the prov type
     */
    public void setProvType(PROV_TYPE provType) {
        this.provType = provType;
    }

    /**
     * Sets a label for the element, which will be used for rendering the
     * element in the UI.
     *
     * @param label the label
     */
    public void setLabel(String label) {
        if (label != null) {
            //escape quotes in label
            label = label.replace("\"", "\\\"");
            EDMTriple triple = new EDMTriple(this.iri, rdfsLabel,
                    "\"" + label + "\"^^xsd:string", TRIPLE_TYPE.ANNOTATION_PROPERTY);
            if (!this.contains(triple)) {
                triples.put(triple.getID(), triple);
            }
        }
    }

}
