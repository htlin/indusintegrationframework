package org.iastate.ailab.qengine.core;

import java.util.Set;
import java.util.Vector;

import org.iastate.ailab.qengine.core.exceptions.EngineException;
import org.iastate.ailab.qengine.core.reasoners.interfaces.Axiom;

//TODO: Deprecated
//TODO: Name the interface accordingly(not Ontology) say MappedOntoView
//TODO: Move to Reasoner package
@Deprecated
public interface Ontology {

   public void setTypeName(String typeName);

   public void setSubTypeOf(String subTypeOf);

   public void setOrderName(String orderName);

   public Vector<OntologyNode> getSuperClass(String attributeValue);

   public Vector<OntologyNode> getSubClass(String attributeValue);

   public Vector<OntologyNode> getEquivalentClass(String attributeValue);

   public String getTypeName();

   public String getSubTypeOf();

   public String getOrderName();

   public void setDataSource(String s);

   public String getDataSource();

   public void build(Set<Axiom> localAxioms, Set<Axiom> userViewAxioms,
         Set<Axiom> ontoMapAxioms) throws EngineException;
}
