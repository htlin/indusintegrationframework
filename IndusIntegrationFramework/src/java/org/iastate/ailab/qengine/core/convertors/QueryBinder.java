package org.iastate.ailab.qengine.core.convertors;

import Zql.ZQuery;

public interface QueryBinder {
   public ZQuery bind(ZQuery query, String dataSource);
}
