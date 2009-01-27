package org.iastate.ailab.qengine.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.TransFormerUtility.FromTranslationResult;
import org.iastate.ailab.qengine.core.TransFormerUtility.QueryTranslationResult;
import org.iastate.ailab.qengine.core.TransFormerUtility.SelectTranslationResult;
import org.iastate.ailab.qengine.core.TransFormerUtility.WhereTranslationResult;
import org.iastate.ailab.qengine.core.datasource.ColumnDescriptor;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.datasource.DataSourceDescriptor;
import org.iastate.ailab.qengine.core.exceptions.TranslationException;
import org.iastate.ailab.qengine.core.reasoners.impl.DefaultReasonerImpl;
import org.iastate.ailab.qengine.core.reasoners.impl.DefaultSchemaMapStoreImpl;
import org.iastate.ailab.qengine.core.reasoners.impl.Reasoner;
import org.iastate.ailab.qengine.core.reasoners.impl.URIUtils;
import org.iastate.ailab.qengine.core.reasoners.interfaces.DataSchemaResource;
import org.iastate.ailab.qengine.core.util.ExpressionEvaluator;
import org.iastate.ailab.qengine.core.util.QueryUtils;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;

public class DefaultQueryTransFormer implements QueryTransFormer {

   private static final Logger logger = Logger
         .getLogger(DefaultQueryTransFormer.class);

   TransFormerUtility transformUtility = new TransFormerUtility();

   /**
    * This return a query which returns all the columns in the passedQuery
    * as are present in this data source
    * 
    * @param passedQuery
    * @param currNode the data source
    * @return
    */
   public QueryTranslationResult translate2GetAllColumnsQuery(
         ZQuery passedQuery, DataNode currNode) {

      if (currNode.isLeafNode()) {
         // passedQuery =
         // NodeQueryUtils.getMappedQuery(passedQuery,currNode,false);
      }

      DataSourceDescriptor desc = currNode.getDataSourceDescriptor();

      QueryTranslationResult queryResult = transformUtility.new QueryTranslationResult();
      Vector<String> queryColumns = QueryUtils.getQueryColumns(passedQuery);

      String currColumnName = null;
      String tableName = null;
      Map<String, String> translatedColumns = new HashMap<String, String>(); // HashMap so no duplication
      Map<String, String> translatedTables = new HashMap<String, String>(); // HashMap so no duplication

      for (int i = 0; i < queryColumns.size(); i++) {
         currColumnName = queryColumns.get(i);
         if (desc.isThisColumnPresent(currColumnName)) {
            tableName = desc.getTableForThisColumn(currColumnName);
            translatedColumns.put(currColumnName, currColumnName);
            translatedTables.put(tableName, tableName);
         }
      }

      if (translatedColumns.isEmpty()) {
         queryResult.participatesInTranslation = false;
         return queryResult;
      } else {
         queryResult.participatesInTranslation = true;
         queryResult.query = QueryUtils.generateSelectQuery(
               changeMapKeySetToStringArray(translatedColumns),
               changeMapKeySetToStringArray(translatedTables));
      }

      return queryResult;
   }

   public QueryTranslationResult simpleTranslateQuery(ZQuery passedQuery,
         DataNode currNode) {
      // this is a simple translation in which you take subset of query with the columns present in the desc.
      // TODO: Instead of translation it is more of SubQuery Calculation
      DataSourceDescriptor desc = currNode.getDataSourceDescriptor();

      if (currNode.isLeafNode()) {
         //passedQuery = NodeQueryUtils.getMappedQuery(passedQuery, currNode,    false);
      }

      SelectTranslationResult selectClause = simpleTranslateSelect(passedQuery,
            currNode);
      FromTranslationResult fromClause = simpleTranslateFrom(passedQuery,
            currNode);
      if (selectClause.participatesInTranslation == false
            || fromClause.participatesInTranslation == false) {
         return null;
         /*
          * TODO: Throw an exception. A query should always participate in
          * translation, otherwise it should not have been passed to this
          * datasource. In vertically distributed databases there will be
          * cases when only id's are in select clause.
          */
      }
      ZQuery res = new ZQuery();
      res.addSelect(selectClause.select);
      res.addFrom(fromClause.from);
      ZExpression where = (ZExpression) passedQuery.getWhere();
      if (where != null) {
         WhereTranslationResult whereClause = simpleTranslateZExpression(
               (ZExpression) passedQuery.getWhere(), currNode);
         if (whereClause.participatesInTranslation) {
            res.addWhere(whereClause.exp);
         }
      }

      QueryTranslationResult queryResult = transformUtility.new QueryTranslationResult();
      queryResult.participatesInTranslation = true; // a complete query should always participate in translation
      queryResult.query = res;
      return queryResult;
   }

