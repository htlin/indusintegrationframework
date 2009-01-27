package org.iastate.ailab.qengine.core.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.Init;
import org.iastate.ailab.qengine.core.datasource.ColumnDescriptor;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.datasource.DefaultColumnDescriptorImpl;
import org.iastate.ailab.qengine.core.datasource.DefaultDataSourceDescriptorImplementation;
import org.iastate.ailab.qengine.core.exceptions.ConfigurationException;
import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;
import org.iastate.ailab.qengine.core.reasoners.impl.SchemaMapStore;

public class SchemaMapPropertyLoader implements SchemaMapLoader {

   private static final Logger logger = Logger
         .getLogger(SchemaMapPropertyLoader.class);

   public SchemaMapStore addSchemaMap(DataNode node) {

      SchemaMapStore map = null;
      String mapFile = Init._this().getViewConfigData().getSchemaMap();

      try {
         map = Init._this().getViewContext().getSchemaMapStore();
      } catch (RuntimeException e) {
         String message = "Error 1000";
         logger.warn(message, e);
         throw new ConfigurationException(message, e);
      }

      Properties schemaMap = FileUtils.loadProperites(mapFile);

      Enumeration<Object> keys = schemaMap.keys();
      Map<String, Vector<String>> dataNodeMap = new HashMap<String, Vector<String>>();

      while (keys.hasMoreElements()) {
         String key = (String) keys.nextElement();
         String[] blah = key.split("\\.");
         String nodeName = blah[0];

         if (dataNodeMap.containsKey(nodeName)) {
            /*
             * if the nodeName is already in the map then just add the key
             * to the vector under that node name
             */
            dataNodeMap.get(nodeName).add(key);
         } else {
            /*
             * if the nodeName isn't in the map then create a new vector to
             * store the elements and add the vector to the map with the
             * nodeName as the key
             */
            Vector<String> dsElements = new Vector<String>();
            dsElements.add(key);
            dataNodeMap.put(nodeName, dsElements);
         }
      }

      /*
       * go through each data source in the map and get it's respective
       * vector and add it to the correct node
       */
      for (Map.Entry<String, Vector<String>> nodeMap : dataNodeMap.entrySet()) {
         String nodeName = nodeMap.getKey();
         Vector<String> elements = nodeMap.getValue();
         if (addMappedInfo(node, nodeName, elements, schemaMap)) {
            // TODO throw an exception because this map doesn't match a node
         }
      }

      // Add data descriptors to inner nodes
      this.addInnerNodeMap(node, schemaMap);

      //TODO  Fill the map appropriately, currently it is empty
      return map;
   }

   private boolean addMappedInfo(DataNode currNode, String nodeName,
         Vector<String> keys, Properties schemaMap) {
      //TODO: refactor now that we have SchemamapStore. See if we can fill Tablemap, ReverseColumnMap etc. from there

      boolean found = false;

      if (!currNode.getNodeName().equals(nodeName)) {
         for (DataNode child : currNode.getChildren()) {
            found = this.addMappedInfo(child, nodeName, keys, schemaMap);

            // break so we don't traverse the rest of the tree
            if (found)
               break;
         }
      } else {
         boolean addedTableMap = false;

         // table and columns for the data source descriptor
         HashSet<ColumnDescriptor> tableColumns = new HashSet<ColumnDescriptor>();

         String tableName = null;

         for (String key : keys) {
            String value = schemaMap.getProperty(key, ""); // have "" be the default value, this shouldn't happen

            if (!addedTableMap) {
               // only need to map the table 1 time

               // TODO need to change this if we allow for more than 1 table
               addedTableMap = true;
               String userTable = value.split("\\.")[1];
               String dsTable = key.split("\\.")[1];
               tableName = dsTable;

               currNode.addTableMap(userTable, dsTable);
            }

            String userViewCol = value.split("\\.")[2];
            String dsCol = key.split("\\.")[2];

            if (userViewCol.indexOf(",") != -1) {
               //userViewCol may have conversion function tagged with it, So get the actual userViewCol
               //DS1_Table.compensation=DS1_DS2_DS3.EMPLOYEETABLE.benefits,9/5*(x)+32,(x-32)*5/9
               userViewCol = userViewCol.substring(0, userViewCol.indexOf(","));
            }

            currNode.addToReverseColumnMap(dsCol, userViewCol);

            ColumnDescriptor currColumnDescriptor = new DefaultColumnDescriptorImpl();
            currColumnDescriptor.setColumnName(dsCol);
            // add column for data source descriptor
            tableColumns.add(currColumnDescriptor);
         }

         // Add mapped data source descriptor
         DefaultDataSourceDescriptorImplementation desc = (DefaultDataSourceDescriptorImplementation) SolutionCreator
               .getDataSourceDescriptor();

         desc.setDSName(currNode.getNodeName());
         desc.addTable(tableName, tableColumns);
         desc.setVirtual(false);

         //TODO: Verify we no longer need Mapped Descriptor
//         currNode.setMappedDataSourceDescriptor(desc);

         found = true;
      }

      return found;
   }

   private void addVirtualDataDesc(DataNode currNode, Properties schemaMap) {
      Enumeration<Object> keys = schemaMap.keys();
      HashSet<ColumnDescriptor> userColDescs = new HashSet<ColumnDescriptor>();
      Vector<String> userCol = new Vector<String>();
      String tableName = null;
      // get column names and table name
      while (keys.hasMoreElements()) {
         String key = (String) keys.nextElement();

         if (tableName == null) {
            tableName = schemaMap.getProperty(key).split("\\.")[1];
         }

         // Create a list of all user view column names
         String userColName = schemaMap.getProperty(key).split("\\.")[2];
         if (userColName.indexOf(",") != -1) {
            //userViewCol may have conversion function, So get the actual userView Col
            //DS1_Table.compensation=DS1_DS2_DS3.EMPLOYEETABLE.benefits,9/5*(x)+32,(x-32)*5/9
            userColName = userColName.substring(0, userColName.indexOf(","));
         }

         if (!userCol.contains(userColName)) {
            ColumnDescriptor currColumnDescriptor = new DefaultColumnDescriptorImpl();
            currColumnDescriptor.setColumnName(userColName);

            /*
             * since it is an Inner node we just keep the name in
             * ColumnDescriptor Implies other values will be default. Don't
             * use them(you should not have to)
             */
            userColDescs.add(currColumnDescriptor);

            //keep track of all the Column Names added so that you don't add it twice
            userCol.add(userColName);
         }
      }

      // Add the descriptor to the node
      DefaultDataSourceDescriptorImplementation desc = (DefaultDataSourceDescriptorImplementation) SolutionCreator
            .getDataSourceDescriptor();

      desc.setDSName(currNode.getNodeName());
      desc.addTable(tableName, userColDescs);
      desc.setVirtual(true);

      currNode.setDataSourceDescriptor(desc);

      // Add table map for virtual node.  Maps userview table to userview table
      currNode.addTableMap(tableName, tableName);
   }

   private void addInnerNodeMap(DataNode currNode, Properties schemaMap) {
      if (!currNode.isLeafNode()) {
         if (!currNode.getNodeName().equals(
               Init._this().getViewConfigData().getUserViewName())) {
            this.addVirtualDataDesc(currNode, schemaMap);
         }

         for (DataNode child : currNode.getChildren()) {
            this.addInnerNodeMap(child, schemaMap);
         }
      }
   }
}
