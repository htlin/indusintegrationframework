package org.iastate.ailab.qengine.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author neeraj A utility class to store and retrieve data associated
 * with UserView Configurations
 */
public class ViewConfigData {

   //template: 
   //UserViewName=DS1_DS2_DS3
   //DTreeFile=config/tree.xml
   //SchemaMap=config/schemamap.txt
   //OntologyMap=config/ontomap.txt

   //TODO: Should we make it an singleton class
   private String viewName;

   private String dTreeFile;

   private String schemaMap;

   private String ontologyMap;

   /**
    * base directory relative to which other paths are calculated. It is
    * the directory that contains the indusConfig File
    */

   private final String baseDirectoryAsString;

   private final File viewConfigFile;

   private boolean initialized = false;

   public ViewConfigData(File configFile) throws java.io.FileNotFoundException,
         java.io.IOException {
      FileInputStream in = new FileInputStream(configFile);
      Properties config = new Properties();
      config.load(in);
      init(config);
      baseDirectoryAsString = configFile.getParentFile().getCanonicalPath();

      viewConfigFile = configFile;
   }

   private void init(Properties config) {
      //TODO Verify relevant properties exist, throw exceptions as required
      //TODO  Verify properties is not null
      viewName = config.getProperty("UserViewName");
      dTreeFile = config.getProperty("DTreeFile");
      schemaMap = config.getProperty("SchemaMap");
      ontologyMap = config.getProperty("OntologyMap");
      initialized = true;
   }

   /**
    * Returns the view name
    * 
    * @return
    */
   public String getUserViewName() {
      return viewName;
   }

   /**
    * Returns a absolute path to the DTreeFile
    * 
    * @return
    */
   public String getDTreefile() {
      return baseDirectoryAsString + File.separator + dTreeFile;
   }

   /**
    * Returns absolute path to the schema map file
    * 
    * @return
    */

   public String getSchemaMap() {
      return baseDirectoryAsString + File.separator + schemaMap;
   }

   /**
    * Returns absolute path to the Ontology Map File
    * 
    * @return
    */

   public String getOntologyMap() {
      return baseDirectoryAsString + File.separator + ontologyMap;
   }

   public boolean isInitialized() {
      return initialized;
   }

   /**
    * Returns the base directory with relative to which calculate the other
    * configurationFile
    * 
    * @return
    */
   public String getBaseDirectoryAsString() {
      return baseDirectoryAsString;
   }

   public File getConfigFile() {
      return viewConfigFile;
   }

}
