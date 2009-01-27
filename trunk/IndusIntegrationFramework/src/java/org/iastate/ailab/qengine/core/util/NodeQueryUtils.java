package org.iastate.ailab.qengine.core.util;

import java.util.Vector;

import org.iastate.ailab.qengine.core.Init;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.datasource.DataSourceDescriptor;
import org.iastate.ailab.qengine.core.datasource.DataNode.QueryType;
import org.iastate.ailab.qengine.core.reasoners.impl.SchemaMapStore;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;

public class NodeQueryUtils {
   public static boolean isSingleChildrenContainTheseColumns(DataNode currNode,
         Vector<String> columns) {
      boolean result = false;
      DataSourceDescriptor desc = null;

      DataNode currChild = null;
      Vector<DataNode> children = currNode.getChildren();
      for (int i = 0; i < children.size(); i++) {
         currChild = children.get(i);
         desc = currChild.getDataSourceDescriptor();
         /*
          * for leaf nodes the datasource descriptor refers to actual data
          * source. So convert the columns you are looking in data source
          * view (transformation happens only for leaf nodes)
          */
         Vector<String> dataSourceColumns = userViewColumnsToDataSourceColumns(
               columns, currChild);
         if (desc.areAllColumnsPresent(dataSourceColumns)) {
            result = true;
            break;
         }
      }
      return result;
   }

   /**
    * Checks if the currNode contains all the columns
    * 
    * @param currNode
    * @param columns
    * @return
    */
   public static boolean isCurrentNodeContainTheseColumns(DataNode currNode,
         Vector<String> columns) {
      boolean result = false;
      DataSourceDescriptor desc = currNode.getDataSourceDescriptor();
      /*
       * for leaf nodes the datasource descriptor refers to actual data
       * source. So convert the columns you are looking in data source view
       * (transformation happens only for leaf nodes)
       */
      Vector<String> dataSourceColumns = userViewColumnsToDataSourceColumns(
            columns, currNode);
      if (desc.areAllColumnsPresent(dataSourceColumns)) {
         result = true;
      }
      return result;
   }

   /**
    * This checks if the query can be answered by a single child Will be
    * useful when children of node are vertically fragmented .If it returns
    * -1 implies it is not a singlePath Query Any value > -1 implies it is
    * a singlePath query with the index referring to the child answering
    * the query
    * 
    * @param currNode
    * @param nodeQuery
    * @return
    */
   public static int isSinglePathQuery(DataNode currNode, ZQuery nodeQuery) {
      /**
       * Case 1: All Columns present in a single child
       * 
       * Case 2: Count(*)query with no where clause. Since Count(*) with no
       * where clause it is equal to number of instances in any of
       * children.
       * 
       * Case: Count(*) with the where clause columns in a single child
       */

      //See if Count queries can be handled by a single child
      if (currNode.getQueryType() == QueryType.COUNT_QUERY) {
         return isSinglePathCountQuery(currNode, nodeQuery);
      }

      //see if data query can be answered by a single child
      int resultIndex = -1;
      Vector<DataNode> children = currNode.getChildren();
      Vector<String> columns;
      DataSourceDescriptor desc = null;
      DataNode currChild = null;
      for (int i = 0; i < children.size(); i++) {
         currChild = children.get(i);
         desc = currChild.getDataSourceDescriptor();
         columns = QueryUtils.getQueryColumns(nodeQuery);
         /*
          * for leaf nodes the datasource descriptor refers to actual data
          * source. So convert the columns you are looking in data source
          * view (transformation happens only for leaf nodes)
          */
         Vector<String> dataSourceColumns = userViewColumnsToDataSourceColumns(
               columns, currChild);

         if (desc.areAllColumnsPresent(dataSourceColumns)) {
            resultIndex = i;
            //set which child answers query

            break;
         }
      }

      return resultIndex;
   }

