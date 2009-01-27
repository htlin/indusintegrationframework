package org.iastate.ailab.qengine.core.datasource;

import java.util.Set;
import java.util.Vector;

import org.iastate.ailab.qengine.core.reasoners.interfaces.DataSchemaResource;
import org.iastate.ailab.qengine.core.util.Displayable;

public interface DataSourceDescriptor extends Displayable {

   //TODO: Refactor to use  ColumnDescriptor at appropriate places
   public String getDSId();

   public String getDSName();

   public void setDSName(String name);

   //Future: When we move to multiple tables use a Table Descriptor
   public String[] getDSTables();

   public void addTable(String tableName, Set<ColumnDescriptor> columns);

   /*
    * if it is an inner node, the column names should correspond to ones in
    * user view. The exception would be keys used to join vertical data
    * (and hence user need not know about it). In other words it is
    * responsibility of the populator to do appropriate transforms.
    */
   public Vector<String> getDSTableColumnNames(String tableName);

   public Vector<String> getAllColumnNames();

   public String getTableForThisColumn(String columnName);

   //TODO: USe a ColumnDescriptor
   public String getOntologyNameForThisColumn(String table, String columnName);

   public void setOntologyNameForThisColumn(String table, String columnName,
         String ontologyName);

   public void setColumnTypeToAVH(String table, String column);

   //utility functions: Should we move it to DefaultImplemntations

   /**
    * Checks whether all the columns are part of this data source
    * 
    * @param columns
    * @return
    */
   public boolean areAllColumnsPresent(Vector<String> columns);

   /**
    * checks whether at least one of the columns passed is present in this
    * data source
    * 
    * @param columns
    * @return
    */
   public boolean isSomeColumnPresent(Vector<String> columns);

   //setters

   public boolean isThisColumnPresent(String columnName);

   public boolean isThisTablePresent(String tableName);

   /**
    * returns true if this is not a leaf data node and is composed for
    * children nodes(hence a virtual node)
    * 
    * @return
    */
   public boolean isVirtual();

   public void setVirtual(boolean isVirtual);

   //can be done away with once we use ColumnDescriptor
   public boolean isColumnAVH(String table, String column);

   /**
    * returns the ColumnDescriptor associated with the passed
    * DataSchemaResource
    * 
    * @param column
    * @return
    */
   public ColumnDescriptor getColumnDescriptor(DataSchemaResource column);

   /**
    * Gets the column descriptor for this column
    * 
    * @param column
    * @return
    */
   //It will loop through all tables and return the descriptor for first table containing this column
   public ColumnDescriptor getColumnDescriptor(String column);
}
