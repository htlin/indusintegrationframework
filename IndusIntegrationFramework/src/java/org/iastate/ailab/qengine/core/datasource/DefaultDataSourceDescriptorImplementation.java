package org.iastate.ailab.qengine.core.datasource;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;

import org.iastate.ailab.qengine.core.reasoners.impl.DataSchemaResourceImpl;
import org.iastate.ailab.qengine.core.reasoners.interfaces.DataSchemaResource;

public class DefaultDataSourceDescriptorImplementation implements
      DataSourceDescriptor {

   private String DSName;

   private static int id = 0;

   private boolean virtualNode;

   /*
    * Stores a mapping from the DataSchemaResource to the ColumnDescriptor
    */
//	private Map<DataSchemaResource,ColumnDescriptor> colDescriptors = 
//					new HashMap<DataSchemaResource,ColumnDescriptor>();
   private Map<String, ColumnDescriptor> colDescriptors = new HashMap<String, ColumnDescriptor>();

   class Table {
      String tableName;

      Set<ColumnDescriptor> columns;

      Table(String tableName, Set<ColumnDescriptor> cols) {
         this.tableName = tableName;
         this.columns = cols;
      }

      Vector<String> getColumnNames() {
         Vector<String> columnNames = new Vector<String>();
         if (columns == null)
            return columnNames;
         Iterator<ColumnDescriptor> it = columns.iterator();
         while (it.hasNext()) {

            columnNames.add(it.next().getColumnName());
         }
         return columnNames;
      }
   }

   private Set<String> AVHColumns = new HashSet<String>();

   // Stores a map with tableName as key
   private Map<String, Table> tableMap = new HashMap<String, Table>();

   private Map<String, String> columnOntologies = new HashMap<String, String>();

   public DefaultDataSourceDescriptorImplementation() {
      id++;
   }

   public String getDSId() {

      return String.valueOf(id);
   }

   public void setDSName(String DSName) {
      this.DSName = DSName;
   }

   public String getDSName() {
      return DSName;
   }

   public void addTable(String tableName, Set<ColumnDescriptor> columns) {
      Table newTable = new Table(tableName, columns);
      tableMap.put(tableName, newTable);

      //Also build a map between dataSchemaresource and ColumnDescriptors
      DataSchemaResource inp = null;
      ColumnDescriptor currCol = null;
      Iterator<ColumnDescriptor> it = columns.iterator(); //This is Space Vs. Time constraint. You are building this beforehand
      while (it.hasNext()) {
         currCol = it.next();
         inp = new DataSchemaResourceImpl(DSName, tableName, currCol
               .getColumnName());
         colDescriptors.put(inp.toString(), currCol);
      }
   }

   public String[] getDSTables() {
      return (String[]) tableMap.keySet().toArray();
   }

   public Vector<String> getDSTableColumnNames(String tableName) {
      Table res = tableMap.get(tableName);

      return res.getColumnNames();
   }

   public Vector<String> getAllColumnNames() {
      /*
       * TODO: Resolve Name conflicts if two different tables have the same
       * columnName. Probably the return type should be table.columnName.
       * Also Refactor the Return Type to be a HashSet.
       */
      Vector<String> allCols = new Vector<String>();
      Iterator<String> it = tableMap.keySet().iterator();
      while (it.hasNext()) {
         String currTableName = it.next();
         Vector<String> currTableColumns = getDSTableColumnNames(currTableName);
         for (int i = 0; i < currTableColumns.size(); i++) {
            allCols.add(currTableColumns.get(i));
         }
      }
      return allCols;
   }

   public String getTableForThisColumn(String columnName) {
      boolean found = false;
      String foundTableName = null;
      String currTableName = null;
      Iterator<String> it = tableMap.keySet().iterator();
      Vector<String> tableColumns;
      while (it.hasNext() && !found) {
         currTableName = it.next();
         getDSTableColumnNames(currTableName);
         tableColumns = getDSTableColumnNames(currTableName);
         if (tableColumns.contains(columnName)) {
            found = true;
            foundTableName = currTableName;
         }
      }
      return foundTableName;
   }

   public boolean areAllColumnsPresent(Vector<String> columns) {
      Vector<String> allColumnNames = this.getAllColumnNames();
      return allColumnNames.containsAll(columns);
   }

   public boolean isSomeColumnPresent(Vector<String> columns) {
      //Also returns true if  columns is *
      boolean found = false;
      String currColumnName = null;
      for (int i = 0; i < columns.size(); i++) {
         currColumnName = columns.get(i);
         if (currColumnName.equals("*") || isThisColumnPresent(currColumnName)) {
            found = true;
            break;
         }
      }
      return found;
   }

   public boolean isThisColumnPresent(String columnName) {
      Vector<String> allColumnNames = this.getAllColumnNames();
      return allColumnNames.contains(columnName);
   }

   public boolean isThisTablePresent(String tableName) {
      return tableMap.containsKey(tableName);
   }

   public boolean isVirtual() {
      return virtualNode;
   }

   public void setVirtual(boolean isVirtual) {
      this.virtualNode = isVirtual;
   }

   public String getOntologyNameForThisColumn(String table, String column) {
      return columnOntologies.get(table + ":" + column);
   }

   public void setOntologyNameForThisColumn(String table, String column,
         String ontologyName) {
      columnOntologies.put(table + ":" + column, ontologyName);
   }

   public void setColumnTypeToAVH(String table, String column) {
      AVHColumns.add(table + ":" + column);
   }

   public boolean isColumnAVH(String table, String column) {
      return AVHColumns.contains(table + ":" + column);
   }

//	private void displayColDescriptors() {
//      Iterator<DataSchemaResource> it = colDescriptors.keySet().iterator();
//      DataSchemaResource key = null;
//      ColumnDescriptor value = null;
//      while (it.hasNext()) {
//         temp = it.next();
//         colDescriptors.get(temp);
//      }
//   }

   public ColumnDescriptor getColumnDescriptor(DataSchemaResource column) {
      String key = column.toString();
      return colDescriptors.get(key);
   }

//	 public void setColumnDescriptor(DataSchemaResource column,
//         ColumnDescriptor desc) {
//      colDescriptors.put(column, desc);
//   }

   //It will loop through all tables and return the descriptor for first table containing this column
   public ColumnDescriptor getColumnDescriptor(String column) {
      DataSchemaResource dsr = null;
      String currTableName = null;
      ColumnDescriptor result = null;
      boolean found = false;
      Iterator<String> it = tableMap.keySet().iterator();
      //loop through all the table Names
      while (it.hasNext() && !found) {
         currTableName = it.next();
         //construct a DataSchemaResource with the current table Name
         dsr = new DataSchemaResourceImpl(DSName, currTableName, column);
         result = getColumnDescriptor(dsr);
         if (result != null)
            found = true;
      }
      return result;
   }

   @Override
   public String toString() {
      StringWriter stringWriter = new StringWriter();
      display(stringWriter);
      return stringWriter.toString();
   }

   public void display(OutputStream out) {
      display(new PrintWriter(out));
   }

   public void display(Writer writer) {
      display(new PrintWriter(writer));
   }

   public void display(PrintWriter printWriter) {
      printWriter.println(DSName);
   }
}