   /**
    * For a count query checks if it can be answered by a single child
    * 
    * @param currNode
    * @param query
    * @return index of the child that can answer query (-1 otherwise)
    */
   private static int isSinglePathCountQuery(DataNode currNode, ZQuery query) {
      /*
       * Case 2: Count()query with no where clause. Since Count() with no
       * where clause it is equal to number of instances in any of
       * children.
       * 
       * Case: Count() with the where clause columns in a single child
       */
      int childIndex = -1;
      if (NodeQueryUtils.isWildCardCountQuery(query)) {
         ZExpression where = (ZExpression) query.getWhere();
         if (where == null) {
            childIndex = 0; //select the first children as one  where to get it from (could be any child)
         } else {
            //check if all the where columns are present in a single child
            Vector<String> columns = QueryUtils.getWhereColumns(query);
            Vector<DataNode> children = currNode.getChildren();
            DataSourceDescriptor desc = null;
            DataNode currChild = null;
            for (int i = 0; i < children.size(); i++) {
               currChild = children.get(i);
               desc = currChild.getDataSourceDescriptor();

               /*
                * for leaf nodes the datasource descriptor refers to actual
                * data source. So convert the columns you are looking in
                * data source view (transformation happens only for leaf
                * nodes)
                */
               Vector<String> dataSourceColumns = userViewColumnsToDataSourceColumns(
                     columns, currChild);

               if (desc.areAllColumnsPresent(dataSourceColumns)) {
                  childIndex = i;
                  //set which child answers query

                  break;
               }

            }
         }

      }
      return childIndex;
   }

   public static boolean isSubSetQueryForOperandOR(DataNode currNode,
         ZQuery nodeQuery) {
      boolean result = true; //initialize to true, will make if false if conditions don't match

      if (!QueryUtils.isThisFirstOperator(nodeQuery, "OR")) {
         return false;
      }

      //first operator is OR
      ZExpression where = (ZExpression) nodeQuery.getWhere();
      Vector<ZExp> operands = where.getOperands();
      Vector<String> operandColumns = null;
      for (int i = 0; i < operands.size(); i++) {
         ZExp currOperand = operands.get(i);
         operandColumns = QueryUtils.getOperandColumns(currOperand);
         if (!isAllColumnsExistInSomeChild(operandColumns, currNode)) {
            result = false;
            break;
         }
      }

      return result;
   }

   public static boolean isSubSetQueryForOperandAND(DataNode currNode,
         ZQuery nodeQuery) {
      boolean result = true;
      if (!QueryUtils.isThisFirstOperator(nodeQuery, "AND")) {
         return false;
      }
//		first operator is AND
      ZExpression where = (ZExpression) nodeQuery.getWhere();
      Vector<ZExp> operands = where.getOperands();
      Vector<String> operandColumns = null;
      for (int i = 0; i < operands.size(); i++) {
         ZExp currOperand = operands.get(i);
         operandColumns = QueryUtils.getOperandColumns(currOperand);
         if (!isAllColumnsExistInSomeChild(operandColumns, currNode)) {
            result = false;
            break;
         }
      }
      return result;
   }

   /**
    * returns true if all the columns are present in some children of the
    * currNode
    * 
    * @param columns
    * @param currNode
    * @return
    */
   public static boolean isAllColumnsExistInSomeChild(Vector<String> columns,
         DataNode currNode) {
      boolean result = false;
      Vector<DataNode> children = currNode.getChildren();
      DataNode currChild = null;

      for (int i = 0; i < children.size(); i++) {
         currChild = children.get(i);
         /*
          * for leaf nodes the datasource descriptor refers to actual data
          * source. So convert the columns you are looking in data source
          * view (transformation happens only for leaf nodes)
          */
         Vector<String> dataSourceColumns = userViewColumnsToDataSourceColumns(
               columns, currChild);
         if (currChild.getDataSourceDescriptor().areAllColumnsPresent(
               dataSourceColumns)) {
            //note it will work for leaf nodes too as we have converted the columns in data source view
            result = true;
            break;
         }
      }
      return result;
   }

