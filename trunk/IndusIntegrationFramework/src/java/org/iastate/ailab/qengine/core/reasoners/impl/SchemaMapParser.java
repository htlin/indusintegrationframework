package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface SchemaMapParser<E extends SchemaMapStore> {
   /**
    * Parses the source and stores it into the SchemaMap store
    * 
    * @param source
    * @param inline if true, the source contains the string to be parsed
    * @param store
    * @throws FileNotFoundException
    * @throws IOException
    */
   public void parse(String source, boolean inline, E store)
         throws FileNotFoundException, IOException;
}
