/**
 * @author neeraj
 */

package org.iastate.ailab.qengine.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.datasource.DataSourceDescriptor;
import org.iastate.ailab.qengine.core.exceptions.ConfigurationException;
import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;
import org.iastate.ailab.qengine.core.reasoners.impl.Reasoner;

/**
 * Access to the View should only be through this Class
 */
public class Init {

   private static final Logger logger = Logger.getLogger(Init.class);

   private File viewConfigFile; // handle to the view Configuration  File

   // private final Properties config = new Properties(); //View Configuration as Properties

   private ViewConfigData viewConfigData = null;

   private View viewContext = null;

   //private boolean initialized = false;
   private boolean configured = false;

   //A Class associates with Configuration of the Indus System
   private IndusConfiguration indusConfig = null;

   private static final Init init = new Init();

   private Init() {
      //make constructor private for Singleton
   }

   public static Init _this() {
      return init;
   }

   /*
    * A map between the NodeName/DataSourceName and its associated
    * reasoner. This allows reasoners to be cached particularly important
    * if they store important results
    */
   private final Map<String, Reasoner> reasonerCache = new HashMap<String, Reasoner>();

   public void init(File indusConfigFile, File viewConfigFile)
         throws IOException, FileNotFoundException {

      this.viewConfigFile = viewConfigFile;
      indusConfig = new IndusConfiguration(indusConfigFile);
      configured = true;

      //build the view.
      getViewContext();

   }

   /**
    * This returns a Data Structure which provides access to the various
    * parameters with which the View was Configured
    * 
    * @return
    */
   public ViewConfigData getViewConfigData() {
      if (viewConfigData != null) {
         return viewConfigData;
      }
      if (!configured) {
         throw new ConfigurationException(
               "org.iastate.ailab.qengine.core.Init.getViewConfigData() was called without proper configuration");
      }
      try {
         viewConfigData = new ViewConfigData(viewConfigFile);
      } catch (Exception e) {
         throw new ConfigurationException(
               "Error building View Configuration. Check Configuration Files ",
               e);
      }
      //viewConfigData.init(config);
      //initialized = true;
      return viewConfigData;
   }

   /**
    * returns a representation of the current View
    * 
    * @return
    */
   public View getViewContext() {
      //return viewContext if it has already been built
      if (viewContext != null) {
         return viewContext;
      }

      //first call, build the viewContext
      ViewConfigData localViewConfigData = getViewConfigData();
      try {
         viewContext = SolutionCreator.getViewImpl();

         //use a function call with init to get ConfigData since it may not have been initialized
         viewContext.init(localViewConfigData);
      } catch (RuntimeException e) {
         String message = "FATAL ERROR 000: Cannot build View";
         logger.fatal(message, e);
         throw e;
      }

      return viewContext;
   }

   /**
    * A utility function which provides access to the DataSourceDescriptor
    * of root node which corresponds to user View
    * 
    * @return
    */
   public DataSourceDescriptor getUserViewDataSourceDescriptor() {
      return getViewContext().getDTree().getDataSourceDescriptor();
   }

   /**
    * A utility functions that returns a queryTransformer
    * 
    * @param nodeName currently ignored
    * @return
    */
   public QueryTransFormer getNodeQueryTransFormer(String nodeName) {
      //Currently nodeName is ignored as all nodes have the same queryTransfoirmer
      //In future individual nodes may their own transformers

      return SolutionCreator.getQueryTransformerImpl();
   }

   /**
    * Returns a Reasoner for the particular dataSource. if it does not
    * exist in Reasoner Cache a new Instance is created.
    * 
    * @param nodeName
    * @return
    */
   public Reasoner getReasonerFromCache(String nodeName) {
      //If the built MappingGraphs are stored in Reasoner, Cache will ensure their re use
      if (reasonerCache.containsKey(nodeName)) {
         return reasonerCache.get(nodeName);
      }
      Reasoner reasoner = SolutionCreator.getReasonerImpl();
      reasonerCache.put(nodeName, reasoner); //store it
      return reasoner;
   }

   public void clearReasonerFromCache(String nodeName) {
      reasonerCache.remove(nodeName);
   }

   /**
    * provide Access to Indus configuration
    * 
    * @return
    */
   public IndusConfiguration getIndusConfiguration() {
      return indusConfig;
   }
}
