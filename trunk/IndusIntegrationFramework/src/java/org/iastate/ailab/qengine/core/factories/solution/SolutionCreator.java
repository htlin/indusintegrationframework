package org.iastate.ailab.qengine.core.factories.solution;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.Init;
import org.iastate.ailab.qengine.core.QueryTransFormer;
import org.iastate.ailab.qengine.core.RequestFlow;
import org.iastate.ailab.qengine.core.ResponseFlow;
import org.iastate.ailab.qengine.core.View;
import org.iastate.ailab.qengine.core.aggregators.DataAggregator;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.datasource.DataSourceDescriptor;
import org.iastate.ailab.qengine.core.exceptions.SolutionCreaterException;
import org.iastate.ailab.qengine.core.reasoners.impl.OntologyI;
import org.iastate.ailab.qengine.core.reasoners.impl.OntologyMapParser;
import org.iastate.ailab.qengine.core.reasoners.impl.OntologyMapStore;
import org.iastate.ailab.qengine.core.reasoners.impl.OntologyParser;
import org.iastate.ailab.qengine.core.reasoners.impl.OntologyStore;
import org.iastate.ailab.qengine.core.reasoners.impl.Reasoner;
import org.iastate.ailab.qengine.core.reasoners.impl.SchemaMapParser;
import org.iastate.ailab.qengine.core.reasoners.impl.SchemaMapStore;
import org.iastate.ailab.qengine.core.util.DataSourceDescriptorLoader;
import org.iastate.ailab.qengine.core.util.DataTreeLoader;
import org.iastate.ailab.qengine.core.util.SchemaMapLoader;

/**
 * The Solution creator is a factory that returns the run time
 * implementations of the various interfaces. Custom developers can
 * override the default implementations via config file
 * 
 * @author neeraj
 */
public class SolutionCreator {

   private static final Logger logger = Logger.getLogger(SolutionCreator.class);

   /**
    * Strings associating default Implementations of the various Interfaces
    */
   private static final String viewImplClass = "org.iastate.ailab.qengine.core.DefaultViewImpl";

   private static final String queryTransformerImplClass = "org.iastate.ailab.qengine.core.DefaultQueryTransFormer";

   private static final String schemaMapStoreImplClass = "org.iastate.ailab.qengine.core.reasoners.impl.DefaultSchemaMapStoreImpl";

   private static final String ontologyMapStoreImplClass = "org.iastate.ailab.qengine.core.reasoners.impl.DefaultOntologyMapStoreImpl";

   private static final String ontologyStoreImplClass = "org.iastate.ailab.qengine.core.reasoners.impl.DefaultOntologyStoreImpl";

   private static final String ontologyImplClass = "org.iastate.ailab.qengine.core.reasoners.impl.DefaultOntologyImpl";

   private static final String ontologyParserImplClass = "org.iastate.ailab.qengine.core.reasoners.impl.DefaultOntologyParserImpl";

   private static final String ontologyMapParserImplClass = "org.iastate.ailab.qengine.core.reasoners.impl.DefaultOntologyMapParserImpl";

   private static final String schemaMapParserImplClass = "org.iastate.ailab.qengine.core.reasoners.impl.DefaultSchemaMapParserImpl";

   private static final String reasonerImplClass = "org.iastate.ailab.qengine.core.reasoners.impl.DefaultReasonerImpl";

   private static final String dataAggregatorImpClass = "org.iastate.ailab.qengine.core.aggregators.DefaultDataAggregatorImplementation";

   private static final String dataNodeImpClass = "org.iastate.ailab.qengine.core.datasource.DefaultDataNodeImplementation";

   private static final String dataSourceDescriptorImpClass = "org.iastate.ailab.qengine.core.datasource.DefaultDataSourceDescriptorImplementation";

   private static final String dataSourceDescriptorLoaderImpClass = "org.iastate.ailab.qengine.core.util.DataSourceDescriptorXMLLoader";

   private static final String dataTreeLoaderImpClass = "org.iastate.ailab.qengine.core.util.SAXDataTreeLoader";

   private static final String requestFlowImpClass = "org.iastate.ailab.qengine.core.DefaultRequestFlowImplementation";

   private static final String responseFlowImpClass = "org.iastate.ailab.qengine.core.DefaultResponseFlowImplementation";

   private static final String schemaMapLoader = "org.iastate.ailab.qengine.core.util.SchemaMapPropertyLoader";

   private static Properties implClass = new Properties();

   // A File that contains custom classes that override the default solution file
   private static File customSolutionConfigFile = Init._this()
         .getIndusConfiguration().getIndusConfigFile();

   private static boolean initialized = false;

