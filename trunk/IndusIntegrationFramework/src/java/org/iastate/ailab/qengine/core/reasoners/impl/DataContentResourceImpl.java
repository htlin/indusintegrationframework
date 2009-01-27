package org.iastate.ailab.qengine.core.reasoners.impl;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.reasoners.interfaces.DataContent;
import org.iastate.ailab.qengine.core.reasoners.interfaces.DataContentResource;
import org.iastate.ailab.qengine.core.reasoners.interfaces.DataSchemaResource;
import java.io.PrintStream;

/*
 * TODO unused classes - These two interface/class pairs are not used
 * outside of each other: (DataContentResource, DataContentResourceImpl)
 * and (DataContent, DataContentImpl).
 */
public class DataContentResourceImpl implements DataContentResource {

   private static final Logger logger = Logger
         .getLogger(DataContentResourceImpl.class);

   private DataSchemaResource theResource; // Identifies the Attribute

   private DataContent value;

   public DataSchemaResource getDataSchemaResource() {
      return theResource;
   }

   public DataContent getValue() {
      return value;
   }

   public void setValue(DataContent v) {
      this.value = v;
   }

   public void setDataSchemaResource(DataSchemaResource r) {
      this.theResource = r;
   }

   public void print(PrintStream out) {
      try {
         if (theResource != null) {
            out.print(theResource.getDataSourceName() + "."
                  + theResource.getTableName() + "."
                  + theResource.getAttributeName() + "=");
         }

         out.println(value.getQualifiedValue());
      } catch (Exception e) {
         logger.error("Exception while trying to print this DataContentResourceImpl", e);
      }
   }
}
