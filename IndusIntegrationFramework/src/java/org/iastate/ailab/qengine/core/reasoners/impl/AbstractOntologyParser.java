package org.iastate.ailab.qengine.core.reasoners.impl;

public abstract class AbstractOntologyParser implements OntologyParser {

   private String separator = initSeparator();

   @Override
   public String getSeparator() {
      return separator;
   }

   protected abstract String initSeparator();
}
