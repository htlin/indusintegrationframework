package org.iastate.ailab.qengine.core;

import java.util.Vector;

public class UserViewMetaData {
   private int dataSourceCount;

   private Vector<Attribute> attributeList;

   private String tableName;

   public void setDataSourceCount(int count) {
      this.dataSourceCount = count;
   }

   public int getDataSourceCount() {
      return dataSourceCount;
   }

   public void setAttributeList(Vector<Attribute> attrList) {
      this.attributeList = attrList;
   }

   public Vector<Attribute> getAttributeList() {
      return attributeList;
   }

   public void setUserViewTable(String tableName) {
      this.tableName = tableName;
   }

   public String getUserViewTable() {
      return tableName;
   }

   public class Attribute {
      public String attributeName;

      public String attributeType;

      public Vector<String> attributeValues;

      public Attribute(String attrName, String attrType,
            Vector<String> attrValues) {
         this.attributeName = attrName;
         this.attributeType = attrType;
         this.attributeValues = attrValues;
      }
   }
}
