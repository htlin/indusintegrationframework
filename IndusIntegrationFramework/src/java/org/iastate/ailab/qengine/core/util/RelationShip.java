package org.iastate.ailab.qengine.core.util;

public class RelationShip {

   public static String EQUAL_TO = "=";

   public static String LESS_THAN = "<";

   public static String GREATER_THAN = ">";

   public static String NOT_EQUAL_TO = "!=";

   public static String NOT_DEFINED = "NOT_DEFINED";

   private String relationShip = NOT_DEFINED;

   public String getRelationShip() {
      return relationShip;
   }

   RelationShip(String relationShip) {
      this.relationShip = relationShip;
   }

   public void setRelationShip(String relationShip) {
      this.relationShip = relationShip;
   }
}
