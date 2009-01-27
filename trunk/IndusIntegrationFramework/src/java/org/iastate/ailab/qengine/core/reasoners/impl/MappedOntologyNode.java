package org.iastate.ailab.qengine.core.reasoners.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.Init;
import org.iastate.ailab.qengine.core.exceptions.InvalidMappingException;
import org.iastate.ailab.qengine.core.reasoners.interfaces.Axiom;

/**
 * @author neeraj
 */
public class MappedOntologyNode {

   private static final Logger logger = Logger
         .getLogger(MappedOntologyNode.class);

   public enum NodeType {
      userviewNode, dataSourceNode
   }

   /* The URI used to identify this node */
   URI nodeId;

   /* The target dataSoure to which this mappedOntologyGraph is associated */
   String dataSourceName;

   /**
    * Whether this Node was userView Axioms or data Source Axioms
    */
   NodeType nodeType;

   /*
    * parents of the current Node. Includes nodes both in data Source and
    * the User View
    */
   Set<MappedOntologyNode> parents = new HashSet<MappedOntologyNode>();

   /**
    * Peers of the current node. The peers form when a node in user view is
    * equivalent to node in the data source
    */
   Set<MappedOntologyNode> peers = new HashSet<MappedOntologyNode>();

   /*
    * children of the current Node. Includes nodes both in data Source and
    * the User View
    */
   Set<MappedOntologyNode> children = new HashSet<MappedOntologyNode>();

   boolean subClassCalculated = false;

   boolean superClassCalculated = false;

   boolean equivalentClassCalculated = false;

   /*
    * Store the URI of the subClasses of the current class. Only
    * targetDataSource classes are stored and not the ones in the userView
    */
   Set<URI> subClassConceptIDs = new HashSet<URI>();

   Set<URI> superClassConceptIDs = new HashSet<URI>();

   Set<URI> equivalentClassIDs = new HashSet<URI>();

   /**
    * Provides a reference to the node in the MappedOntologyGraph based
    * concept in user view (identified by URI)
    * 
    * Remark: Multiple ontologies (associated with different attributes)
    * for a data source may be stored in this map as each class will have a
    * different URI
    */

   private Map<URI, MappedOntologyNode> ref = new HashMap<URI, MappedOntologyNode>();

   public Set<MappedOntologyNode> getRootNodes() {
      //should be called after build
      Set<MappedOntologyNode> roots = new HashSet<MappedOntologyNode>();
      Iterator<MappedOntologyNode> it = ref.values().iterator();
      MappedOntologyNode curr = null;
      while (it.hasNext()) {
         curr = it.next();
         if (curr.parents.isEmpty()) {
            roots.add(curr); //roots are the ones who have no parent. These are the top concepts
            logger.debug("ROOT=" + curr.nodeId);
         }
      }
      return roots;
   }

   public void displayRoot() {
      Set<MappedOntologyNode> roots = getRootNodes();
      Iterator<MappedOntologyNode> rootIterator = roots.iterator();
      while (rootIterator.hasNext()) {
         rootIterator.next().display("");
      }
   }

   private void display(String indent) {
      logger.trace(indent + "ID=" + nodeId);
      //peers keep same indentation
      Iterator<MappedOntologyNode> it = peers.iterator();
      while (it.hasNext()) {
         it.next().display(indent);
      }
      indent += "\t";
      //children, increased indentation
      it = children.iterator();
      while (it.hasNext()) {
         it.next().display(indent);
      }
   }

   public void setGraphDataSourceName(String dataSourceName) {
      this.dataSourceName = dataSourceName;
   }

   public void build(Set<Axiom> userViewAxioms, Set<Axiom> dataSourceAxioms,
         Set<Axiom> mappingAxioms, URI userViewOntologyURI,
         URI dataSourceOntologyURI) {

      //TODO should these comments be removed?
      //System.out.println("USER VIEW AXIOMS");
      //System.out.println("+++++++++++++++++++++++");
      buildGraph(userViewAxioms, NodeType.userviewNode);

      //System.out.println("DATA SOURCE  AXIOMS");
      //System.out.println("+++++++++++++++++++++++");
      buildGraph(dataSourceAxioms, NodeType.dataSourceNode);

      //System.out.println("MAPPING ");
      //System.out.println("+++++++++++++++++++++++");
      addMappings(mappingAxioms, userViewOntologyURI, dataSourceOntologyURI);
   }

