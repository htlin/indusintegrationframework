package org.iastate.ailab.qengine.core.reasoners.impl;

import java.net.URI;
import java.net.URISyntaxException;

import org.iastate.ailab.qengine.core.reasoners.interfaces.DataContent;

/*
 * TODO unused classes - These two interface/class pairs are not used
 * outside of each other: (DataContentResource, DataContentResourceImpl)
 * and (DataContent, DataContentImpl).
 */
public class DataContentImpl implements DataContent {

   //pr="http://www.ailab.iastate.edu/Ont/Weather
   //pr:Sunny should mean http://www.ailab.iastate.edu/Ont/Weather/Sunny

   //<===============   IMPORTANT    REFACTOR    ===>
   //TODO: Refactor so that you just store the classId and get prefix and localValue from it
   //This will save space for huge ontologies
   //<===============  IMPORTANT REFACTOR      ====>

   private String dataContentValue; // e.g Sunny

   private String prefixURI = null; //e.g. http://www.ailab.iastate.edu/Ont/Weather/

   private URI classId;

   public DataContentImpl(String localName) {
      dataContentValue = localName;
   }

   /**
    * @param localName
    * @param prefix if this is null, it is assumed to be indus:
    */
   public DataContentImpl(String localName, String prefix) {
      dataContentValue = localName;
      prefixURI = (prefix != null) ? prefix : "indus:"; //make indus: as default prefix
      try {
         classId = URIUtils.createURI(dataContentValue, prefixURI);
      } catch (URISyntaxException e) {
         throw new RuntimeException(
               "URISyntaxException while trying to create a URI from the data content value "
                     + dataContentValue + " and the URI prefix "
                     + prefixURI, e);
      }
   }

   public DataContentImpl(URI classId) {
      this.classId = classId;
      //refactoring necessary
   }

   public String getQualifier() {
      return prefixURI;
   }

   public String getLocalValue() {
      String value = classId.toString();
      int index;
      if (classId.isOpaque()) {
         //it is opaque, of the form GO:2345 
         index = value.lastIndexOf(":");
         return value.substring(index + 1);
      } else {
         //it is hierarchical
         index = value.lastIndexOf("/");
         return value.substring(index + 1);
      }
   }

   public java.net.URI getQualifiedValue() {
      //TODO: Refactor: Do we need this
      return classId;
   }

   //TODO this method is never called, is it needed?
   public String getQualifiedValue(boolean throwExceptionIfUnqualified) {
      if (prefixURI.isEmpty()) {
         throw new RuntimeException(dataContentValue + " is not qualified");
      }
      
      return prefixURI + dataContentValue;
   }
}
