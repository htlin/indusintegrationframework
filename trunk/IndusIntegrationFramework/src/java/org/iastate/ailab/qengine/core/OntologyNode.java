package org.iastate.ailab.qengine.core;

import java.util.Enumeration;
import java.util.Dictionary;
import java.util.Hashtable;

import org.iastate.ailab.qengine.core.reasoners.impl.DFSResult;

//TODO; Delete, no longer used
@Deprecated
public class OntologyNode {
   private Dictionary<String, OntologyNode> superclass = new Hashtable<String, OntologyNode>();

   private Dictionary<String, OntologyNode> subclass = new Hashtable<String, OntologyNode>();

   private Dictionary<String, OntologyNode> equivalentclass = new Hashtable<String, OntologyNode>();

   private String name;

   private String source;

   public OntologyNode(String name) {
      this.name = name;
      superclass = new Hashtable<String, OntologyNode>();
      subclass = new Hashtable<String, OntologyNode>();
   }

   public void addToSuperClass(OntologyNode o) {
      if (superclass.get(o.getName()) == null) {
         superclass.put(o.getName(), o);
      }
   }

   public void addToSubClass(OntologyNode o) {
      if (subclass.get(o.getName()) == null) {
         subclass.put(o.getName(), o);
      }
   }

   public void addToEquivalentClass(OntologyNode o) {
      if (equivalentclass.get(o.getName()) == null) {
         equivalentclass.put(o.getName(), o);
      }
   }

   public DFSResult getSubClass(DFSResult result) {
      result.setProcessed(this);
      // Refactor this code.
      // necessary for "equivalent" traversal
      if (result.numprocessedNode() > 1) {
         for (Enumeration<String> e = equivalentclass.keys(); e
               .hasMoreElements();) {
            OntologyNode n = equivalentclass.get(e.nextElement());
            if (!result.isProcessed(n)) {
               result.addResult(n);
               result = n.getSubClass(result);
            }
         }
      }
      // ===

      for (Enumeration<String> e = subclass.keys(); e.hasMoreElements();) {
         OntologyNode n = subclass.get(e.nextElement());
         if (!result.isProcessed(n)) {
            result.addResult(n);
            result = n.getSubClass(result);
         }
      }
      return result;
   }

   public DFSResult getSuperClass(DFSResult result) {
      result.setProcessed(this);
      // Refactor this code.
      // necessary for "equivalent" traversal
      if (result.numprocessedNode() > 1) {
         for (Enumeration<String> e = equivalentclass.keys(); e
               .hasMoreElements();) {
            OntologyNode n = equivalentclass.get(e.nextElement());
            if (!result.isProcessed(n)) {
               result.addResult(n);
               result = n.getSuperClass(result);
            }
         }
      }
      // ===

      for (Enumeration<String> e = superclass.keys(); e.hasMoreElements();) {
         OntologyNode n = superclass.get(e.nextElement());
         if (!result.isProcessed(n)) {
            result.addResult(n);
            result = n.getSuperClass(result);
         }
      }
      return result;
   }

   public DFSResult getEquivalentClass(DFSResult result) {
      result.setProcessed(this);
      for (Enumeration<String> e = equivalentclass.keys(); e.hasMoreElements();) {
         OntologyNode n = equivalentclass.get(e.nextElement());
         if (!result.isProcessed(n)) {
            result.addResult(n);
            result = n.getEquivalentClass(result);
         }
      }
      return result;
   }

   public String getName() {
      return this.name;
   }

   public void setSource(String s) {
      this.source = s;
   }

   public String getSource() {
      return source;
   }
}
