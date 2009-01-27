package org.iastate.ailab.qengine.core.exceptions;

public class MappingException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   public MappingException(String message) {
      super(message);
   }
   
   public MappingException(Exception e) {
      super(e);
   }
   
   public MappingException(String message, Exception e) {
      super(message, e);
   }
}
