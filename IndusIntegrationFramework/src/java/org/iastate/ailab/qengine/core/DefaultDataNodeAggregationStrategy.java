package org.iastate.ailab.qengine.core;

import java.util.HashMap;
import java.util.Map;

import org.iastate.ailab.qengine.core.datasource.DataNode.LevelFragmentationType;

public class DefaultDataNodeAggregationStrategy {
   // TODO FUTURE USE: HERE TO CAPTURE IDEA HOW COMPLEX MAPPINGS MAY OCCUR
   public enum RELATION_SHIP {
      EQUAL_TO, LESS_THAN, GREATER_THAN, LESS_THAN_EQUAL_TO, GREATER_THAN_EQUAL_TO,

      // below used to combine atomic relationships (FUTURE)
      AND, OR
      // e.g MaleAdult => rel1 AND rel2 where
      // rell = Sex EQUAL_TO Male
      // rel2 = Age GREATER_THAN 17
   }

   public class JoinColumn {
      public String columnName;

      public String tableName;

      public String columnType;

      public JoinColumn(String tableName, String columnName, String columnType) {
         this.columnName = columnName;
         this.tableName = tableName;
         this.columnType = columnType;
      }
   }

   // use to store the fragmentation type of children
   LevelFragmentationType childLevelFragmentationType;

   // describes what columns the children are to be joined on.
   // will make sense only if LevelFragmentationType of CHILDREn is vertical
   Map<String, JoinColumn> childJoinColumns = new HashMap<String, JoinColumn>();

   public LevelFragmentationType getChildLevelFragmentationType() {
      return childLevelFragmentationType;
   }

   public void setChildLevelFragmentationType(
         LevelFragmentationType fragmentationType) {
      // whether it is horizontal and vertical
      this.childLevelFragmentationType = fragmentationType;
   }

   public void setChildJoinColumn(String childName, JoinColumn joinColumn) {
      childJoinColumns.put(childName, joinColumn);
   }

   public JoinColumn getChildJoinColumn(String childName) {
      return childJoinColumns.get(childName);
   }
}
