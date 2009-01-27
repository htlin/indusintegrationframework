package org.iastate.ailab.qengine.core.exceptions;

public class PropertiesException extends ConfigurationException {

   private static final long serialVersionUID = 1L;

   public PropertiesException(String message) {
      super(message);
   }
   
   public PropertiesException(Exception e) {
      super(e);
   }
   
   public PropertiesException(String message, Exception e) {
      super(message, e);
   }
}