   private void buildGraph(Set<Axiom> axioms, NodeType nType) {

      if (axioms == null)
         return;

      Iterator<Axiom> it;
      MappedOntologyNode from;
      MappedOntologyNode to;
      AVHRole role;
      Axiom curr;
      it = axioms.iterator();
      while (it.hasNext()) {
         curr = it.next();
         from = createNode(curr.getFromNode(), nType);
         to = createNode(curr.getToNode(), nType);
         role = curr.getRole();
         linkNodes(from, to, role);
      }
   }

   private void addMappings(Set<Axiom> mappings, URI userViewOntologyURI,
         URI dataSourceOntologyURI) {

      if (mappings == null)
         return;

      Iterator<Axiom> it;
      MappedOntologyNode from;
      MappedOntologyNode to;
      AVHRole role;
      Axiom curr;
      it = mappings.iterator();
      while (it.hasNext()) {
         curr = it.next();
         from = ref.get(curr.getFromNode());
         to = ref.get(curr.getToNode());
         role = curr.getRole();
         if (from == null || to == null) {
            // Handle the case when the nodes may be freely hanging nodes(hence not axioms)
            //Go to the OntoStore to verify the node exists before throwing exception
            if (checkNodesExist(curr, userViewOntologyURI,
                  dataSourceOntologyURI)) {
               //nodes exist, create them and link them
               from = createNode(curr.getFromNode(), NodeType.userviewNode);
               to = createNode(curr.getToNode(), NodeType.dataSourceNode);
               linkNodes(from, to, role); //freely hanging nodes(no children) part of mapping
               logger.trace("Freely hanging nodes part of the mapping->");
               curr.display(System.out);
            } else {
               String message = "ERROR: Mappings between non existent nodes: "
                     + curr;
               logger.warn(message);
               throw new InvalidMappingException(message);
            }
         }
         linkNodes(from, to, role);
      }
   }

   /**
    * Checks whether the nodes that are part of the mapping exist in
    * respective ontologies
    * 
    * @param mapping
    * @param userViewOntologyURI
    * @param dataSourceOntologyURI
    * @return
    */
   private boolean checkNodesExist(Axiom mapping, URI userViewOntologyURI,
         URI dataSourceOntologyURI) {
      boolean exists = false;
      OntologyStore ontStore = Init._this().getViewContext().getOntologyStore();
      URI from = mapping.getFromNode();
      URI to = mapping.getToNode();
      boolean fromExists = ontStore.getOntology(userViewOntologyURI)
            .containsClass(from);
      if (fromExists) {
         //the from Node (userView) exists, check if to Node exists in the dataSource
         exists = ontStore.getOntology(dataSourceOntologyURI).containsClass(to);
      }

      return exists;
   }

   private MappedOntologyNode createNode(URI nodeURI, NodeType nType) {

      if (ref.containsKey(nodeURI)) {
         return ref.get(nodeURI);
      }

      MappedOntologyNode res = new MappedOntologyNode();
      res.nodeId = nodeURI;
      res.nodeType = nType;

      //store it
      ref.put(nodeURI, res);

      return res;
   }

   private void linkNodes(MappedOntologyNode src, MappedOntologyNode target,
         AVHRole mappingType) {

      logger.trace("Linking:  " + src.nodeId + "--> " + mappingType);
      logger.trace("\t \t " + target.nodeId);

      if (mappingType.equals(AVHRole.SUPER_CLASS)) {
         //relationship is supercalss, so the target is a child/subclass
         //The mappings are always stored userView AVHRole dataSourceView	
         src.children.add(target); //Adding to a HashSet so no need to worry about duplicates
         target.parents.add(src);
      } else if (mappingType.equals(AVHRole.EQUIVALENT_CLASS)) {
         src.peers.add(target);
      } else if (mappingType.equals(AVHRole.SUB_CLASS)) {
         //subclass, the target should be superclass
         src.parents.add(target);
         target.children.add(src);
      } else {
         throw new IllegalArgumentException("Undefined AVHRole in mapping: "
               + mappingType);
      }
   }

