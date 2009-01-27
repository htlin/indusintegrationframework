package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

public interface OntologyStore {

   /**
    * Returns the ontology associated wit this URI
    * 
    * @param ontologyId
    * @return
    */
   public OntologyI getOntology(URI ontologyId);

   /**
    * Parses the source and adds it to the store
    * 
    * @param source
    * @param inline
    * @throws IOException
    * @throws FileNotFoundException
    */
   public void parse(String source, boolean inline)
         throws FileNotFoundException, IOException;

   /**
    * Add the ontology to the store identified by ontologyId
    * 
    * @param ontologyId URI of the Ontology
    * @param ontology
    */
   public void addOntology(URI ontologyid, OntologyI ontology);
}
