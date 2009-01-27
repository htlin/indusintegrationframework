package org.iastate.ailab.qengine.core.reasoners.interfaces;

import java.net.URISyntaxException;

/*
 * TODO unused classes - These two interface/class pairs are not used
 * outside of each other: (DataContentResource, DataContentResourceImpl)
 * and (DataContent, DataContentImpl).
 */
public interface DataContent {

   /**
    * 
    * @return a String which represent the Value Associated with the
    * DataContent
    * 
    */
   public String getLocalValue();

   /**
    * @return the fully qualified Value Associated with the DataConetnt
    */
   public java.net.URI getQualifiedValue() throws URISyntaxException;
}
