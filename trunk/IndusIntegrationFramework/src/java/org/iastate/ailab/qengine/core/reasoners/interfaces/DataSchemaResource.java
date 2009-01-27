package org.iastate.ailab.qengine.core.reasoners.interfaces;

import org.iastate.ailab.qengine.core.util.Displayable;

public interface DataSchemaResource extends Displayable {

   public String getDataSourceName();

   public String getTableName();

   public String getAttributeName();

   public String toString();
}
