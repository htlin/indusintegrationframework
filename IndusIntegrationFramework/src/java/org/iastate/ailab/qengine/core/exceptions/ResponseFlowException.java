package org.iastate.ailab.qengine.core.exceptions;

public class ResponseFlowException extends MappingException {

   private static final long serialVersionUID = 1L;

   public ResponseFlowException(String message) {
      super(message);
   }
   
   public ResponseFlowException(Exception e) {
      super(e);
   }
   
   public ResponseFlowException(String message, Exception e) {
      super(message, e);
   }
}
