package org.iastate.ailab.qengine.core.reasoners.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;

public class DefaultOntologyStoreImpl implements OntologyStore {

   OntologyParser parser = SolutionCreator.getOntologyParserImpl();

   /**
    * A map between the URI and the associated Ontology
    */
   Map<URI, OntologyI> store = new HashMap<URI, OntologyI>();

   public DefaultOntologyStoreImpl() {
      //must define the default constructor because it can throw an exception
   }

   /**
    * Returns the ontology associated wit this URI
    * 
    * @param ontologyId
    * @return
    */
   public OntologyI getOntology(URI ontologyId) {
      //TODO: Throw Exception if no ontology found
      return store.get(ontologyId);
   }

   /**
    * Add the ontology to the store identified by ontologyId
    * 
    * @param ontologyId URI of the Ontology
    * @param ontology
    */
   public void addOntology(URI ontologyId, OntologyI ontology) {
      store.put(ontologyId, ontology);
   }

   public void parse(String source, boolean inline) {
      //pass it to the parser which will the store
      parser.parse(source, inline, this);
   }
}
