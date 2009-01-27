package org.iastate.ailab.qengine.core.exceptions;

public class ConfigurationException extends RuntimeException {

   private static final long serialVersionUID = 123452;

   //TODO Add an error code
   public ConfigurationException(String message) {
      super(message);
   }
   
   public ConfigurationException(String message, Throwable cause) {
      super(message, cause);
   }
   
   public ConfigurationException(Exception e) {
      super(e);
   }
}
