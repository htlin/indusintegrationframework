package org.iastate.ailab.qengine.core.reasoners.impl;

import java.util.HashSet;
import java.util.Set;

import org.iastate.ailab.qengine.core.OntologyNode;

public class DFSResult {

   private Set<OntologyNode> result = new HashSet<OntologyNode>();

   private Set<OntologyNode> processedNodes = new HashSet<OntologyNode>();

   public void addResult(OntologyNode r) {
      if (!result.contains(r)) {
         result.add(r);
      }
   }

   public void setProcessed(OntologyNode node) {
      processedNodes.add(node);
   }

   public boolean isProcessed(OntologyNode node) {
      return processedNodes.contains(node);
   }

   public Set<OntologyNode> getResult() {
      return result;
   }

   public int numprocessedNode() {
      return processedNodes.size();
   }
}
