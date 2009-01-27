package org.iastate.ailab.qengine.core.aggregators;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.datasource.DataNode.DataAggregationType;

public interface DataAggregator {

   public ResultSet aggregate() throws SQLException;

   public ResultSet aggregate(Statement stmt) throws SQLException;

   public void setAggregationType(DataAggregationType type);

   public DataAggregationType getAggregationType();

   public void setDataNode(DataNode node);

   public DataNode getDataNode();
}
