package org.iastate.ailab.qengine.core.exceptions;

public class NoMappingException extends MappingException {

   private static final long serialVersionUID = 1L;

   public NoMappingException(String message) {
      super(message);
   }
   
   public NoMappingException(Exception e) {
      super(e);
   }
   
   public NoMappingException(String message, Exception e) {
      super(message, e);
   }
}
