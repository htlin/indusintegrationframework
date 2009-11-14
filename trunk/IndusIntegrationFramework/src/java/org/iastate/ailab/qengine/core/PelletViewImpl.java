
package org.iastate.ailab.qengine.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.exceptions.ConfigurationException;
import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;
import org.iastate.ailab.qengine.core.reasoners.impl.OntologyMapStore;
import org.iastate.ailab.qengine.core.reasoners.impl.OntologyStore;
import org.iastate.ailab.qengine.core.reasoners.impl.SchemaMapStore;
import org.iastate.ailab.qengine.core.util.DataTreeLoader;
import org.iastate.ailab.qengine.core.util.ReadPropFile;

/**
 * @author Roshan, Rohit
 * This class is used to initialize the configuration files in view.txt and store the
 * information contained in those files for later use.
 */

public class PelletViewImpl implements View {

   private static final Logger logger = Logger.getLogger(DefaultViewImpl.class);

   //TODO: Should we make it a singleton class

   private ViewConfigData viewConfig;

   private DataNode dTree;

   /**
    * This stores the mappings
    */
   private final SchemaMapStore schemaMapStore;

   /**
    * This stores the mappings between various ontolgies in user view and
    * data sources.
    */
   //private final OntologyMapStore ontologyMapStore;

   /**
    * This stores axioms of the ontologies associated with data schema
    * resources (columns in a table).
    */
   private final OntologyStore ontologyStore;

   public PelletViewImpl() {
      //allocate space for axiom store. It will be filled when creating DTree
      ontologyStore = SolutionCreator.getOntologyStoreImpl();

      /* ontologyMapStore
         ontologyMapStore = SolutionCreator.getOntologyMapStoreImpl(); */

      // schemaMapStore
      schemaMapStore = SolutionCreator.getSchemaMapStoreImpl();
   }

   public void init(ViewConfigData config) {
      //TODO: Better Exception Type

      this.viewConfig = config;

      /* fill the ontologyMap
       String ontologyMapFile = viewConfig.getOntologyMap(); */

   /*   try {
         ontologyMapStore.parse(ontologyMapFile, false);
      } catch (FileNotFoundException e) {
         throw new ConfigurationException(
               "FileNotFoundException for the ontology map file: "
                     + ontologyMapFile, e);
      } catch (IOException e) {
         throw new ConfigurationException(
               "IOException while trying to parse the ontology map file: "
                     + ontologyMapFile, e);
      } */

      //fill the schemaMap
      String schemaMapFile = viewConfig.getSchemaMap();
      try {
         schemaMapStore.parse(schemaMapFile, false);
      } catch (FileNotFoundException e) {
         throw new ConfigurationException(
               "FileNotFoundException for the schema map file: "
                     + schemaMapFile, e);
      } catch (IOException e) {
         throw new ConfigurationException(
               "ConfigurationException for the schema map file: "
                     + schemaMapFile, e);
      }

      //build the DTree. It also fills the ontologyStore
      dTree = buildDTree();
      
      //build the hash table for ont map properites
      String ontMappropertiesFilePath = viewConfig.getOntMapProperties();
      ReadPropFile.loadToHashTableFromOntmapPropertiesFile(ontMappropertiesFilePath); 
      
   }

   private DataNode buildDTree() {
      try {
         DataTreeLoader DTreeLoader = SolutionCreator.getDataTreeLoader();
         return DTreeLoader.getDataTree();
      } catch (RuntimeException e) {
         logger.error("RuntimeException creating DTee", e);
         throw e;
      }
   }

   public SchemaMapStore getSchemaMapStore() {
      return schemaMapStore;
   }

   /* public OntologyMapStore getOntologyMapStore() {
    
      return ontologyMapStore;
   } */

   public OntologyMapStore getOntologyMapStore() {
   
   return null;
} 
   public OntologyStore getOntologyStore() {
      return ontologyStore;
   }

   public DataNode getDTree() {
      return dTree;
   }

   public ViewConfigData getViewConfigData() {
      return viewConfig;
   }
}
