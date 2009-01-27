package org.iastate.ailab.qengine.core.exceptions;

public class OntologyParsingException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   public OntologyParsingException(String message) {
      super(message);
   }
   
   public OntologyParsingException(Exception e) {
      super(e);
   }
   
   public OntologyParsingException(String message, Exception e) {
      super(message, e);
   }
}
