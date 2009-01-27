package org.iastate.ailab.qengine.core.util;

import java.util.HashSet;

import org.iastate.ailab.qengine.core.reasoners.interfaces.Axiom;

public interface OntologyLoader {

   public HashSet<Axiom> loadOntology(String attrs);

   public void setOntologyPath(String path);
}