   public Set<URI> getSubClass(URI classId) {
      Set<URI> result = new HashSet<URI>();
      result = ref.get(classId).getSubClass(0, result); //start at depth 0 indicating this is the first node
      return result;
   }

   public Set<URI> getSuperClass(URI classId) {
      Set<URI> result = new HashSet<URI>();
      result = ref.get(classId).getSuperClass(0, result); //start at height 0 indicating this is the first node
      return result;
   }

   /*
    * This is not required since equivalent class is explictly calculated
    * in the Reasoner public HashSet<URI> getEquivalentClass(URI classId) { }
    */

   private Set<URI> getSubClass(int depth, Set<URI> result) {

      if (this.subClassCalculated)
         return subClassConceptIDs;
      //calculate the subClass;

      Iterator<MappedOntologyNode> it = null;
      if (nodeType.equals(NodeType.dataSourceNode)) {
         //Remark: For the  dataSource the equivalent /peer nodes should be empty for AVH and hence not handled
         //handle Children
         it = children.iterator();
         while (it.hasNext()) {
            result = it.next().getSubClass(++depth, result);
         }
         //if depth is > 0, the current node is a valid subclass, add it to the result
         if (depth > 0) {
            result.add(nodeId);
            //Remark: Only the data sources nodes are ever added to the result
         }
      } else if (nodeType.equals(NodeType.userviewNode)) {
         it = children.iterator();
         //Remark: A user view node can have data source nodes as children due to mappings

         while (it.hasNext()) {
            result = it.next().getSubClass(++depth, result);
         }

         /**
          * depth > 0 implies you are below the node for which you wanted
          * subcalss, hence the subclasses as well as equivalent classes of
          * this node should be added to result
          */
         if (depth > 0) {
            it = peers.iterator();
            while (it.hasNext()) {
               result = it.next().getSubClass(depth, result);
            }
         }
      } else {
         //should not happen
      }

      //cache the results
      this.subClassConceptIDs = result;
      this.subClassCalculated = true;

      return result;
   }

   private Set<URI> getSuperClass(int height, Set<URI> result) {

      logger.trace("calculating SuperClass for: " + this.nodeId
            + " with height=" + height);

      if (this.superClassCalculated)
         return superClassConceptIDs;
      //calculate the superClass;

      Iterator<MappedOntologyNode> it = null;
      MappedOntologyNode temp = null;
      if (nodeType.equals(NodeType.dataSourceNode)) {
         //Remark: For the  dataSource the equivalent /peer nodes should be empty for AVH and hence not handled
         //handle parents
         it = parents.iterator();
         while (it.hasNext()) {
            temp = it.next();
            result = temp.getSuperClass(++height, result);
         }
         //if height is > 0, the current node is a valid superclass, add it to the result
         if (height > 0) {
            result.add(nodeId);
            //Remark: Only the data sources nodes are ever added to the result
         }
      } else if (nodeType.equals(NodeType.userviewNode)) {
         it = parents.iterator();
         //Remark: A user view node can have data source parent as children due to mappings

         while (it.hasNext()) {
            temp = it.next();
            result = temp.getSuperClass(++height, result);
         }
         /**
          * height > 0 implies you are above the node for which you wanted
          * supercalss, hence the superclasses as well as equivalent
          * classes of this node should be added to result
          */
         if (height > 0) {
            it = peers.iterator();
            while (it.hasNext()) {
               result = it.next().getSuperClass(height, result);
            }
         }
      } else {
         //should not happen
         //TODO be more specific for the logged message
         logger.warn("Should never happen");
      }

      //cache the results
      this.superClassConceptIDs = result;
      this.superClassCalculated = true;

      return result;
   }
}