   /**
    * This converts the columns in userView to the associated columns in
    * this data source This transformation really does anything only for
    * leaf nodes only. For inner nodes returns the input columns This is
    * used when you want to find if all the columns of a query(in user
    * view) can be answered by this data source The mapping is required
    * because for leaf nodes the datasource descriptor corresponds to the
    * real data sources
    * 
    * @param columns
    * @param currNode
    * @return
    */
   private static Vector<String> userViewColumnsToDataSourceColumns(
         Vector<String> columns, DataNode currNode) {

      if (!currNode.isLeafNode())
         return columns;

      Vector<String> mappedColumns = new Vector<String>();
      String temp;
      for (int i = 0; i < columns.size(); i++) {
         temp = currNode.getMappedDataSourceColumnName(columns.get(i));
         mappedColumns.add(temp);
      }
      return mappedColumns;
   }

   /**
    * converts the query into a mappedQuery if dropUnMapped = true, the
    * unMapped columns/tables are dropped
    * 
    * @param query
    * @param currNode
    * @param dropUnMapped
    * @return
    */
   public static ZQuery getMappedQuery(ZQuery query, DataNode currNode,
         boolean dropUnMapped) {
      //TODO: REFACTOR: It was written before we had ColumnDescriptor and SchemaMap Store
      // Make Appropriate Changes to the DataNode Interface and Implementation
      String qs = query.toString();

      if (!currNode.isLeafNode()) {
         //TODO Verify  FROM clause needs to be mapped'
         //assuming not since that inner nodes will have same tables as user view or subset of them

         return query;
      }

      Vector<ZSelectItem> replacedSelect = getReplacedSelect(query.getSelect(),
            currNode, dropUnMapped);
      Vector<ZFromItem> replacedFrom = getReplacedFrom(query.getFrom(),
            currNode, dropUnMapped);
      ZExpression where = (ZExpression) query.getWhere();
      ZExpression replacedWhere = null;
      //all queries need not necessarily have where clause
      if (where != null) {
         replacedWhere = getReplacedWhere(where, currNode, dropUnMapped);
      }
      ZQuery res = new ZQuery();
      res.addSelect(replacedSelect);
      res.addFrom(replacedFrom);
      if (where != null) {
         res.addWhere(replacedWhere);
      }
      return res;
   }

   /**
    * Checks if the passed query has the following select clause:
    * 
    * Select Count(*)
    * 
    * @param query
    * @return
    */
   public static boolean isWildCardCountQuery(ZQuery query) {
      Vector<ZSelectItem> select = query.getSelect();
      ZSelectItem currSelectItem;
      boolean countWildCardQuery = false;

      for (int i = 0; i < select.size(); i++) {
         currSelectItem = select.get(i);
         if (currSelectItem.getExpression() instanceof ZExpression) {
            if (currSelectItem.getAggregate().equalsIgnoreCase("Count")) {
               ZExpression zExp = (ZExpression) currSelectItem.getExpression();
               String col = zExp.getOperand(0).toString();
               if (col.equals("*")) {
                  countWildCardQuery = true;
               }
            } else {
               countWildCardQuery = false;
            }

         }

      }
      return countWildCardQuery;
   }

