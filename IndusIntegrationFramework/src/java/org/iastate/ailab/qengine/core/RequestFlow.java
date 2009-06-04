package org.iastate.ailab.qengine.core;

import org.iastate.ailab.qengine.core.datasource.DataNode;

public interface RequestFlow {
   public void execute(DataNode node, Object nodeQuery) throws Exception;
}
