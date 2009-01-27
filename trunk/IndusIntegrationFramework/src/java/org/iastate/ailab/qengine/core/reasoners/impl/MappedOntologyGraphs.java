package org.iastate.ailab.qengine.core.reasoners.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.iastate.ailab.qengine.core.reasoners.interfaces.Axiom;

/**
 * @author neeraj
 */
public class MappedOntologyGraphs {

   class MappedOntologyGraphsForDataSource {

      /**
       * This stores the mapping graph for various attributes within a
       * dataSource The key is concatenation of the userViewOntologyURI and
       * dataSourceOntologyURI (userViewOntologyURI + "->" +
       * dataSourceOntologyURI) and hence uniquely identifies the mapping
       * from user view attributes to an attribute for a data Source
       * Remark: It May not be unique across data Source as the same
       * ontolgies may be used but with different mappings. Hence it must
       * be stored per data source
       */
      Map<String, MappedOntologyNode> mappedGraphForAttributes = new HashMap<String, MappedOntologyNode>();

      /*
       * identifies the target data source for which the mappings are
       * stored
       */
      String dataSourceName;

      MappedOntologyGraphsForDataSource(String dataSourceName) {
         this.dataSourceName = dataSourceName;
      }

      boolean graphExists(String id) {
         return mappedGraphForAttributes.containsKey(id);
      }

      /**
       * In the Graphs for mapping between various ontologies in the
       * datasource (and user view) gets access to the mapping graph
       * identified by id
       * 
       * @param id
       * @return
       */
      public MappedOntologyNode getMappedOntologyGraph(String id) {
         return mappedGraphForAttributes.get(id);
      }

      void setMappingGraph(String id, MappedOntologyNode graph) {
         mappedGraphForAttributes.put(id, graph);
         //TODO: Throw an  Exception if this id has already been stored
         //You should only do this once
      }
   }

   /**
    * Stores a Map from the dataSource(identified by it s name) to the
    * MappedOntologyGraphs For DataSource Remark; A data source will one
    * graph each for the attributes mapped between user view and data
    * source view If multiple data sources use same ontology they must have
    * same mappings within a data source
    */
   Map<String, MappedOntologyGraphsForDataSource> dataSourceToMappedOntologyGraph = new HashMap<String, MappedOntologyGraphsForDataSource>();

   //Singleton pattern
   private static MappedOntologyGraphs _this = null;

   public static MappedOntologyGraphs _this() {
      if (_this == null) {
         _this = new MappedOntologyGraphs();
      }
      return _this;
   }

   public MappedOntologyGraphsForDataSource getMappedOntologyGraphsForDataSource(
         String dataSourceName) {
      return dataSourceToMappedOntologyGraph.get(dataSourceName);
   }

   public void build(Set<Axiom> userViewAxioms, Set<Axiom> dataSourceAxioms,
         Set<Axiom> mappingAxioms, String dataSourceName,
         URI userViewOntologyURI, URI dataSourceOntologyURI) {

      /* build a key which uniquely identifies the mapping */
      String mappingIdentifier = userViewOntologyURI + "->"
            + dataSourceOntologyURI;

      /*
       * get All the mapped graphs for this data source and see if we have
       * already handled it
       */

      MappedOntologyGraphsForDataSource mappedGraphsForDataSource = dataSourceToMappedOntologyGraph
            .get(dataSourceName);

      //see if this mapping has already been build
      if (mappedGraphsForDataSource != null
            && mappedGraphsForDataSource.graphExists(mappingIdentifier)) {
         //we have seen this before and built it. No need to do again
         return;
      }

      if (mappedGraphsForDataSource == null) {
         //first time mapping for this data source is being see, hence create a a new instance
         //This will NOT be the case when we may had mappings between other attributes(for this data source) before this
         mappedGraphsForDataSource = new MappedOntologyGraphsForDataSource(
               dataSourceName);
      }

      MappedOntologyNode mappedGraph = new MappedOntologyNode();
      mappedGraph.dataSourceName = dataSourceName;
      mappedGraph.build(userViewAxioms, dataSourceAxioms, mappingAxioms,
            userViewOntologyURI, dataSourceOntologyURI);

      mappedGraphsForDataSource.setMappingGraph(mappingIdentifier, mappedGraph);

      dataSourceToMappedOntologyGraph.put(dataSourceName,
            mappedGraphsForDataSource); //store it

      //display it
      //mappedGraph.displayRoot();
      mappedGraph.getRootNodes(); //internally displays root nodes
   }
}
