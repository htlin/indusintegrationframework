package org.iastate.ailab.qengine.core.reasoners.impl;

import java.net.URI;
import java.util.Set;

import org.iastate.ailab.qengine.core.reasoners.interfaces.Axiom;
import org.iastate.ailab.qengine.core.util.Displayable;

public interface OntologyI extends Displayable {

   /**
    * Returns the <code>URI</code> used to uniquely identify
    * <code>OntologyI</code>.
    * 
    * @return
    */
   public URI getOntologyURI();

   /**
    * Sets the <code>URI</code> of this <code>OntologyI</code> to the
    * specified URI.
    * 
    * @param newURI the new <code>URI</code>
    */
   public void setOntologyURI(URI newURI);

   /**
    * Adds the specified class to this <code>OntologyI</code>.
    * 
    * @param newClass the class to be added to this <code>OntologyI</code>
    */
   public void addClass(URI newClass);

   /**
    * Add the specified <code>Axiom</code> to this <code>OntologyI</code>.
    * 
    * @param newAxiom the <code>Axiom</code> to be added to this
    * <code>OntologyI</code>
    */
   public void addAxiom(Axiom newAxiom);

   /**
    * Returns the Axioms of this <code>OntologyI</code>.
    * 
    * @return the <code>Set</code> of all <code>Axioms</code> for this
    * <code>OntologyI</code>.
    */
   public Set<Axiom> getAxioms();

   /**
    * Returns all the concepts of this <code>OntologyI</code>.
    * 
    * @return all the concepts of this <code>OntologyI</code>
    */
   public Set<URI> getAllConcepts();

   /**
    * Determines whether the class/concept is part of this
    * <code>OntologyI</code>.
    * 
    * @param classId the <code>URI</code> to check for existence in this
    * <code>OntologyI</code>.
    * 
    * @return <code>true</code> if the specified <code>URI</code> is
    * part of this <code>OntologyI</code>; <code>false</code>
    * otherwise.
    */
   public boolean containsClass(URI classId);

   /**
    * Returns a value associated with a property for this
    * <code>OntologyI</code>. As an illustration this may contain
    * description of this <code>OntologyI</code>, creator etc.
    * 
    * @param propertyName
    * @return
    */
   public String getOntologyProperty(String propertyName);
}
