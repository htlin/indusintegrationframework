package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface OntologyParser {

   /**
    * Parses the source and stores it in ontology store
    * 
    * @param source
    * @param inline if true, the source contains the string to be parsed
    * @param store
    * @throws IOException
    * @throws FileNotFoundException
    */
   public void parse(String source, boolean inline, OntologyStore store);

   public String getSeparator();
}
