package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.exceptions.NoMappingException;
import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;
import org.iastate.ailab.qengine.core.reasoners.interfaces.DataSchemaResource;
import org.iastate.ailab.qengine.core.util.ExpressionEvaluator;

/**
 * @author neeraj
 */
public class DefaultSchemaMapStoreImpl implements SchemaMapStore {

   private static final Logger logger = Logger
         .getLogger(DefaultSchemaMapStoreImpl.class);

   private SchemaMapParser<DefaultSchemaMapStoreImpl> parser = null; //initialized in constructor

//   private Map<DataSchemaResource, DataSchemaResource> dataSourceToUserViewMap = new HashMap<DataSchemaResource, DataSchemaResource>();

   //Maps from dataSource to UserView. 
   //It is indexed by String equivalent of the dataSources DataSchemaResource(i.e DataSourceName.TableName.attributeName)  so as not to have to overite the hashFunction
   private final Map<String, DataSchemaResource> dataSourceToUserViewMap = new HashMap<String, DataSchemaResource>();

   /*
    * Stored a map from a userView the DataSourceView. It is indexed by the
    * name of the dataSource Given a dataSource a HashMap that corresponds
    * to the map for this particular data source is returned The HashMap
    * for the particular datasource is indexed by string representation of
    * column in the userView(Reason as above)
    */
   private final Map<String, Map<String, DataSchemaResource>> userViewToDataSourceMap = new HashMap<String, Map<String, DataSchemaResource>>();

   //keep track of all the tables in a particular DataSource. It is indexed by name of DataSource (In Indus 2.0 should be one)
   private final Map<String, Set<String>> allDataSourceTableNames = new HashMap<String, Set<String>>();

   //keep track of which attributes in datasources  have a conversion function. 

   private final HashSet<DataSchemaResource> dSAttributeHasConversionFunction = new HashSet<DataSchemaResource>();

   /*
    * keep track of attributes in the user view that have a conversion
    * function to a particular datasource. Applicable per datasources
    * because same attribute for user view may have conversion for one
    * datasource and not for other
    */
   private final HashMap<String, HashSet<DataSchemaResource>> uVAttributeHasConversionFunction = new HashMap<String, HashSet<DataSchemaResource>>();

   //returns a conversion function for converting a value for the DataSchemaResource to the userView
   private final HashMap<DataSchemaResource, ExpressionEvaluator> conversionFunctionsToUserView = new HashMap<DataSchemaResource, ExpressionEvaluator>();

   // A Map for conversion functions from userView to DataSource. It is indexed by the dataSource name
   private final HashMap<String, HashMap<DataSchemaResource, ExpressionEvaluator>> conversionFunctionsToDataSource = new HashMap<String, HashMap<DataSchemaResource, ExpressionEvaluator>>();

   public DefaultSchemaMapStoreImpl() {
      parser = (SchemaMapParser<DefaultSchemaMapStoreImpl>) SolutionCreator
            .getSchemaMapParserImpl();
   }

