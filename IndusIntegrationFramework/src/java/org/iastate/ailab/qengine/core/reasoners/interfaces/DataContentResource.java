package org.iastate.ailab.qengine.core.reasoners.interfaces;

/**
 * This class associates a Specific Value (from an associated Ontology) to
 * an Attribute (e.g Column in a Table)
 * 
 * @author neeraj
 */
/*
 * TODO unused classes - These two interface/class pairs are not used
 * outside of each other: (DataContentResource, DataContentResourceImpl)
 * and (DataContent, DataContentImpl).
 */
public interface DataContentResource {
   //DataSchemaResource theResource; //Identifies the Attribute
   //DataContent        value;      //Associate Value with

   public DataSchemaResource getDataSchemaResource();

   public DataContent getValue();

   public void setValue(DataContent v);

   public void setDataSchemaResource(DataSchemaResource r);
}