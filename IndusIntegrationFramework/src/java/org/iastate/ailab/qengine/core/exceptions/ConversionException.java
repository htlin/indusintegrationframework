package org.iastate.ailab.qengine.core.exceptions;

public class ConversionException extends RuntimeException {

   public ConversionException(String message) {
      super(message);
   }

   public ConversionException(Exception e) {
      super(e);
   }

   public ConversionException(String message, Exception e) {
      super(message, e);
   }

}