   /**
    * Adds a given mapping to the store
    * 
    * @param userViewAttribute
    * @param mappedTo
    */
   public void addUserViewToDataSourceMap(DataSchemaResource userViewAttribute,
         DataSchemaResource mappedTo) {

      Map<String, DataSchemaResource> schemaMap;
      String dataSourceName = mappedTo.getDataSourceName();

      schemaMap = userViewToDataSourceMap.get(dataSourceName);
      if (schemaMap == null) {
         //first time a mapping for this dataSource is being seen
         schemaMap = new HashMap<String, DataSchemaResource>();
      }

      schemaMap.put(userViewAttribute.toString(), mappedTo); //note key is string as we want to get it without having to make sure the objects are equal

      userViewToDataSourceMap.put(dataSourceName, schemaMap);

      //Now also add the map the allows you to go from a dataSource to userView

      dataSourceToUserViewMap.put(mappedTo.toString(), userViewAttribute);

      //keep track of all the tables in a apsrticular data source
      //use in utility function getUserViewColumnName
      String currDataSourceName = mappedTo.getDataSourceName();

      //get tables for the current DataSource
      Set<String> tableNames = allDataSourceTableNames.get(currDataSourceName);
      if (tableNames == null) {
         //first table being added
         tableNames = new HashSet<String>();
      }
      tableNames.add(mappedTo.getTableName());
      allDataSourceTableNames.put(currDataSourceName, tableNames);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.iastate.ailab.qengine.core.reasoners.impl.SchemaMapStore#getMappedToDataSourceDataSchemaResource(org.iastate.ailab.qengine.core.reasoners.interfaces.DataSchemaResource,
    * java.lang.String)
    */
   public DataSchemaResource getMappedToDataSourceDataSchemaResource(
         DataSchemaResource userViewDataSchemaResource, String targetDataSource) {

      Map<String, DataSchemaResource> map = userViewToDataSourceMap
            .get(targetDataSource);

      if (map == null) {
         String message = "No mapping from the user view to the data source "
               + targetDataSource + " exists."
               + " Was looking to map attribute " + userViewDataSchemaResource;
         logger.warn(message);
         throw new NoMappingException(message);
      }

      DataSchemaResource result = map.get(userViewDataSchemaResource);

      if (result == null) {
         String message = "No mapping from the user view attribute "
               + userViewDataSchemaResource + " to the data source "
               + targetDataSource;
         logger.warn(message);
         throw new NoMappingException(message);
      }
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.iastate.ailab.qengine.core.reasoners.impl.SchemaMapStore#getMappedToUserViewDataSchemaResource(org.iastate.ailab.qengine.core.reasoners.interfaces.DataSchemaResource)
    */
   public DataSchemaResource getMappedToUserViewDataSchemaResource(
         DataSchemaResource dataSourceDataSchemaResource) {

      DataSchemaResource result = dataSourceToUserViewMap
            .get(dataSourceDataSchemaResource.toString()); //note have to use toString to get it

      if (result == null) {
         String message = "A mapping to the userView does not exist for: "
               + dataSourceDataSchemaResource;
         logger.warn(message);
         throw new IllegalStateException(message);
      }
      return result;
   }

   /**
    * For the given dataSource and columName returns the corresponding
    * DataSchemaResource in user View It does this by looking through all
    * the tables in the datasource Returns null if none found
    * 
    * @param columnName
    * @param dataSourceName
    * @return
    */
   public DataSchemaResource getUserViewColumnName(String columnName,
         String dataSourceName) {
      DataSchemaResource inp = null;
      DataSchemaResource result = null;
      boolean found = false;
      Set<String> currDataSourceTableNames = allDataSourceTableNames
            .get(dataSourceName);
      java.util.Iterator<String> it = currDataSourceTableNames.iterator();
      if (it == null)
         return null;
      while (it.hasNext() && !found) {
         inp = new DataSchemaResourceImpl(dataSourceName, it.next(), columnName);
         result = getMappedToUserViewDataSchemaResource(inp);
         if (result != null)
            found = true;
      }

      return result;
   }

   /**
    * Describe if a particular data source attribute (DataSchemaResource)
    * has a conversion function which converts value to the user view. A
    * use case of this is when the attribute is stored in different units
    * in DSView and UserView and hence need for conversion
    * 
    * @param attribute
    * @return
    */
   public boolean dataSourceAttributeHasConversionFunction(
         DataSchemaResource attribute) {
      return dSAttributeHasConversionFunction.contains(attribute);
   }

   /**
    * Denote that the attribute has a ConversionFunction
    * 
    * @param attribute
    */
   public void addDSAttributeHasConversioFunction(DataSchemaResource attribute) {
      dSAttributeHasConversionFunction.add(attribute);
   }

   /**
    * Describes if a particular user view attribute has a conversion
    * function to the mapped attribute in the targetDataSource
    * 
    * @param userViewAttribute
    * @param targetDataSource
    * @return
    */
   public boolean UViewAttributeHasConversionFunctionToDataSource(
         DataSchemaResource userViewAttribute, String targetDataSource) {

      boolean contains = false;
      HashSet<DataSchemaResource> cFunctions = uVAttributeHasConversionFunction
            .get(userViewAttribute);
      if (cFunctions != null && cFunctions.contains(userViewAttribute)) {
         contains = true;
      }
      return contains;
   }

   /**
    * Conversion function to convert a data from userView into data for the
    * targetDataSource
    * 
    * @param userViewDataSchemaResource
    * @param targetDataSource
    * @return
    */
   public ExpressionEvaluator conversionFunctionToDataSource(
         DataSchemaResource userViewDataSchemaResource, String targetDataSource) {
      ExpressionEvaluator eval = null;
      try {

         eval = conversionFunctionsToDataSource.get(targetDataSource).get(
               userViewDataSchemaResource);

      } catch (Exception e) {
         throw new org.iastate.ailab.qengine.core.exceptions.ConfigurationException(
               "No Conversion Function from(uView)  "
                     + userViewDataSchemaResource.toString()
                     + " to datasource " + targetDataSource + " ", e);
      }
      return eval;
   }

   public void addConversionFunctionToDataSource(
         DataSchemaResource userViewDataSchemaResource,
         String targetDataSource, ExpressionEvaluator eval) {

      HashMap<DataSchemaResource, ExpressionEvaluator> dSConversionFunctions = conversionFunctionsToDataSource
            .get(targetDataSource);

      if (dSConversionFunctions == null) {

         //first time for this datasource. Allocate memory
         dSConversionFunctions = new HashMap<DataSchemaResource, ExpressionEvaluator>();
      }
      //add entry
      dSConversionFunctions.put(userViewDataSchemaResource, eval);

      //store 
      conversionFunctionsToDataSource.put(targetDataSource,
            dSConversionFunctions);
      logger.debug("Added ConversionFunctionToDataSource  for "
            + userViewDataSchemaResource.toString() + "to  " + targetDataSource
            + " :" + eval.printExpression());
   }

   /**
    * Conversion Function to convert a data from datasource view to user
    * view.
    * 
    * @param dataSourceDataSchemaResource
    * @return
    */
   public ExpressionEvaluator conversionFunctionToUserView(
         DataSchemaResource dataSourceDataSchemaResource) {
      ExpressionEvaluator eval = null;
      try {
         eval = conversionFunctionsToUserView.get(dataSourceDataSchemaResource);
      } catch (Exception e) {
         throw new org.iastate.ailab.qengine.core.exceptions.ConfigurationException(
               "No Conversion Function from  "
                     + dataSourceDataSchemaResource.toString() + "  userView ",
               e);
      }
      return eval;
   }

   /**
    * Add a conversionFunction to convert data associated with the
    * dataSource DataSchemaResource to userView
    * 
    * @param dataSourceDataSchemaResource
    * @param expEvalutor
    */
   public void addConversionFunctionToUserView(
         DataSchemaResource dataSourceDataSchemaResource,
         ExpressionEvaluator expEvalutor) {
      conversionFunctionsToUserView.put(dataSourceDataSchemaResource,
            expEvalutor);

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.iastate.ailab.qengine.core.reasoners.impl.SchemaMapStore#parse(java.lang.String,
    * boolean)
    */
   public void parse(String source, boolean inline)
         throws FileNotFoundException, IOException {
      parser.parse(source, inline, this);
   }
}
