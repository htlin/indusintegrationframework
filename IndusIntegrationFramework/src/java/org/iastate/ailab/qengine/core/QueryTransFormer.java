package org.iastate.ailab.qengine.core;

import org.iastate.ailab.qengine.core.TransFormerUtility.QueryTranslationResult;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.exceptions.TranslationException;

import Zql.ZQuery;

public interface QueryTransFormer {

   /**
    * This return a query which returns all the columns in the passedQuery
    * as are present in this data source The return contains no where
    * clause but has the columns participating in where clause returned as
    * part of select
    * 
    * @param passedQuery
    * @param DataNode The data source
    * @return
    */
   public QueryTranslationResult translate2GetAllColumnsQuery(
         ZQuery passedQuery, DataNode node);

   /**
    * this is a simple translation in which you take subset of query with
    * the columns present in the descriptor of the data node. This includes
    * the subset of the query in where clauses too. It is assumed that
    * atleast one column of query
    * 
    * @param passedQuery
    * @param node
    * @return
    * @throws TranslationException
    */
   public QueryTranslationResult simpleTranslateQuery(ZQuery passedQuery,
         DataNode node);

   /**
    * Re-write the query so that it can be executed. It is defined only for
    * a leaf node This is Specific to a Data Source binding. For a
    * Relational Data, it will return ZQuery string which can be executed
    */

   public Object reWriteQuery(ZQuery translatedQuery, DataNode currNode);
}
