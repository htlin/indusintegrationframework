package org.iastate.ailab.qengine.core.exceptions;

public class InvalidMappingException extends MappingException {

   private static final long serialVersionUID = 1L;

   public InvalidMappingException(String message) {
      super(message);
   }
   
   public InvalidMappingException(Exception e) {
      super(e);
   }
   
   public InvalidMappingException(String message, Exception e) {
      super(message, e);
   }
}
