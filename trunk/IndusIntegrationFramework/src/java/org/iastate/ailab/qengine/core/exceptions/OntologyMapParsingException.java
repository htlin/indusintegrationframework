package org.iastate.ailab.qengine.core.exceptions;

public class OntologyMapParsingException extends MappingException {

   private static final long serialVersionUID = 1L;

   public OntologyMapParsingException(String message) {
      super(message);
   }
   
   public OntologyMapParsingException(Exception e) {
      super(e);
   }
   
   public OntologyMapParsingException(String message, Exception e) {
      super(message, e);
   }
}
