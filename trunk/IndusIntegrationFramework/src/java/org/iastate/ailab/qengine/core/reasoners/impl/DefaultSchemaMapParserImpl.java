package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.util.ExpressionEvaluator;

/**
 * @author neeraj
 */
public class DefaultSchemaMapParserImpl implements
      SchemaMapParser<DefaultSchemaMapStoreImpl> {

   private static final Logger logger = Logger
         .getLogger(DefaultSchemaMapParserImpl.class);

   /*
    * (non-Javadoc)
    * 
    * @see org.iastate.ailab.qengine.core.reasoners.impl.SchemaMapParser#parse(java.lang.String,
    * boolean,
    * org.iastate.ailab.qengine.core.reasoners.impl.SchemaMapStore)
    */
   //Keep track of files you parsed so that you don't do it again
   private final Set<String> filesParsed = new HashSet<String>();

   // Stores the user defined schemaMap as property
   // format: DS1.DS1_Table.id=D.EMPLOYEETABLE.key

   Properties maps = new Properties();

   public void parse(String source, boolean inline,
         DefaultSchemaMapStoreImpl store) throws FileNotFoundException,
         IOException {
      // We are handling the Schema Map in the following format
      // DataSource --> userView
      // It is from DataSource to userView as it helps to read as properties keeping keys unique
      // #DS1
      // DS1.DS1_Table.id=D.EMPLOYEETABLE.key
      // DS1.DS1_Table.status=DDS1.DS1_Table.compensation=DS1_DS2_DS3.EMPLOYEETABLE.benefits:ToDSViewExp@9/5(x)+32,ToUViewExp@(x-32)*5/9.EMPLOYEETABLE.position
      // 
      // #DS2
      // DS2.DS2_Table.ssn=D.EMPLOYEETABLE.key
      // DS2.DS2_Table.type=D.EMPLOYEETABLE.position

      FileInputStream in = null;
      //TODO reader is never actually used
      BufferedReader reader = null;

      try {
         if (inline == false) {
            //the source points to a file where the schemaMap exists

            if (filesParsed.contains(source)) {
               return; //already parsed it, no need to do it
               //TODO: Handle Timestamps so that you can do dynamic loading
            }
            in = new FileInputStream(source);
         } else {
            //inline = true => source itself contains the schemaMap
            reader = new BufferedReader(new StringReader(source));
            in = new FileInputStream(source);
            //reader = new BufferedReader(new InputStreamReader(in));
         }
         maps.load(in);

         Iterator<Object> it = maps.keySet().iterator();
         while (it.hasNext()) {
            String key = (String) it.next();
            String value = (String) maps.get(key);
            ParseAndStore(key, value, store);
         }
      } finally {
         try {
            if (reader != null) {
               reader.close();
            }
         } catch (IOException e) {
            logger.error("IOException closing the Reader: " + reader, e);
         }
         try {
            if (in != null) {
               in.close();
            }
         } catch (IOException e) {
            logger.error("IOException closing the InputStream: " + in, e);
         }
      }
   }

   private void ParseAndStore(String key, String value,
         DefaultSchemaMapStoreImpl store) {

      //DS1.DS1_Table.id=D.EMPLOYEETABLE.key 
      //DataSource --> to userView  
      //name=value,exp1,exp2
      String[] dataSourceValues = key.split("\\.");

      String[] viewValuesWithExpressions = value.split(",");
      String viewSchemaAttribute = viewValuesWithExpressions[0]; //associated attribute in userview

      //there may be  associated conversion functions as name=value,exp1,exp2
      boolean hasConversionFunction = false;
      String exp2DSView = null;
      String exp2UView = null;

      //Some Info back to user for debugging
      switch (viewValuesWithExpressions.length) {
      case 0:
         //should not happen
         logger.debug("No SchemaMapping for  " + viewSchemaAttribute
               + " to dataSource " + dataSourceValues[0]);
         break;
      case 1:
         logger.trace("No Conversion Function for " + viewSchemaAttribute
               + " to dataSource " + dataSourceValues[0]);
         break;
      case 2:
         logger.error("Check SchemaMap :Only one Conversion Function for "
               + viewSchemaAttribute + " to dataSource " + dataSourceValues[0]);
         break;
      case 3:
         //expected...handled below
         break;
      default:
         logger.debug("Extra characters: Check SchemaMap for "
               + viewSchemaAttribute + " to dataSource " + dataSourceValues[0]);

      }

      if (viewValuesWithExpressions.length > 1) {

         //the associated values are related by expressions
         exp2DSView = viewValuesWithExpressions[1]; //expression to convert values in userView to dataSource View
         exp2UView = viewValuesWithExpressions[2];
         hasConversionFunction = true;
      }

      String[] viewValues = viewSchemaAttribute.split("\\.");

      DataSchemaResourceImpl dataSourceAttribute = new DataSchemaResourceImpl(
            dataSourceValues[0], dataSourceValues[1], dataSourceValues[2]);

      DataSchemaResourceImpl userAttribute = new DataSchemaResourceImpl(
            viewValues[0], viewValues[1], viewValues[2]);

      store.addUserViewToDataSourceMap(userAttribute, dataSourceAttribute);

      if (hasConversionFunction) {

         store.dataSourceAttributeHasConversionFunction(dataSourceAttribute);

         String targetDataSource = dataSourceValues[0];
         store.UViewAttributeHasConversionFunctionToDataSource(userAttribute,
               targetDataSource);

         ExpressionEvaluator eval2DSView = new ExpressionEvaluator(exp2DSView);
         store.addConversionFunctionToDataSource(userAttribute,
               targetDataSource, eval2DSView);

         ExpressionEvaluator eval2UserView = new ExpressionEvaluator(exp2UView);
         store.addConversionFunctionToUserView(dataSourceAttribute,
               eval2UserView);

      }
   }
}