   private WhereTranslationResult simpleTranslateAtomWhere(ZExpression expr,
         DataNode currNode) {
      WhereTranslationResult res = transformUtility.new WhereTranslationResult();
      DataSourceDescriptor desc = currNode.getDataSourceDescriptor();
      String mappedColumnName; // DataSourceView
      String columnName; // userView

      if (isAtomWhere(expr)) {
         // TranslationResult res = new DefaultQueryTransFormer.TranslationResult();

         // both operands that constitute this opearnd will be Zconstants since it is an atomWhere
         boolean addAtom = false;
         ZExpression thisOperand = expr;
         ZConstant exp1 = (ZConstant) thisOperand.getOperands().get(0);
         ZConstant exp2 = (ZConstant) thisOperand.getOperands().get(1);
         if (exp1.getType() == ZConstant.COLUMNNAME) {
            columnName = exp1.getValue();
            if (currNode.isLeafNode()) {
               mappedColumnName = currNode
                     .getMappedDataSourceColumnName(columnName);
            } else {
               mappedColumnName = columnName; // same for inner nodes
            }
            if (desc.isThisColumnPresent(mappedColumnName)) {
               addAtom = true;
            }
         } else if (exp2.getType() == ZConstant.COLUMNNAME) {
            columnName = exp2.getValue();
            if (currNode.isLeafNode()) {
               mappedColumnName = currNode
                     .getMappedDataSourceColumnName(columnName);
            } else {
               mappedColumnName = columnName; // same for inner nodes
            }
            if (desc.isThisColumnPresent(mappedColumnName)) {
               addAtom = true;
            }
         }

         if (addAtom) {
            res.participatesInTranslation = true;
            res.exp = expr;
         } else {
            res.participatesInTranslation = false;
         }

         return res;
      } else {
         // remark: for faster execution, if flow is correct you can do away with this check
         // currently keeping there since am paranoid
         int errorCode = 100;
         throw new TranslationException("Atom translation tried for " + expr,
               errorCode);
      }
   }

   private WhereTranslationResult simpleTranslateZExpression(ZExpression expr,
         DataNode currNode) {

      WhereTranslationResult globalRes = transformUtility.new WhereTranslationResult();
      WhereTranslationResult localRes = null;
      if (isAtomWhere(expr)) {
         return this.simpleTranslateAtomWhere(expr, currNode);
      }

      int numberOfOperandsInTranslation = 0;

      Vector<ZExp> operands = expr.getOperands();
      Vector<ZExp> translatedOperands = new Vector<ZExp>();
      for (int i = 0; i < operands.size(); i++) {
         ZExp currOperand = operands.get(i);
         if (currOperand instanceof ZQuery) {
            QueryTranslationResult res = simpleTranslateQuery(
                  (ZQuery) currOperand, currNode);
            if (res.participatesInTranslation) {
               numberOfOperandsInTranslation++;
               translatedOperands.add(res.query);
            }
         } else if (currOperand instanceof ZExpression) {
            if (isAtomWhere((ZExpression) currOperand)) {
               localRes = simpleTranslateAtomWhere((ZExpression) currOperand,
                     currNode);
               if (localRes.participatesInTranslation) {
                  numberOfOperandsInTranslation++;
                  translatedOperands.add(localRes.exp); // should be same as currOperand
               }
            } else {
               // not atom where
               localRes = simpleTranslateZExpression((ZExpression) currOperand,
                     currNode);
               if (localRes.participatesInTranslation) {
                  numberOfOperandsInTranslation++;
                  translatedOperands.add(localRes.exp); // should be same as currOperand;
               }
            }
         }
      }

      if (numberOfOperandsInTranslation > 1) {
         String operator = expr.getOperator();
         ZExpression resExpr = new ZExpression(operator);

         // add only translated operands
         resExpr.setOperands(translatedOperands);

         globalRes.participatesInTranslation = true;
         globalRes.exp = resExpr;

      } else if (numberOfOperandsInTranslation == 1) {
         /*
          * Only one operand. e.g a>2 oR c > 3. If only c>3 comes as part
          * of translation, the OR operator has to be dropped, instead >
          * becomes operator with two Zconstants(i.e. c , 3) as its
          * operands.
          */
         ZExpression exp = (ZExpression) translatedOperands.get(0);
         globalRes.participatesInTranslation = true;
         globalRes.exp = exp;
      } else {
         globalRes.participatesInTranslation = false;
      }
      return globalRes;
   }

