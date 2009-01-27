package org.iastate.ailab.qengine.core.util;

import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.reasoners.impl.SchemaMapStore;

public interface SchemaMapLoader {
   /**
    * Add appropriate SchemaMap to all the nodes in the dTree;
    * 
    * @param dTree
    * @return
    */
   public SchemaMapStore addSchemaMap(DataNode dTree);
}
