package org.iastate.ailab.qengine.core.exceptions;

public class SolutionCreaterException extends RuntimeException {
   
   private static final long serialVersionUID = 1;

   //TODO Add an error code
   public SolutionCreaterException(String message) {
      super(message);
   }
   
   public SolutionCreaterException(String message, Throwable e) {
      super(message, e);
   }
}
