package org.iastate.ailab.qengine.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.DefaultQueryTransFormer;
import org.iastate.ailab.qengine.core.exceptions.ConfigurationException;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

/*
 * @ author Roshan, Rohit
 * Contains utility methods to read and store ontology map properties file
 */

public class ReadPropFile {


public static final String ONTMAP_PROPERTIES_FILENAME = "ontmap.properties";    
   
public static Hashtable ontMapPropertiesStore = new Hashtable(); 

private static final Logger logger = Logger
.getLogger(DefaultQueryTransFormer.class);


/*
 * Loads the content of ontology map properties file to a hash table
 * @param ontMappropertiesFilePath path of the ontology map properties file  
 */
@SuppressWarnings("unchecked")
public static void loadToHashTableFromOntmapPropertiesFile(String ontMappropertiesFilePath){
   try{
      Set<Map.Entry<Object, Object>> temp = new HashSet();
      Map.Entry<Object, Object> me = null;
      Properties propertiesFile = new Properties();
      //FileInputStream is = (FileInputStream) getClass().getResourceAsStream(ONTMAP_PROPERTIES_FILENAME);
      
     // FileInputStream is = new FileInputStream("C://Users//rose//Documents//workspace_indus//IndusIntegrationFramework//config-example-5//ontmap.properties");
      
      FileInputStream is = new FileInputStream(new File(ontMappropertiesFilePath));
      
      System.out.println("is==null: " + is==null);
      propertiesFile.load(is);
      System.out.println("after load");
      
      temp= propertiesFile.entrySet();
      
      System.out.println("temp==null: " + temp==null);
      
      Iterator it = temp.iterator();
      while(it.hasNext()){
          me = (Entry<Object, Object>) it.next(); 
          System.out.println("me==null: " + me==null);
          ontMapPropertiesStore.put(me.getKey(), me.getValue());
          System.out.println(me.getKey()+" "+ me.getValue());
          
      }
   } catch (FileNotFoundException e) {
      String message = "FileNotFoundException for the ontmap.properties file: ";
   logger.error(message, e);
   throw new ConfigurationException(message, e);
} catch (IOException e) {
   String message = "IOException while trying to load the FileInputStream of the ontmap.properties file: ";
   logger.error(message, e);
   throw new ConfigurationException(message, e);
}
 }

/*
 * Given a key, Returns the path of the ontology file
 * @param key AVH-column to AVH-column mapping e.g., Reference<->Entry
 * @Return Path of the ontology map file   
 */
public static String  getData(String key){
   if(ReadPropFile.ontMapPropertiesStore.containsKey(key))
      return (String) ReadPropFile.ontMapPropertiesStore.get(key);
   else
      return null;
}
}
