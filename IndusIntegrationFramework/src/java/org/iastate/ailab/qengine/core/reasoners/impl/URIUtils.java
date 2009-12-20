package org.iastate.ailab.qengine.core.reasoners.impl;

import java.net.URI;
import java.net.URISyntaxException;

import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;

public class URIUtils {
   public static URI createURI(String localName, String prefix)
         throws URISyntaxException {

      if (localName.endsWith("#") || localName.endsWith("/")
            || localName.endsWith(":")) {
         return new URI(prefix + localName);
      } else {
         return new URI(prefix
               + SolutionCreator.getOntologyParserImpl().getSeparator()
               + localName);
      }
   }

   public static URI createURIForUseByPellet(String localName, String prefix)
         throws URISyntaxException {
      return new URI(prefix + "#" + localName);
   }

   public static URI createURI(String uriString) throws URISyntaxException {
      return new URI(uriString);
   }
}
