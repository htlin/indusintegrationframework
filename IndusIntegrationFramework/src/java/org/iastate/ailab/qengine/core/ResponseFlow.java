package org.iastate.ailab.qengine.core;

import java.sql.ResultSet;
import java.sql.Statement;

import org.iastate.ailab.qengine.core.datasource.DataNode;

public interface ResponseFlow {
   public ResultSet execute(DataNode node, Statement stmt);
}
