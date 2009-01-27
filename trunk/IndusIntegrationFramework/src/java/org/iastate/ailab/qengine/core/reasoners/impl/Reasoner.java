package org.iastate.ailab.qengine.core.reasoners.impl;

import java.net.URI;
import java.util.Set;

/**
 * The reasoner returns a particular relationship(subclass, superclass,
 * equivalent class) for a particular class/concept in the user view data as
 * mapped to the target Data Source
 * 
 * @author neeraj
 */
public interface Reasoner {

   /**
    * @param userViewOntologyURI A Handle to get access to the ontology
    * associated with the attributed in userView
    * @param classID The concept/dataContent in the user view for which to
    * find the subClass
    * @param dataSourceOntologyURI A handle to get access to the ontology
    * associated with the attribute in the data source view
    * @param targetDataSource The target data source in which to compute
    * the subclass. This along with the userViewOntologyURI allows to index
    * the mappings between user view and data source view
    * @return
    */
   public Set<URI> getSubClass(URI userViewOntologyURI, URI classID,
         URI dataSourceOntologyURI, String targetDataSource);

   /**
    * @param userViewOntologyURI A Handle to get access to the ontology
    * associated with the attributed in userView
    * @param classID The concept/dataContent in the user view for which to
    * find the superClass
    * @param dataSourceOntologyURI A handle to get access to the ontology
    * associated with the attribute in the data source view
    * @param targetDataSource The target data source in which to compute
    * the subclass. This along with the userViewOntologyURI allows to index
    * the mappings between user view and data source view
    * @return
    */
   public Set<URI> getSuperClass(URI userViewOntologyURI, URI classID,
         URI dataSourceOntologyURI, String targetDataSource);

   /**
    * @param userViewOntologyURI A Handle to get access to the ontology
    * associated with the attributed in userView
    * @param classID The concept/dataContent in the user view for which to
    * find the equivalnetClass
    * @param dataSourceOntologyURI A handle to get access to the ontology
    * associated with the attribute in the data source view
    * @param targetDataSource The target data source in which to compute
    * the subclass. This along with the userViewOntologyURI allows to index
    * the mappings between user view and data source view
    * @return
    */
   public Set<URI> getEquivalnetClass(URI userViewOntologyURI, URI classID,
         URI dataSourceOntologyURI, String targetDataSource);
}