   /**
    * if the where clause is composed of two Zconstants e.g status =
    * 'undergrad'; temp > 30; name = 'neeraj';
    * 
    * @param where
    * @return
    */
   private static boolean isAtomWhere(ZExpression where) {
      boolean result = false;
      int size = where.getOperands().size();
      if (size > 2) {
         return false;
      } else {
         // size=2
         ZExp exp1 = (ZExp) where.getOperands().get(0);
         ZExp exp2 = (ZExp) where.getOperands().get(1);
         if ((exp1 instanceof ZConstant) && (exp2 instanceof ZConstant)) {
            result = true;
         }
      }

      return result;
   }

   private FromTranslationResult simpleTranslateFrom(ZQuery query,
         DataNode currNode) {
      FromTranslationResult res = transformUtility.new FromTranslationResult();
      Vector<ZFromItem> translatedFrom = new Vector<ZFromItem>();
      ZFromItem currFromItem = null;
      String currTableName = null;
      String mappedTableName = null;
      DataSourceDescriptor desc = currNode.getDataSourceDescriptor();

      Vector<ZFromItem> from = query.getFrom();

      for (int i = 0; i < from.size(); i++) {
         currFromItem = from.get(i);
         currTableName = currFromItem.getTable();

         if (currNode.isLeafNode()) {
            mappedTableName = currNode.getMappedDataSourceTable(currTableName);
         } else {
            mappedTableName = currTableName;
         }

         logger.trace("MAPPING Tables(user->dataSourc):" + currTableName
               + "-->" + mappedTableName);
         if (desc.isThisTablePresent(mappedTableName)) {
            translatedFrom.add(currFromItem);
         }
      }

      if (translatedFrom.isEmpty()) {
         res.participatesInTranslation = false;
      } else {
         res.participatesInTranslation = true;
         res.from = translatedFrom;
      }

      return res;
   }

