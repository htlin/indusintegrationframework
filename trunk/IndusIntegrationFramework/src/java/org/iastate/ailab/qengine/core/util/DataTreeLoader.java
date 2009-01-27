package org.iastate.ailab.qengine.core.util;

import org.iastate.ailab.qengine.core.datasource.DataNode;

public interface DataTreeLoader {

   public DataNode getDataTree();

   /**
    * Sets a name value pair with the DataTree Loader. This may be used to
    * configure the DataTreeLoader
    * 
    * @param name
    * @param value
    */
   public void setProperty(String name, String value);

   /**
    * @param name
    * @return
    */
   public String getProperty(String name);
}
