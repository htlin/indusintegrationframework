package org.iastate.ailab.qengine.core.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.iastate.ailab.qengine.core.exceptions.PropertiesException;

public class FileUtils {

   public static Properties loadProperites(String fileName) {
      
      Properties properties = null;
      FileInputStream in = null;
      try {
         in = new FileInputStream(fileName);
         properties = new Properties();

         properties.load(in);
      } catch (FileNotFoundException e) {
         throw new PropertiesException("Cound not find the file " + fileName, e);
      } catch (IOException e) {
         throw new PropertiesException(
               "Could not load the properties from the InputStream " + in, e);
      }
      
      return properties;
   }
}
