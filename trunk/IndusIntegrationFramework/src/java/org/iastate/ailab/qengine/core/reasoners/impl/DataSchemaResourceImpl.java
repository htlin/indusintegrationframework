package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.iastate.ailab.qengine.core.reasoners.interfaces.DataSchemaResource;

public class DataSchemaResourceImpl implements DataSchemaResource {

   private String dataSourceName;

   private String tableName;

   private String attributeName;

   public DataSchemaResourceImpl(String ds, String table, String column) {
      this.dataSourceName = ds;
      this.tableName = table;
      this.attributeName = column;
   }

   public String getDataSourceName() {
      return this.dataSourceName;
   }

   public String getTableName() {
      return this.tableName;
   }

   public String getAttributeName() {
      return this.attributeName;
   }

   public void setAttributeName(String attributeName) {
      this.attributeName = attributeName;
   }

   public void setDataSourceName(String dataSourceName) {
      this.dataSourceName = dataSourceName;
   }

   public void setTableName(String tableName) {
      this.tableName = tableName;
   }

   @Override
   public void display(OutputStream out) {
      display(new PrintWriter(out));
   }

   @Override
   public String toString() {
      StringWriter stringWriter = new StringWriter();
      display(stringWriter);
      return stringWriter.toString();
   }

   @Override
   public void display(Writer writer) {
      display(new PrintWriter(writer));
   }

   @Override
   public void display(PrintWriter printWriter) {
      printWriter.println(dataSourceName + "." + tableName + "."
            + attributeName);
   }
}