   private SelectTranslationResult simpleTranslateSelect(ZQuery query,
         DataNode currNode) {
      DataSourceDescriptor desc = currNode.getDataSourceDescriptor();
      SelectTranslationResult res = transformUtility.new SelectTranslationResult();
      Vector<ZSelectItem> translatedSelect = new Vector<ZSelectItem>();
      Vector<ZSelectItem> zSelectItems = query.getSelect();

      ZSelectItem currSelect;
      String currColumnName;
      String dataSourceColumnName;
      for (int i = 0; i < zSelectItems.size(); i++) {
         if (QueryUtils.isCountQuery(query)) {
            translatedSelect.add(zSelectItems.get(i)); // The Select will be a ZExpression and not a ZSelectItem
            // TODO: Handle count queries of form select count(firstname), key. firstname nor present (in future)
         } else {
            currSelect = zSelectItems.get(i);

            if (currSelect.isWildcard()) {
               // add this select column
               translatedSelect.add(currSelect);
            } else if (QueryUtils.isCountQuery(query)) {
               translatedSelect.add(currSelect);
               // if it a count query of form select count(firstname), key2 and firstname is not present
            } else {
               currColumnName = currSelect.getColumn();

               // convert the column in userView to dataSource view as you will see if this present from the dataSourceDescriptor
               dataSourceColumnName = currNode
                     .getMappedDataSourceColumnName(currColumnName);

               if (desc.isThisColumnPresent(dataSourceColumnName)) {
                  // add this select column
                  translatedSelect.add(currSelect);
               }
            }
         }
      }

      if (translatedSelect.isEmpty()) {
         res.participatesInTranslation = false;
         String message = desc.getDSName() + " does not particpate in query: "
               + query + " for SELECT clause";
         logger.trace(message);
      } else {
         res.participatesInTranslation = true;
         res.select = translatedSelect;
      }
      return res;
   }

   public Object reWriteQuery(ZQuery query, DataNode currNode) {
      // return same as input if not a leaf node (rewriting makes sense only for leaf node)
      if (!currNode.isLeafNode())
         return query;

      /*
       * note this should be called after mapping of column names has been
       * done so that you don't have to worry that the data descriptor is
       * the real one associated with the leaf node
       */
      ZQuery res = new ZQuery();
      // rewriting only applicable for where clause so keep the select and from clause
      res.addSelect(query.getSelect());
      res.addFrom(query.getFrom());
      if (query.getWhere() != null) {
         ZExpression where = reWriteQuery((ZExpression) query.getWhere(),
               currNode);
         res.addWhere(where);
      }

      return res;
   }