   private static Vector<ZSelectItem> getReplacedSelect(
         Vector<ZSelectItem> select, DataNode currNode, boolean dropUnMapped) {
      Vector<ZSelectItem> replacedSelect = new Vector<ZSelectItem>();
      ZSelectItem currSelectItem;
      ZSelectItem replacedSelectItem = null;
      ZSelectItem replacedCountSelectItem = null;
      boolean countQuery = false;
      String mappedColumnName = null;
      for (int i = 0; i < select.size(); i++) {
         currSelectItem = select.get(i);
         if (currSelectItem.getExpression() instanceof ZExpression) {
            // If it's a count query we want to keep the Count part but replace the part in parenthesis with the mapped column name
            if (currSelectItem.getAggregate().equalsIgnoreCase("Count")) {
               countQuery = true;
               ZExpression zExp = (ZExpression) currSelectItem.getExpression();
               String col = zExp.getOperand(0).toString();
               if (col.equals("*")) {
                  mappedColumnName = currSelectItem.getColumn();
                  //this is a count(*). No Need to map, so you can use the currSelectItem
                  replacedCountSelectItem = currSelectItem;
               } else {
                  String mappedCol = currNode
                        .getMappedDataSourceColumnName(col);

                  //this will be COUNT(columnName) but Zquery construction treats it as a columnName, so we use replacedCountSelectItem
                  mappedColumnName = currSelectItem.getColumn().replace(col,
                        mappedCol); //this will be COUNT(columnName)
                  replacedCountSelectItem = QueryUtils
                        .createCountQuerySelectPart(mappedCol);
               }
            }
         } else {
            mappedColumnName = currNode
                  .getMappedDataSourceColumnName(currSelectItem.getColumn());
         }

         if (mappedColumnName != null) {
            if (!countQuery) {
               replacedSelectItem = new ZSelectItem(mappedColumnName);
            } else {
               replacedSelectItem = replacedCountSelectItem;
            }
         } else if (!dropUnMapped) {
            //no mapping found but if NOT dropUnMapped use the currSelectItem
            replacedSelectItem = currSelectItem;
         }
         replacedSelect.add(replacedSelectItem);
      }

      return replacedSelect;
   }

   private static Vector<ZFromItem> getReplacedFrom(Vector<ZFromItem> from,
         DataNode currNode, boolean dropUnMapped) {
      Vector<ZFromItem> replacedFrom = new Vector<ZFromItem>();
      ZFromItem currFromItem;
      ZFromItem replacedFromItem = null;
      String mappedTableName;

      for (int i = 0; i < from.size(); i++) {
         currFromItem = from.get(i);
         mappedTableName = currNode.getMappedDataSourceTable(currFromItem
               .getTable());
         if (mappedTableName != null) {
            replacedFromItem = new ZFromItem(mappedTableName);
         } else if (!dropUnMapped) {
            replacedFromItem = currFromItem;
         }
         replacedFrom.add(replacedFromItem);
      }
      return replacedFrom;
   }

   private static ZExpression getReplacedWhere(ZExpression where,
         DataNode currNode, boolean dropUnMapped) {
      SchemaMapStore schemaStore = Init._this().getViewContext()
            .getSchemaMapStore();

      String operator = where.getOperator();
      ZExpression exp = new ZExpression(operator);

      ZConstant tempZConstant;
      ZConstant mappedZConstant = null;
      String mappedColumnName;
      Vector<ZExp> operands = where.getOperands();
      for (int i = 0; i < operands.size(); i++) {
         ZExp currOperand = operands.get(i);
         if (currOperand instanceof ZConstant) {
            tempZConstant = (ZConstant) currOperand;

            if (tempZConstant.getType() == ZConstant.COLUMNNAME) {
               mappedColumnName = currNode
                     .getMappedDataSourceColumnName(tempZConstant.getValue());
               if (mappedColumnName != null) {
                  mappedZConstant = new ZConstant(mappedColumnName,
                        ZConstant.COLUMNNAME);
               } else if (!dropUnMapped) {
                  mappedZConstant = tempZConstant;
               }
               exp.addOperand(mappedZConstant);
            } else {
               exp.addOperand(currOperand);
            }
         } else if (currOperand instanceof ZExpression) {
            exp.addOperand(getReplacedWhere((ZExpression) currOperand,
                  currNode, dropUnMapped));
         } else if (currOperand instanceof ZQuery) {
            exp.addOperand(getMappedQuery((ZQuery) currOperand, currNode,
                  dropUnMapped));
         }
      }

      return exp;
   }
//	public static ZQuery addJoinColumn(ZQuery query, JoinColumn joinColumn,
//         DataNode node, boolean dropUnMapped) {
//
//      if (node.isLeafNode()) {
//         query = NodeQueryUtils.getMappedQuery(query, node, dropUnMapped);
//      }
//      return QueryUtils.addJoinColumn(query, joinColumn);
//   }
}
