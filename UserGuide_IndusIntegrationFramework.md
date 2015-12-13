# User Guide for INDUS Integration Framework #

#### _IOWA STATE UNIVERSITY_ ####


### Contents ###

#### About INDUS ####
  * What is INDUS?
  * Where can I download/get INDUS?
  * How do/can I contribute?
  * Vocabulary lesson
  * Common Problems
  * Using INDUS
  * Environment Setup
  * Universal Configuration File
    1. indus.conf
  * User Files
    1. The User View File
    1. Data Source Tree File (DTreeFile)
    1. Schema Map
    1. Ontology Map
    1. Data Source Descriptor
    1. OntLoc File
  * Running INDUS
  * Setup Tutorial
  * Example Queries
#### Credits ####



## About INDUS ##

## What is INDUS? ##

INDUS is a [research project](http://www.cild.iastate.edu/~honavar/nsfitr02.html) under the direction of [Dr. Vasant Honavar](http://www.cs.iastate.edu/~honavar/homepage.html),a professor in the [Computer Science Department Computer Science Department](http://www.cs.iastate.edu/) at [Iowa State University](http://www.iastate.edu/). INDUS stands for Intelligent Data Understanding System.  It allows users to virtually combine data that is physically spread across many data sources and run queries on that data as though it existed in a single database.  The individual data sources can be Ontology Extended Data Sources and  the system is able to understand  queries using ontological  constructs such as superclass and subclass respectively. The user poses the queries in SQL like syntax (INDUS SQL)  which overloads the left and right angle brackets brackets (“<”  and “>”) to imply subclass and superclass respectively.


## Where can I download/get INDUS? ##

INDUS is an open source code and is available as [here](http://code.google.com/p/indusintegrationframework/). The source is provided as is without any warranty.

## How do/can I contribute? ##

You certainly may.  Contact [Neeraj Koul](http://www.cs.iastate.edu/~neeraj/) at neeraj@cs.iastate.edu for further direction.


## Using INDUS ##

### Environment Setup ###

INDUS requires a database to store temporary results (the results from queries to real data sources).  The information (name, location, and type of database) must be stated in the indus.conf file (described below).  The temporary results are deleted when they are no longer needed.

### INDUS Configuration File ###

This is the main configuration file  that sets up the  INDUS framework.  This file has to be named **indus.conf**.  The   possible key/value pairs that can exist in this file are


| **Key** | **Value** | **Comments** |
|:--------|:----------|:-------------|
| driver

&lt;rdbms&gt;

 | 

<driver\_class>

 | Examples of 

&lt;rdbms&gt;

:mysql, postgress, oracle Examples of 

<driver\_class>

:com.mysql.jdbc.Driver,org.postgresql. Driver The current version has been tested with mysql |
| dbname\_indus | 

<DB\_name>

 | Name of the database in which INDUS will store temporary results |
| hostname\_indus | 

<URL\_to\_DB>

 | Location of a database in which INDUS will store temporary results |
| dbtype\_indus | 

<DB\_type>

 | Type of the database in which INDUS will store temporary results. It must match 

&lt;rdbms&gt;

 for one of the driver keys provided above. |
| 

<DB\_name>

 | 

&lt;username&gt;

;

&lt;password&gt;

 | A user name (with read access) and password must be provided for every database |
| 

<view\_filename>

 | <view.txt> | Path to the file  containing configuration files for view. |
| 

<class\_name\_key>

 | 

<class\_name>

 | The possible strings for **class\_name\_key** can be found in:org.iastate.ailab.qengine.core.factories.solution.SolutionCreator. The value of **class\_name** should be the fully qualified class name of a class that implements the same interface as **class\_name\_key**. |
| world\_semantics | 

<open\_or\_closed>

 | Future use. Currently Ignored. |


## User Configuration Files ##

These  includes  configurations files to describe to data sources, ontologies (if any) associated with the attributes in the data sources and schema and ontology mappings to resolve schema and data content heterogeneity. Once these files have been set, the user can query over the disparate data sources as if it is a single datasource


## The UserView File ##

This file gives a name to a user view and provides the  paths to the files that describe the DTree, SchemaMap, and  the  Ontology Map associated with this user view (See the appendix for an example of a user view file.).  The structure of  the UserView File is a It is also a Java Properties file and the   the possible key/value pairs that can exist in this file:


| **Key** | **Value** | **Comments** |
|:--------|:----------|:-------------|
|UserViewName|

<name\_of\_userview>

|Name of this userview|
|DTreeFile|

<path\_to\_DTreeFile>

| Path to the Data Agrregation Tree File|
|SchemaMap|

<path\_to\_schema\_map>

| Path to the Schema Map File|
|OntologyMap|

<path\_to\_ontology\_map>

|	Path to the Ontology Map File|


The Data Aggregation Tree, Schema Map, and Ontology Map files are described below.

## Data AggregationTree File (DTree File) ##

The DTreeFile describes the structure of the data aggregation tree.  It is an XML file with following structure ( See the Appendix for an example of a DTreeFile)



| **Elements** | **Attributes** | **Subelements** | **Comments** |
|:-------------|:---------------|:----------------|:-------------|
|Tree          |	               |	Node            |	This is the root element.|
|node          |name, fragmentationType|	dataSourceDescriptor, children, dbInfo|	If the node is a leaf node, it must contain the subelements _dbInfo_ and _dataSourceDescriptor_, otherwise it must contain the subelement _children_ except for the root node, which always has the element _dataSourceDescriptor_.|
|dataSourceDescriptor|	File           |                 |              |
|children      |	joinTable, joincol|	node            |	There must be one or more node subelements.  When the fragmentation of the subelement nodes are vertical, the children element must contain the attributes joinTable and joincol.|
|dbInfo        |	               |host, type       |	             |


Description of attributes:

| **Element: Attribute**	| **Value** | **Comments** |
|:-----------------------|:----------|:-------------|
|node: name              |	

&lt;unrestricted&gt;

|	This is used to uniquely identify an element node.  This name must match a name attribute of the datasource element in the Data Source Descriptor file.|
|node: fragmentationType |	

<horizontal\_or\_vertical>

|	The fragmentation type of the element node. The value must be of the following: horizontal, vertical|
|dataSourceDescriptor: file|	

<path\_to\_desc>

|	Relative path to the file describing the data source of the superelement node.|
|children: joinTable     |	

<table\_name>

|	The name of the table containing the joincol attribute for this children element.|
|children: joincol       |	

<column\_name>

|	The name of the column on which the data will be joined.  If the value of joincol is the same for two fragmented pieces of data, then each fragment is referring to the same data as a whole.|
|dbInfo: host            |	

<URL\_of\_DB>

|	Location of database described by the superelement node.|
|dbInfo: type            |	

<DB\_type>

|	Type of the database described by the superelement node.  It must match  

&lt;rdbms&gt;

 for one of the driver keys provided above.|


## Schema Map ##

The Schema Map is a Java Properties file where the keys are actual table and column names that exist in real databases and their values are the table and column names from the user view (root DTree node) to which they should be mapped.

See the appendix for an example of a Schema Map file.

## Ontology Map ##

The Ontology Map is a Java Properties file that will map the names of class ids from the target data source ontologies to the ontology of the user view (root DTree node).

The key/value pairs should follow the following form:


&lt;targetDataSource&gt;

@

&lt;ontologyId&gt;

@

&lt;classId&gt;

 = [EQUAL|SUPER|SUB]@

&lt;ontologyI&gt;

@

&lt;classId&gt;



where the at sign (@) and equals sign (=) are the only literals.  Both 

&lt;ontologyId&gt;

 and 

&lt;classId&gt;

 must be valid URIs unless the 

&lt;classId&gt;

 begins with and underscore (_) in which case the_

&lt;classId&gt;

 is assumed to be relative to the 

&lt;ontologyId&gt;

.  

&lt;targetDataSource&gt;

 must be the value of a name attribute for some node element that is describing a real database.  The value of the key/value pair must begin with EQUAL, SUPER, or SUB, which describes the relation of the key to the value where EQUAL is the equlivance class, SUPER is the super class, and SUB is the sub class.

See the appendix for an example of an Ontology Map file.

## DataSource Descriptor ##

The Data Source Descriptor file is an XML file that describes the tables, columns, and types of data in those columns for either the virtual database that exists at the root node or for actual databases at leaf nodes.  If the attribute  a column is associated with an ontology, it also containts links to the file (or database)  containing the ontology.

See the appendix for an example of a Data Source Descriptor file.

Here is the structure of a Data Source Descriptor file:

| **Elements**	| **Attributes** |	**Subelements** |	**Comments**|
|:-------------|:---------------|:----------------|:------------|
|descriptors   |   		           |descriptor       |	Must have one or more descriptor subelement.|
|descriptor    |	 	             |datasource       |	            |
|datasource	   |name            |	table           |	Must have one or more table subelements.|
|table         |	name           |	column          |	Must have one or more column subelements.|
|column        |	name, type     |	operation       |	If the values in a column are associated with an ontology, then this element must contain a subelement operation.|
|operation     |	type, base, ontology, ontLoc	|                 |	ontLoc points to the file containing the ontology. ontology  is the URI of the ontology associated with the attribute type is restricted  to value AVH base is the Uri to be appended to the values of the attribute to form concepts in the ontology.|


Description of attributes: //TODO finish table

| **Element: Attribute** | **Value** |	**Comments** |
|:-----------------------|:----------|:-------------|
|datasource: name        |	

&lt;unrestricted&gt;

|	This is used to uniquely identify an element datasource.  This name must match a name attribute of the node element in the DTreeFile.|
|table: name             |	

<table\_name>

|	Name of the table|
|column: name            |	

<col\_name>

|	Name of the column|
|column: type            |	

<col\_type>

|	The type of the data in this column.|
|operation: type         |	

<op\_type>

|	Fixed value AVH|
|operation: base         |	

<op\_base>

|	base is the Uri to be appended to the values of the attribute to form concepts in the ontology|
|operation: ontology     |	

<op\_ontology>

|	The  ontology associated with the attribute|
|operation: ontLoc       |	

<path\_to\_ontLoc>

|	The relative path to the file that describes the ontology.|


## OntLoc File ##

The ontLoc file is a file  that contains an  attribute value hierarchy that  can be associated  with an attribute  in the data source. The framework supports a custom format to specify the ontology and can also be configured to read attribute value hierarchies  written in OWL syntax. Developers can add support to attribute value hierarchies in their own custom format by implementing the interface  _org.iastate.ailab.qengine.core.reasoners.impl.OntologyParser .core.reasoners.impl.DefaultOntologyParserImpl_.

The default format for a single ontology is:

Three key/value pairs proceeded by a single semicolon (;). These are for information purposes only

The three key/value pairs are:

| **Key**|	**Value**|	**Comments**|
|:-------|:---------|:------------|
|typename|	

&lt;attributeName&gt;

Type|	attributeName is the column in the table with which ontology is used|
|subTypeOf|	AVH      |	 fixed      |
|ordername|	ISA      |	fixed       |

Following the key/value pairs is an optional alias that would be used to shorten the URIs for each item in the ontology.  The alias is a key/value pair that is preceded by the string “xmlns:” (without quotes).  Of course, the key is the alias and the value is a common portion of the URIs for the items in the ontology.

Finally, the structure of the ontology begins with… followed by a open curly brace ({).  Each line between the curly braces is the URI to an item in the ontology.  If using an alias, use double periods (..) to denote the use of the alias.  The rest of the line should complete the URI for an item of the ontology.  All items in the ontology that are refered to in the Ontology Map file must be present.  After the last URI, there must be a closing curly brace (}).

More than one ontology can be within the same file.

Comments begin with the pound sign (#) or double forward slashes (//) and continue until the end of the line.

See the appendix for an example.


## Running Indus ##

## Dependant Jars ##

Zql.jar -  SQL Parser  (http://www.gibello.com/code/zql/)

meval.jar -  Math Evaluator (http://lts.online.fr/dev/java/math.evaluator/ )

OWL API implementation - (http://owlapi.sourceforge.net/releases.html ), Not Required if you use custom ontology format.

Log4j -  (http://logging.apache.org/log4j/)

MySQL  drivers for JDBC connectivity (http://www.mysql.com/products/connector/ )

Apache Commons Lang ( http://commons.apache.org/lang/ )

The lib package in the source package contains the dependant jars. User’s are required to check for license requirements (if any)

## Command Line ##

Coming soon

## API Integration ##

The following snippet of code indicates how to integrate into your application(once the configuration files have been set as before).
The relevant classes are:

  * org.iastate.ailab.qengine.core.QueryEngine
  * org.iastate.ailab.qengine.core.QueryResult

```
String baseDir; //  contains the directory that points to indus.conf 

baseDir = System.getProperty("user.dir") + File.separator + "config";
QuerEngine engine = new QueryEngine(baseDir); //configure the Engine


String query1 = "select COUNT(*) from EMPLOYEETABLE;";
QueryResult result1 = Engine.execute execute (query1);  //Execute a query
```

## Common Problems ##
//TODO

## Vocabulary lesson ##
**ARFF file
  * ARFF stands for Attribute-Relation File Format.  It was developed bythe       Machine Learning Project at the Department of Computer Science
of The University of Waikato for use with the Weka machine learning software.**	AVH
    * An attribute value hierarchy is… //TODO finish definition
**Closed-World Semantics
    * Under closed-world semantics, it is assumed that "if proposition P cannot be proved True, then assume that P is False”.**	See Open-World Semantics
      * Current Working Directory
    * The current working directory is specific to a process and allows that process to use file paths relative to that directory.
    * See http://en.wikipedia.org/wiki/Working_directory
**Database
    * A database is a structured collection of records or data that is stored in a computer system
    * See http://en.wikipedia.org/wiki/Database
    * Some popular databases are:
    * DB2 (by IBM)
      * MySQL (by MySQL AB but recently bought by Sun Microsystems)
    * Oracle (by Oracle Corporation)**	Data Source
    * A data source is an abstract view of what is normally viewed as a database.  Currently, the two instances of a data source are a database and ARFF file.
**Data Source Tree (DTree)
    * A data source tree is a tree where the nodes are data sources.  More specifically, the leaves are actual physical data sources and non-leaves are virtual data sources created by INDUS.  The root of this tree is the user view.**	Horizontal Fragmentation
    * Data is horizontally fragmented when (possibly overlapping) subsets of data tuples are stored at different sites.
      * See Vertical Fragmentation
**INDUS
      * INDUS stands for Intelligent Data Understanding System
      * See http://www.cild.iastate.edu/~honavar/nsfitr02.html**	Java Properties file
    * A Java Properties file is a map of keys and values.
      * Comments in this file begin with the pound sing (#) and continue until the end of the line.
      * See http://java.sun.com/javase/6/docs/api/java/util/Properties.html
**Ontology
      * An ontology is a representation of a set of concepts within a domain and the relationships between those concepts.
      * See http://en.wikipedia.org/wiki/Ontology_(computer_science)**	Open-World Semantics
    * Under open-world semantics, if a proposition P cannot proved or disproved, then P is unknown.
**See Closed-World Semantics**	RDBMS
      * RDBMS stands for Relational DataBase Management System.  It is the most common type of database used to day.
    * See http://en.wikipedia.org/wiki/Relational_database_management_system
**SQL
    * It is a database programming language for reading and writing data in a database
    * See http://en.wikipedia.org/wiki/SQL**	 URI
    * A Uniform Resource Identifier is a compact string of characters used to identify or name a resource.
    * See http://en.wikipedia.org/wiki/Uniform_Resource_Identifier
**User View
    * The user view is the root view of the data source tree.  The user will write queries and receive results with respect to this view.
> Vertical Fragmentation
    * This occurs from distribution of subtuples of the data tuples among various sites.
    * See Horizontal Fragmentation**	XML
    * XML stands for eXtensible Markup Language and is a general-purpose markup language
      * Comments in this file start with “<!--" and continue until “-->” and cannot contain “--" within the comment.
      * See http://en.wikipedia.org/wiki/XML




## Running INDUS ##

## Setup Tutorial ##

//TODO

## Example Queries ##
//TODO

## Credits ##
//TODO

## Appendix ##

Examples of Various Configuration File  For a Sample Example

Examples of Various Configuration File  For a Sample Example
•	 indus.conf
```
# The Driver for various types of relational databases

driver_mysql=com.mysql.jdbc.Driver
driver_postgress=org.postgresql.Driver

#the name of the default database where internal results are storeddbname_indus=indus
# where indus is hosted
hostname_indus=localhost
#indus dbtype
dbtype_indus=mysql

#username password of various datasources

account_indus=Indususer;induspasswd
account_DS1=neerajkoul1;pass1
account_DS2=neerajkoul2;pass2
account_DS3=neerajkoul3;pass33

#Uncomment below if you want to change to another file
view_filename=view.txt

```



•	UserView  (view.txt)

```
UserViewName=DS1_DS2_DS3
DTreeFile=tree.xml
SchemaMap=schemamap.txt
OntologyMap=ontomap.txt

```

DTree (tree.xml)
```
<?xml version="1.0" encoding="ISO-8859-1" ?> 
<tree>
<node name="DS1_DS2_DS3" fragmentationType="horizontal">
	<dataSourceDescriptor file="userviewdesc.xml" />
	<children>
		<node name="DS1" fragmentationType="horizontal">
			<dbInfo type="mysql" host="localhost" DRIVER="org.postgresql.Driver" datasource="DS1" />
			<dataSourceDescriptor file="ds1desc.xml" />
		</node>
		<node name="DS2_DS3" fragmentationType="horizontal">
			<children joincol="key2" joinTable="EMPLOYEETABLE"  type="int"> <!-- other type could be varchar -->
				<node name="DS2" fragmentationType="vertical">
					<dbInfo type="mysql" host="localhost" DRIVER="org.postgresql.Driver" datasource="DS2" />
					<dataSourceDescriptor file="ds2desc.xml" />				</node>
				<node name="DS3" fragmentationType="vertical">
					<dbInfo type ="mysql" host="localhost" DRIVER="org.postgresql.Driver" datasource="DS3" />
					<dataSourceDescriptor file="ds3desc.xml" />				</node>
			</children>
		</node>
	</children>
</node>
</tree>

```



•	DataSource Descriptor (userviewdesc.xml)

```
<?xml version="1.0" encoding="ISO-8859-1" ?> 
 <descriptors>
 <descriptor>
<datasource name="DS1">
<table name="DS1_Table">
  <column name="id" type="varchar" /> 
  <column name="status" type="varchar">
  <!--  the base is used to construct a URI for the value stored in the table
  e.g if the table stores a value status. It would correspond to URI ${base}/status
   -->
  <operation type="AVH" base="www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH" ontology="www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH" ontLoc="ont.txt" /> 
  </column>
  <column name="compensation" type="float4" /> 
  <column name="alias" type="varchar" /> 
  <column name="servicelength" type="int4" /> 
  </table>
  </datasource>
  </descriptor>
  </descriptors>

```

•	DataSource Descriptor (ds1desc.xml)
```
`<?xml version="1.0" encoding="ISO-8859-1" ?> 
 <descriptors>
 <descriptor>
<datasource name="DS1">
<table name="DS1_Table">
  <column name="id" type="varchar" /> 
  <column name="status" type="varchar">
  <!--  the base is used to construct a URI for the value stored in the table
  e.g if the table stores a value status. It would correspond to URI ${base}/status
   -->
  <operation type="AVH" base="www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH" ontology="www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH" ontLoc="ont.txt" /> 
  </column>
  <column name="compensation" type="float4" /> 
  <column name="alias" type="varchar" /> 
  <column name="servicelength" type="int4" /> 
  </table>
  </datasource>
  </descriptor>
  </descriptors>

```

•	DataSource Descriptor (ds2desc.xml)

```
<?xml version="1.0" encoding="ISO-8859-1" ?>
<descriptors>
	<descriptor>
		<datasource name="DS2" >
			<table name="DS2_Table">
				<column name="ssn" type="varchar" />
				<column name="type"  type ="varchar">
				 <operation type="AVH" base="www.ailab.iastate.edu/indus/ds/ont2/typeType_AVH" ontology="www.ailab.iastate.edu/indus/ds/ont2/typeType_AVH" ontLoc="ont.txt"/> 
				 </column>
			</table>
		</datasource>
	</descriptor>
</descriptors>

 
```



•	DataSource Descriptor (ds3desc.xml)

```
<?xml version="1.0" encoding="ISO-8859-1" ?>
<descriptors>
	<descriptor>
		<datasource name="DS3" >
			<table name="DS3_Table">
				<column name="social" type="varchar" />
				<column name="salary" type="float4"/>
				<column name="nickname" type="varchar"/>
				<column name="serviceyears" type="int4"/>
			</table>
		</datasource>
	</descriptor>
</descriptors>

```


•	Ontology File(ont.txt)
```
;typename=positionType
;subTypeOf=AVH
;ordername=ISA
xmlns:n0=www.ailab.iastate.edu/indus/uView/ont0
n0:positionType_AVH{
..\positionType_AVH\grad
..\positionType_AVH\grad\M.S
..\positionType_AVH\grad\ph.D
..\positionType_AVH\undergraduate
..\positionType_AVH\undergraduate\fe
..\positionType_AVH\undergraduate\fe\redshirt
..\positionType_AVH\undergraduate\so
..\positionType_AVH\undergraduate\jun
..\positionType_AVH\undergraduate\se
}

;typename=statusType
;subTypeOf=AVH
;ordername=ISA
xmlns:n1=www.ailab.iastate.edu/indus/ds/ont1
n1:statusType_AVH{
..\statusType_AVH\graduate
..\statusType_AVH\graduate\mast
..\statusType_AVH\graduate\doct
..\statusType_AVH\undergraduate
..\statusType_AVH\undergraduate\freshman
..\statusType_AVH\undergraduate\sophomore
..\statusType_AVH\undergraduate\junior
..\statusType_AVH\undergraduate\senior
}

#Currently freely hanging nodes have some trouble
;typename=typeType
;subTypeOf=AVH
;ordername=ISA
xmlns:n2=www.ailab.iastate.edu/indus/ds/ont2
n2:typeType_AVH{
..\typeType_AVH\doctor
..\typeType_AVH\master
..\typeType_AVH\fresh
..\typeType_AVH\soph
..\typeType_AVH\junior
..\typeType_AVH\senior
}

```


•	Schema Map (schemamap.txt)
```
`
#----------------------------------------------------------------------# Keys are stored from DataSorceView to UserView in the format,   name=value
# If an conversion function is required for transforming values of the attribute between the two views, the format is
#  
#   name=value,exp1,exp2 where exp1 and exp2 are expressions with variable x
#   exp1 is used to convert a value from the user view to datasource view and exp2 from datasource view to user view
#
#   For format of expression supported http://lts.online.fr/dev/java/math.evaluator/
#   Expression is ignored if the column has an AVH associated with it
#----------------------------------------------------------------------
#DS1
DS1.DS1_Table.id=DS1_DS2_DS3.EMPLOYEETABLE.key2
DS1.DS1_Table.status=DS1_DS2_DS3.EMPLOYEETABLE.position
DS1.DS1_Table.compensation=DS1_DS2_DS3.EMPLOYEETABLE.benefits,x+1,x-1
DS1.DS1_Table.alias=DS1_DS2_DS3.EMPLOYEETABLE.firstname
DS1.DS1_Table.servicelength=DS1_DS2_DS3.EMPLOYEETABLE.timehere,100*x,x

#DS2
DS2.DS2_Table.ssn=DS1_DS2_DS3.EMPLOYEETABLE.key2
DS2.DS2_Table.type=DS1_DS2_DS3.EMPLOYEETABLE.position

#DS3
DS3.DS3_Table.social=DS1_DS2_DS3.EMPLOYEETABLE.key2
DS3.DS3_Table.salary=DS1_DS2_DS3.EMPLOYEETABLE.benefits,x+1,x-1
DS3.DS3_Table.nickname=DS1_DS2_DS3.EMPLOYEETABLE.firstname
DS3.DS3_Table.serviceyears=DS1_DS2_DS3.EMPLOYEETABLE.timehere

```

•	Ontology Map (ontmap.txt)

```
#For Data Source DS1
DS1@www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH@_junior=EQUAL@www.ailab.iastate.edu/indus/uView/ont0/positionType_AVH@_jun
DS1@www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH@_graduate=SUPER@www.ailab.iastate.edu/indus/uView/ont0/positionType_AVH@_M.S
#Also do without the mapping below (should result in empty In clause )
#DS1@www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH@_undergraduate=EQUAL@www.ailab.iastate.edu/indus/uView/ont0/positionType_AVH@_undergraduate
DS1@www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH@_freshman=EQUAL@www.ailab.iastate.edu/indus/uView/ont0/positionType_AVH@_fe

#For Data Source DS2
DS2@www.ailab.iastate.edu/indus/ds/ont2/typeType_AVH@_junior=SUPER@www.ailab.iastate.edu/indus/uView/ont0/positionType_AVH@_redshirt
DS2@www.ailab.iastate.edu/indus/ds/ont2/typeType_AVH@_fresh=SUB@www.ailab.iastate.edu/indus/uView/ont0/positionType_AVH@_undergraduate

```