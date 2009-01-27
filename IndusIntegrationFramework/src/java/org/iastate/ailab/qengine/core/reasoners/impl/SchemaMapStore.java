package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.iastate.ailab.qengine.core.reasoners.interfaces.DataSchemaResource;
import org.iastate.ailab.qengine.core.util.ExpressionEvaluator;

public interface SchemaMapStore {
   /**
    * This takes an attribute in the userview and returns the corresponding
    * attribute in targetDataSource
    * 
    * @param userViewDataSchemaResource the attribute in the userView
    * @param targetDataSource the data source in which to find the mapped
    * attribute name
    */
   public DataSchemaResource getMappedToDataSourceDataSchemaResource(
         DataSchemaResource userViewDataSchemaResource, String targetDataSource);

   /**
    * It takes as input a DataSchemaResource(i.e. an Attribute in some Data
    * Source) and returns the mapped attribute in the userView
    * 
    * @param dataSourceDataSchemaResource
    */
   public DataSchemaResource getMappedToUserViewDataSchemaResource(
         DataSchemaResource dataSourceDataSchemaResource);

   /**
    * Describe if a particular data source attribute (DataSchemaResource)
    * has a conversion function which converts it values to user view. A
    * use case of this is when the attribute is stored in different units
    * in DSView and UserView and hence need for conversion
    * 
    * @param attribute
    * @return
    */
   public boolean dataSourceAttributeHasConversionFunction(
         DataSchemaResource attribute);

   /**
    * Describes if a particular user view attribute has a conversion
    * function to the mapped attribute in the targetDataSource
    * 
    * @param userViewAttribute
    * @param targetDataSource
    * @return
    */
   public boolean UViewAttributeHasConversionFunctionToDataSource(
         DataSchemaResource userViewAttribute, String targetDataSource);

   /**
    * Conversion function to convert a data from userView into data for the
    * targetDataSource
    * 
    * @param userViewDataSchemaResource
    * @param targetDataSource
    * @return
    */
   public ExpressionEvaluator conversionFunctionToDataSource(
         DataSchemaResource userViewDataSchemaResource, String targetDataSource);

   /**
    * Conversion Function to convert a data from datasource view to user
    * view
    * 
    * @param dataSourceDataSchemaResource
    * 
    * @return
    */
   public ExpressionEvaluator conversionFunctionToUserView(
         DataSchemaResource dataSourceDataSchemaResource);

   public void parse(String source, boolean inline)
         throws FileNotFoundException, IOException;
}
