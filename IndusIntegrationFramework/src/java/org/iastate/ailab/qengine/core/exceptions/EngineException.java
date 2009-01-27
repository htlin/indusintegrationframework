package org.iastate.ailab.qengine.core.exceptions;

public class EngineException extends RuntimeException {
   
   private static final long serialVersionUID = 123452;

   //TODO Add an error code
   public EngineException(String message) {
      super(message);
   }
   
   public EngineException(Exception e) {
      super(e);
   }
   
   public EngineException(String message, Exception e) {
      super(message, e);
   }
}
