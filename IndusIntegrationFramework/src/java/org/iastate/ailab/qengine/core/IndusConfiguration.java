package org.iastate.ailab.qengine.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.exceptions.ConfigurationException;

/**
 * @author neeraj This serves as handler for getting values used to
 * configure Indus
 */
public class IndusConfiguration {

   private static final Logger logger = Logger
         .getLogger(IndusConfiguration.class);

   private final File indusConfigFile;

   public enum DbType {
      mysql, postgre
   }

   //TODO: Make Singleton Class
   Properties config = new Properties();

   public IndusConfiguration(File configFile) {
      this.indusConfigFile = configFile;

      /*
       * String configFile = System.getProperty("user.dir") +
       * File.separator + "config" + File.separator + "indus.conf";
       */

      FileInputStream in = null;
      try {
         in = new FileInputStream(configFile);
         config.load(in);
      } catch (FileNotFoundException e) {
         String message = "FileNotFoundException for the indus.conf file: "
               + configFile;
         logger.error(message, e);
         throw new ConfigurationException(message, e);
      } catch (IOException e) {
         String message = "IOException while trying to load the FileInputStream of the indus.conf file: "
               + in;
         logger.error(message, e);
         throw new ConfigurationException(message, e);
      }
   }

   public String getProperty(String key, String defaultValue) {
      return config.getProperty(key);
   }

   /**
    * What type of database is indus configured with default is mysql.
    * corresponds to key dbtype_indus in configuration
    * 
    * @return
    */
   public DbType getIndusDbType() {
      //assume mysql as default
      String val = config.getProperty("dbtype_indus", "mysql");
      return DbType.valueOf(val.trim().toLowerCase());
   }

   /**
    * What is the database where indus stores its intermediatary results
    * 
    * @return
    */
   public String getIndusDbName() {
      return config.getProperty("dbname_indus", "indus");
   }

   /**
    * What is the database associated with a dataSource name in Dtree
    * Defaults to the dataSourceName. It can be overriden by associating a
    * property dbname_<dataSourceName> This allows changing the underlying
    * association of databases
    * 
    * @return
    */
   public String getDataSourceDbName(String dataSourceName) {
      String key = "dbname_" + dataSourceName;
      return config.getProperty(key, "dataSourceName");
   }

   /**
    * @return the indusConfigFile
    */
   public File getIndusConfigFile() {
      return indusConfigFile;
   }

}
