package org.iastate.ailab.qengine.core.exceptions;

public class TranslationException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   private int errorCode;

   public TranslationException(String message, int errorCode) {
      super(message);
      this.errorCode = errorCode;
   }

   public TranslationException(String message, Throwable cause) {
      super(message, cause);
   }

   public int getErrorCode() {
      return errorCode;
   }
}