   private SolutionCreator() {
      //Do not allow instantiations
   }

   public static void init() {
      if (initialized)
         return;
      try {
         implClass.load(new FileInputStream(customSolutionConfigFile));
         //implClass.load(new FileInputStream(customSolutionConfigFile));
      } catch (IOException e) {
         logger.error("Could not load the file '" + customSolutionConfigFile
               + "'.  Will have to use all default implemenations of classes.",
               e);
      }

   }

   public static Object instantiate(String className) {
      Object obj = null;

      try {
         obj = Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return obj;
   }

   /**
    * Returns an implementation of View
    * 
    * @return
    */
   public static View getViewImpl() {
      init();
      String className = implClass.getProperty("viewImplClass", viewImplClass);
      View view = null;

      try {
         view = (View) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return view;
   }

   /**
    * Returns an implementation of SchemaMapStore
    * 
    * @return
    */
   public static SchemaMapStore getSchemaMapStoreImpl() {
      init();
      String className = implClass.getProperty("schemaMapStoreImplClass",
            schemaMapStoreImplClass);
      SchemaMapStore schemaMapStore = null;

      try {
         schemaMapStore = (SchemaMapStore) Class.forName(className)
               .newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return schemaMapStore;
   }

   /**
    * Returns an implementation of OntologyMapStore
    * 
    * @return
    */
   public static OntologyMapStore getOntologyMapStoreImpl() {
      init();
      String className = implClass.getProperty("ontologyMapStoreImplClass",
            ontologyMapStoreImplClass);
      OntologyMapStore ontMapStore = null;

      try {
         ontMapStore = (OntologyMapStore) Class.forName(className)
               .newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return ontMapStore;
   }

   /**
    * Returns an implementation of OntologyStore
    * 
    * @return
    */
   public static OntologyStore getOntologyStoreImpl() {
      init();
      String className = implClass.getProperty("ontologyStoreImplClass",
            ontologyStoreImplClass);
      OntologyStore ontStore = null;

      try {
         ontStore = (OntologyStore) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return ontStore;
   }

   /**
    * Returns an implementation of OntologyI
    * 
    * @return
    */
   public static OntologyI getOntologyImpl() {
      init();
      String className = implClass.getProperty("ontologyImplClass",
            ontologyImplClass);
      OntologyI ont = null;

      try {
         ont = (OntologyI) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return ont;
   }

   /**
    * Returns an implementation of OntologyParser
    * 
    * @return
    */
   public static OntologyParser getOntologyParserImpl() {
      init();
      String className = implClass.getProperty("ontologyParserImplClass",
            ontologyParserImplClass);
      OntologyParser parser = null;

      try {
         parser = (OntologyParser) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return parser;
   }

   /**
    * rReturns an implementation of OntologyMapParser
    * 
    * @return
    */
   public static OntologyMapParser getOntologyMapParserImpl() {
      init();
      String className = implClass.getProperty("ontologyMapParserImplClass",
            ontologyMapParserImplClass);
      OntologyMapParser ontoMapParser = null;

      try {
         ontoMapParser = (OntologyMapParser) Class.forName(className)
               .newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return ontoMapParser;
   }

   /**
    * Returns an implementation of SchemaMapParser&lt;?&gt;
    * 
    * @return
    */
   public static SchemaMapParser<?> getSchemaMapParserImpl() {
      init();
      String className = implClass.getProperty("schemaMapParserImplClass",
            schemaMapParserImplClass);
      SchemaMapParser<?> parser = null;

      try {
         parser = (SchemaMapParser<?>) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return parser;
   }

   /**
    * Returns an implementation of Reasoner
    * 
    * @return
    */
   public static Reasoner getReasonerImpl() {
      init();
      String className = implClass.getProperty("reasonerImplClass",
            reasonerImplClass);
      Reasoner reasoner = null;

      try {
         reasoner = (Reasoner) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return reasoner;
   }

   /**
    * Returns an implementation of QueryTransformer
    * 
    * @return
    */
   public static QueryTransFormer getQueryTransformerImpl() {
      init();
      String className = implClass.getProperty("queryTransformerImplClass",
            queryTransformerImplClass);
      QueryTransFormer transformer = null;
      try {
         transformer = (QueryTransFormer) Class.forName(className)
               .newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return transformer;
   }

   /**
    * Returns an implementation of DataAggregator
    * 
    * @return
    */
   public static DataAggregator getDataAggregatorImpl() {
      init();
      String className = implClass.getProperty("dataAggregatorImpClass",
            dataAggregatorImpClass);
      DataAggregator aggregator = null;
      try {
         aggregator = (DataAggregator) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return aggregator;
   }

   /**
    * Returns an implementation of DataNode
    * 
    * @return
    */
   public static DataNode getDataNode() {
      init();
      String className = implClass.getProperty("dataNodeImpClass",
            dataNodeImpClass);
      DataNode node = null;
      try {
         node = (DataNode) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return node;
   }

   /**
    * Returns an implementation of DataSourceDescriptor
    * 
    * @return
    */
   public static DataSourceDescriptor getDataSourceDescriptor() {
      init();
      String className = implClass.getProperty("dataSourceDescriptorImpClass",
            dataSourceDescriptorImpClass);
      DataSourceDescriptor desc = null;
      try {
         desc = (DataSourceDescriptor) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return desc;
   }

   /**
    * Returns an implementation of DataSourceDescriptorLoader
    * 
    * @return
    */
   public static DataSourceDescriptorLoader getDatasourceDescriptorLoader() {
      init();
      String className = implClass.getProperty(
            "dataSourceDescriptorLoaderImpClass",
            dataSourceDescriptorLoaderImpClass);
      DataSourceDescriptorLoader loader = null;
      try {
         loader = (DataSourceDescriptorLoader) Class.forName(className)
               .newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return loader;
   }

   /**
    * Returns an implementation of DataTreeLoader
    * 
    * @return
    */
   public static DataTreeLoader getDataTreeLoader() {
      init();
      String className = implClass.getProperty("dataTreeLoaderImpClass",
            dataTreeLoaderImpClass);
      DataTreeLoader loader = null;
      try {
         loader = (DataTreeLoader) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return loader;
   }

   /**
    * Returns an implementation of RequestFlow
    * 
    * @return
    */
   public static RequestFlow getRequestFlow() {
      init();
      String className = implClass.getProperty("requestFlowImpClass",
            requestFlowImpClass);
      RequestFlow flow = null;
      try {
         flow = (RequestFlow) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return flow;
   }

   /**
    * Returns an implementation of ResponseFlow
    * 
    * @return
    */
   public static ResponseFlow getResponseFlow() {
      init();
      String className = implClass.getProperty("responseFlowImpClass",
            responseFlowImpClass);
      ResponseFlow flow = null;
      try {
         flow = (ResponseFlow) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return flow;
   }

   /**
    * Returns an implementation of SchemaMapLoader
    * 
    * @return
    */
   public static SchemaMapLoader getSchemaMapLoader() {
      init();
      String className = implClass.getProperty("schemaMapLoader",
            schemaMapLoader);
      SchemaMapLoader loader = null;
      try {
         loader = (SchemaMapLoader) Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
         wrapClassNotFoundException(className, e);
      } catch (ExceptionInInitializerError e) {
         wrapExceptionInInitializerError(className, e);
      } catch (LinkageError e) {
         wrapLinkageError(className, e);
      } catch (InstantiationException e) {
         wrapInstantiationException(className, e);
      } catch (IllegalAccessException e) {
         wrapIllegalAccessException(className, e);
      } catch (Exception e) {
         wrapException(className, e);
      }
      return loader;
   }

   private static void wrapClassNotFoundException(String instantiatingClass,
         ClassNotFoundException e) {
      throwSolutionCreaterException(instantiatingClass, instantiatingClass
            + " could not be found", e);
   }

   private static void wrapExceptionInInitializerError(
         String instantiatingClass, ExceptionInInitializerError e) {
      throwSolutionCreaterException(instantiatingClass,
            "there was an Exception within static initialization code of "
                  + instantiatingClass, e);
   }

   private static void wrapLinkageError(String instantiatingClass,
         LinkageError e) {
      throwSolutionCreaterException(instantiatingClass,
            "there was a problem with another class on which "
                  + instantiatingClass + " depends", e);
   }

   private static void wrapInstantiationException(String instantiatingClass,
         InstantiationException e) {
      throwSolutionCreaterException(
            instantiatingClass,
            instantiatingClass
                  + " could not be instantiated, possibly because it is not a concrete class or does not have a default constructor",
            e);
   }

   private static void wrapIllegalAccessException(String instantiatingClass,
         IllegalAccessException e) {
      throwSolutionCreaterException(instantiatingClass, instantiatingClass
            + " or its default constructor is not accessible", e);
   }

   private static void wrapException(String instantiatingClass, Exception e) {
      throwSolutionCreaterException(instantiatingClass, " the constructor of "
            + instantiatingClass + " probably threw an Exception", e);
   }

   private static void throwSolutionCreaterException(String instantiatingClass,
         String reason, Throwable e) {
      throw new SolutionCreaterException("Cannot instantiatie Class "
            + instantiatingClass + " because " + reason, e);
   }
}