   private ZExpression reWriteQuery(ZExpression where, DataNode currNode) {
      boolean possibleAVHRole = false;
      boolean isAVHRole = false;

      //assume in beginning every column has possibility of  conversion function
      boolean possibleConversionFunction = true;
      boolean isConversionFunction = false;

      DataSourceDescriptor desc = currNode.getDataSourceDescriptor();
      DataSourceDescriptor userViewDesc = Init._this()
            .getUserViewDataSourceDescriptor();
      DefaultSchemaMapStoreImpl schemaStore = (DefaultSchemaMapStoreImpl) Init
            ._this().getViewContext().getSchemaMapStore();

      ColumnDescriptor colDesc = null;
      ColumnDescriptor userViewColDesc = null;
      DataSchemaResource userViewCol = null;

      String targetDataSource = currNode.getNodeName();//This should be actually currNode.getDataSource() but that is not initialized

      String operator = where.getOperator();
      ZExpression exp = new ZExpression(operator);
      if (QueryUtils.isPossibleAVHRole(operator)) {
         possibleAVHRole = true;
      }

      ZConstant tempZConstant;
      // ZConstant tempZConstantUserView;

      // ZConstant mappedZConstant = null;
      // String columnName;

      Vector<ZExp> operands = where.getOperands();
      //Vector userViewOperands = viewWhere.getOperands();

      for (int i = 0; i < operands.size(); i++) {
         ZExp currOperand = operands.get(i);
         // ZExp userViewOperand = (ZExp) userViewOperands.get(i); // the number of operands should be same, hence the index

         if (currOperand instanceof ZConstant) {
            tempZConstant = (ZConstant) currOperand;
            // tempZConstantUserView = (ZConstant)userViewOperand;

            if (tempZConstant.getType() == ZConstant.COLUMNNAME) {
               if (possibleAVHRole || possibleConversionFunction) {
                  colDesc = desc.getColumnDescriptor(tempZConstant.getValue());
                  isAVHRole = colDesc.isAVH();

                  /*
                   * Get the attribute in user view to which the current
                   * attribute in this data source is mapped. We are going
                   * from data source to user View. Works as we have only
                   * one table.
                   */
                  userViewCol = schemaStore.getUserViewColumnName(colDesc
                        .getColumnName(), currNode.getNodeName());

                  // userViewColDesc = userViewDesc.getColumnDescriptor(tempZConstantUserView.getValue());
                  userViewColDesc = userViewDesc
                        .getColumnDescriptor(userViewCol);

                  isConversionFunction = schemaStore
                        .UViewAttributeHasConversionFunctionToDataSource(
                              userViewCol, targetDataSource);
               }

               if (isAVHRole) {
                  isConversionFunction = false; //can't be both AVH and have a conversionFunction. AVH gets priority

                  // if this is AVHRole, the next operand should be value in ontology
                  i++; // move ahead in the operand list
                  // next operand should be an instance of Z Constant
                  ZConstant nextOperand = (ZConstant) operands.get(i);
                  // ZConstant userViewNextOperand = (ZConstant)userViewOperands.get(i);

                  String baseURI = colDesc.getProperty("base");
                  String userViewBaseURI = userViewColDesc.getProperty("base");

                  if (baseURI == null || baseURI.equals(""))
                     baseURI = "indus:";
                  if (userViewBaseURI == null || userViewBaseURI.equals(""))
                     userViewBaseURI = "indus:";

                  exp = new ZExpression("IN"); // remove the earlier operand (>,<,=) and replace by IN
                  exp.addOperand(currOperand);

                  // We have mapped just the columnNames. The attribute values are still in user view
                  String userViewAttributeValue = nextOperand.getValue();

                  URI userViewOntologyURI = userViewColDesc
                        .getColumnOntologyURI();
                  URI dataSourceOntologyURI = colDesc.getColumnOntologyURI();

                  URI userViewClassID;
                  try {
                     userViewClassID = URIUtils.createURI(
                           userViewAttributeValue, userViewBaseURI);
                  } catch (URISyntaxException e) {
                     throw new TranslationException(
                           "TranslationException while trying to crate a URI from the user view attribute value "
                                 + " and the base user view URI "
                                 + userViewBaseURI, e);
                  }
                  // call the reasoner with above values

                  Reasoner reasoner = Init._this().getReasonerFromCache(
                        currNode.getNodeName());
                  // use the utility Function in my DefaultReasoner
                  exp = ((DefaultReasonerImpl) reasoner).getAVHClass(
                        userViewOntologyURI, userViewClassID,
                        dataSourceOntologyURI, currNode.getNodeName(),
                        operator, exp);
               } else {
                  // not overloaded '>' '<' '='. Their normal definition
                  exp.addOperand(currOperand);
                  //see if it has conversionFunction
                  if (isConversionFunction) {
                     i++; // move ahead in the operand list
                     // next operand should be an instance of Z Constant which has to be Converted
                     ZConstant nextOperand = (ZConstant) operands.get(i);
                     String value = nextOperand.getValue();

                     ExpressionEvaluator eval = schemaStore
                           .conversionFunctionToDataSource(userViewCol,
                                 targetDataSource);
                     String convertV = eval.getValue(value); //evaluate conversionFunction
                     ZConstant convertedValue = new ZConstant(convertV,
                           ZConstant.NUMBER);
                     exp.addOperand(convertedValue);
                     logger.debug("Appplied ConversionFunction to get "
                           + currOperand.toString() + ":userView --> " + value
                           + " mapped to  " + convertV);

                  }
               }
            } else { // Not a ZConstant.ColumnName
               exp.addOperand(currOperand);
            }
         } else if (currOperand instanceof ZExpression) {
            exp.addOperand(reWriteQuery((ZExpression) currOperand, currNode));
         } else if (currOperand instanceof ZQuery) {
            exp = new ZExpression(operator);
            exp
                  .addOperand((ZQuery) reWriteQuery((ZQuery) currOperand,
                        currNode));
         }
      }

      return exp;
   }

   private static String[] changeMapKeySetToStringArray(Map<String, String> map) {
      int size = map.size();
      String[] res = new String[size];
      int i = 0;
      Iterator<String> it = map.keySet().iterator();
      while (it.hasNext()) {
         res[i++] = it.next();
      }
      return res;
   }
}
