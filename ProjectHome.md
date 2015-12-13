## A Framework to support Integration Of Heterogeneous Ontology Extended Data Sources. ##
###  ###
A framework that employs ontologies, inter-ontology mappings and conversion functions, to enable a user or an  application to view a collection of physically distributed, autonomous, semantically heterogeneous data sources  as though they were a collection of tables structured according to an ontology supplied by the user.  In the framework the individual data sources can be Ontology Extended Data Sources and the system is able to answer  queries that include  ontological constructs such as equivalent class, superclass and subclass respectively. The  user poses the queries in SQL like syntax (INDUS SQL) which overloads the equalto, greater than and less than  symbols ("=", “<” and “>”) to imply equivalent class, subclass and superclass respectively. Additionally the system can be configured to imply “=” in INDUS SQL as a IS-A relationship existing in the ontology. The system  uses a reasoner to handle queries with ontological constructs. The current system can be configured to use  Pellet (http://clarkparsia.com/pellet ) for the reasoning task (in addition to a custom reasoner developed in our group).

The framework is extensible to use multiple ontology formats (OWL and custom format supported), datsource types (e.g. RDBMS , ARFF files ) and reasoners (Pellet and internal reasoner supported) .

The framework in conjunction  with Indus Learning framework (http://code.google.com/p/induslearningframework/) allows for knowledge acquisition from semantically disparate data sources.
###  ###
# Using The System #
Download the zip file from the downloads. The Zip contains the source, the Indus Integration Framework jar (e.g. iif\_0.0.1.jar),  the jars required by the Indus Integration Framework jar and some sample examples. In addition the extracted zip contains   a user's guide (INDUS\_Guide.pdf)  that describes  how to use the system a run  including running a sample example (Note: Please refer to the INDUS Integration Framework section of the  documentation). In addition the documentation also describes as how the integration framework can be used by  another application (Note: Please refer to Indus Learning Framework section of the documentation and the  subsection titled, Use  with INDUS Integration Framework) .

Note: The user's can easily build the system using the source provided and the included build.xml. The source code includes multiple examples and JUNIT tests to run the examples.

##  ##
**Remark** : The Guide [User Guide](UserGuide_IndusIntegrationFramework.md) is deprecated. Users should refer the INDUS\_Guide.pdf includes in the zip file available for download
##  ##
