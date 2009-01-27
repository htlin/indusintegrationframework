package org.iastate.ailab.qengine.core.exceptions;

public class FactoryConfigurationException extends Exception {
   
   private static final long serialVersionUID = 123451;

   //TODO Add an error code
   public FactoryConfigurationException(String message) {
      super(message);
   }
}
