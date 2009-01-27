package org.iastate.ailab.qengine.core.datasource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.iastate.ailab.qengine.core.Init;
import org.iastate.ailab.qengine.core.exceptions.ConfigurationException;

/**
 * @author neeraj
 */
public class DefaultColumnDescriptorImpl implements ColumnDescriptor {

   String columnName;

   Map<String, String> properties = new HashMap<String, String>();

   boolean isAVH = false;

   /* URI of the ontology associated with this column(if AVH) */
   URI columnOntologyURI;

   /*
    * @see
    * org.iastate.ailab.qengine.core.datasource.ColumnDescriptor#getColumnName
    * ()
    */
   public String getColumnName() {
      return columnName;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.iastate.ailab.qengine.core.datasource.ColumnDescriptor#setColumnName
    * ()
    */
   public void setColumnName(String columnName) {
      this.columnName = columnName;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.iastate.ailab.qengine.core.datasource.ColumnDescriptor#getProperty
    * (java.lang.String)
    */
   public String getProperty(String key) {
      return properties.get(key);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.iastate.ailab.qengine.core.datasource.ColumnDescriptor#isAVH()
    */
   public boolean isAVH() {
      return isAVH;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.iastate.ailab.qengine.core.datasource.ColumnDescriptor#setAVH
    * (boolean)
    */
   public void setAVH(boolean isAVH) {
      this.isAVH = isAVH;
   }

   public URI getColumnOntologyURI() {
      return columnOntologyURI;
   }

   public void setColumnOntologyURI(URI classID) {
      this.columnOntologyURI = classID;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.iastate.ailab.qengine.core.datasource.ColumnDescriptor#setProperty
    * (java.lang.String, java.lang.String)
    */
   public void setProperty(String key, String value) {
      properties.put(key, value);
   }

   @Override
   public String toString() {
      return columnOntologyURI + " - " + columnName;
   }

   @Override
   public Set<URI> getPossibleValues() throws ConfigurationException {
      if (isAVH) {
         return Init._this().getViewContext().getOntologyStore().getOntology(
               columnOntologyURI).getAllConcepts();
      } else {
         throw new ConfigurationException("The attribute " + columnName
               + "  is not an AVH.  Cannot call get possible values");

      }
   }

   @Override
   public java.util.Vector<String> getPossibleValuesAsinDB()
         throws ConfigurationException {
      String base = getProperty("base");
      int baseLength = base.length();
      Vector<String> dbValues = new Vector<String>();
      Set<URI> values = getPossibleValues();
      String temp;
      for (URI value : values) {
         temp = value.toString().substring(baseLength);
         dbValues.add(temp);
      }
      return dbValues;

   }

}