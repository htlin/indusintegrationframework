package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface OntologyMapParser {

   /**
    * Parses the source and stores it in OntologyMapStore
    * 
    * @param source
    * @param inline if true, the source contains the string to be parsed
    * @param store
    * @throws FileNotFoundException
    * @throws IOException
    */
   public void parse(String source, boolean inline, OntologyMapStore store)
         throws FileNotFoundException, IOException;
}
