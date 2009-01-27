package org.iastate.ailab.qengine.core.exceptions;

// TODO: Refactor to some general exception class
public class RewritingException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   private int errorCode;

   RewritingException(String message, int errorCode) {
      super(message);
      this.errorCode = errorCode;
   }
   
   public int getErrorCode() {
      return errorCode;
   }
}