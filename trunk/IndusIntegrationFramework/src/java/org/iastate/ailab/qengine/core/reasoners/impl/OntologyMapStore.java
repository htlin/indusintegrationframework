package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.iastate.ailab.qengine.core.reasoners.interfaces.Axiom;

public interface OntologyMapStore {
   /**
    * returns the mappings from a particular ontology in the user view to
    * the target data source Mappings are always stored as: userOntology
    * relationShip dataSourceOntology
    * 
    * @param userViewOntologyId
    * @param targetDataSource
    * @return
    */
   public Set<Axiom> getMappings(URI userViewOntologyID,
         String targetDataSource);

   /**
    * Parses the ontologyMap
    * 
    * @param source Handler to get access to ontologyMap
    * @param inline
    * <ul>
    * <li> true means the source itself contains the ontology Map </li>
    * </ul>
    * @throws IOException
    * @throws FileNotFoundException
    */
   public void parse(String source, boolean inline) throws FileNotFoundException, IOException;

   /**
    * Add a Mapping to the store
    * 
    * @param userViewOntologyID to which user view ontology the mapping is
    * applicable
    * @param mapping The mapping stored as an Axiom between concepts
    * @param dataSourceName The data Source to which the mapping is
    * applicable (needed because different data sources may use same
    * ontology but the mapping may be different)
    */
   public void addMapping(URI userViewOntologyID, Axiom mapping,
         String dataSourceName);
}
