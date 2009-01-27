package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Dictionary;

import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;
import org.iastate.ailab.qengine.core.reasoners.interfaces.Axiom;

public class DefaultOntologyMapStoreImpl implements OntologyMapStore {

   public class OntologyMap {

      /**
       * Stores the mappings between ontologies as Axioms. It is indexed by
       * URI of the user view The Axioms are always from the user View
       * ontology to the target ontology The intended use is that it will
       * store all the mappings for a partcular data source. THE URI is
       * used to index the mappings for a particular ontology which may be
       * associated with a column/attribute
       */
      Dictionary<URI, Set<Axiom>> mappings = new Hashtable<URI, Set<Axiom>>();

      public Set<Axiom> getMappedAxioms(URI userViewOntologyID) {
         return mappings.get(userViewOntologyID);
      }

      public void setMappedAxioms(URI userviewOntologyID, Set<Axiom> axioms) {
         mappings.put(userviewOntologyID, axioms);
      }
   }

   OntologyMapParser parser = null; //initialized in Constructor

   public DefaultOntologyMapStoreImpl() {
      parser = SolutionCreator.getOntologyMapParserImpl();
   }

   /**
    * Stores the mappings(as Axioms) for a particular data source
    * (identified by the String)
    */
   private Dictionary<String, OntologyMap> dataSourceMappings = new Hashtable<String, OntologyMap>();

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
         String targetDataSource) {
      /*
       * currently mappings between user Ontology and DataSource ontology
       * can be only controlled at data source level. If same ontology is
       * applicable to different attribute and you want to make different
       * mappings between these two we will need to control this at
       * attribute level(FUTURE)
       */
      OntologyMap dsMappings = dataSourceMappings.get(targetDataSource);
      return dsMappings.getMappedAxioms(userViewOntologyID);

      //return  dataSourceMappings.get(targetDataSource).getMappedAxioms(userViewOntologyID);
   }

   /**
    * @param userViewOntologyID to which user view ontology the mapping is
    * applicable
    * @param mapping The mapping stored as an Axiom between concepts
    * @param dataSourceName The data Source to which the mapping is
    * applicable (needed because different data sources may use same
    * ontology but the mapping may be different)
    */
   public void addMapping(URI userViewOntologyID, Axiom mapping,
         String dataSourceName) {
      OntologyMap map = dataSourceMappings.get(dataSourceName);
      if (map == null) {
         //this is the first mapping for this data source, hence create an object
         map = new OntologyMap();
      }

      Set<Axiom> axioms = map.getMappedAxioms(userViewOntologyID);
      if (axioms == null)
         axioms = new HashSet<Axiom>();
      axioms.add(mapping); //add the current mappings
      map.setMappedAxioms(userViewOntologyID, axioms); //update the OntologyMap
      dataSourceMappings.put(dataSourceName, map); //store the updated OntologyMap
   }

   public void parse(String source, boolean inline)
         throws FileNotFoundException, IOException {
      parser.parse(source, inline, this);
   }
}
