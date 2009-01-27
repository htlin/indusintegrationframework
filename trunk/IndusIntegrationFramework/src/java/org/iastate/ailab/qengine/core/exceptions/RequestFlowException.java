package org.iastate.ailab.qengine.core.exceptions;

public class RequestFlowException extends MappingException {

   private static final long serialVersionUID = 1L;

   public RequestFlowException(String message) {
      super(message);
   }
   
   public RequestFlowException(Exception e) {
      super(e);
   }
   
   public RequestFlowException(String message, Exception e) {
      super(message, e);
   }
}
