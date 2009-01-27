package org.iastate.ailab.qengine.core;

import org.iastate.ailab.qengine.core.DefaultPathPlannerImplentation.ChildNodesPlan;
import org.iastate.ailab.qengine.core.datasource.DataNode;

import Zql.ZQuery;

public interface PathPlanner {
   public enum SplitType {
      NO_SPLIT, // Children are horizontal distributed datasources  or  no children (i.e  a  leaf node )

      /**
       * The Following apply where the children of currNode are vertically
       * distributed
       */

      // Vertically distributed where all queryColumns (including select) in one path
      NO_SPLIT_SINGLE_PATH,

      // The select columns occurs in different paths and  the where clauses don't have a split.
      SPLIT_SELECT_YES_WHERE_NO,

      // Case when all select columns in one path and where clauses in other path
      // However, split is there because the select columns and where columns occur in different data sources
      SPLIT_SELECT_NO_WHERE_NO,

      // All the operands are ANDED in where clause.
      // Probably a special case of  SPLIT_SELECT_YES_WHERE_YES_CONDITIONS_ANDED_BUT_SUBSET
      SPLIT_SELECT_NO_WHERE_YES_ALL_CONDITIONS_ANDED,

      // All the operands are ORED in where clause
      // However, the OR conditions nicely  distribute among children
      SPLIT_SELECT_NO_WHERE_YES_CONDITIONS_ORED_BUT_SUBSET,

      SPLIT_SELECT_NO_WHERE_YES_CONDITIONS_ANDED_BUT_SUBSET,

      // All the operands are ANDED in where clause.
      // Special cases for SPLIT_SELECT_YES_WHERE_YES_CONDITIONS_ANDED_BUT_SUBSET
      SPLIT_SELECT_YES_WHERE_YES_ALL_CONDITIONS_ANDED,

      // the operands are ORED in where clause
      //however, the OR conditions nicely  distribute
      //among children (Note the individual conditions
      //may contains any operator but they exist in same path)
      SPLIT_SELECT_YES_WHERE_YES_CONDITIONS_ORED_BUT_SUBSET,

      SPLIT_SELECT_YES_WHERE_YES_CONDITIONS_ANDED_BUT_SUBSET,

      // Split. Get all data and  Run local Query on top of it.
      YES,

      NOT_SUPPORTED
   }

   public ChildNodesPlan getPathPlan(DataNode currNode, ZQuery nodeQuery);
}
