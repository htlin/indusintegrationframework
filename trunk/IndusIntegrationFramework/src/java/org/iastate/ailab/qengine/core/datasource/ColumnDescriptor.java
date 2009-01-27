package org.iastate.ailab.qengine.core.datasource;

import java.net.URI;
import java.util.Set;
import java.util.Vector;

import org.iastate.ailab.qengine.core.exceptions.ConfigurationException;

public interface ColumnDescriptor {

   /**
    * Returns the value associated with the particular key for this column.
    * As an example the base URI to use to convert the value of a column to
    * an URI if the particular column has an AVH associated with it
    * 
    * @param key
    * @return
    */
   public String getProperty(String key);

   /**
    * Returns the ColumnName
    * 
    * @return
    */
   public String getColumnName();

   /**
    * Checks whether a particular ontology is an attribute value hierarchy
    * 
    * @return
    */
   public boolean isAVH();

   /**
    * It is return the URI associated with the Ontology associated with the
    * Column
    * 
    * @return <ul>
    * <li>URI associated with the Column if it is an AVH
    * <li>null if column is not an AVH,
    * </ul>
    */
   public URI getColumnOntologyURI();

   //setters
   public void setColumnName(String columnName);

   public void setColumnOntologyURI(URI classID);

   /**
    * Sets the current Attribute as AVH as indicated by passed parameter
    * 
    * @param isAVH
    */
   public void setAVH(boolean isAVH);

   public void setProperty(String key, String value);

   /**
    * Returns the set of concepts that are associated with the columns
    * associated with the ontology
    * 
    * @return
    */
   public Set<URI> getPossibleValues();

   /**
    * Gets the possible values for this column as stored in DB. Does this
    * by removing the "base" from the URI associated with concepts
    * associated with the columns as possible values
    * 
    * @return
    * @throws ConfigurationException
    */
   Vector<String> getPossibleValuesAsinDB() throws ConfigurationException;

   //TODO: Extend this to include what type it is (Var char etc)
}
