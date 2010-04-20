@echo off

set IIF_MEMORY=1G
set IIF_CLASSPATH=iif_0.1.1.jar;lib/commons-lang-2.2.jar;lib/junit-4.4.jar;lib/log4j-1.2.15.jar;lib/mysql-connector-java-5.0.7-bin.jar;lib/Zql.jar;lib/pellet_jars/aterm-java-1.6.jar;lib/pellet_jars/owlapi-bin.jar;lib/pellet_jars/owlapi-src.jar;lib/pellet_jars/pellet-cli.jar;lib/pellet_jars/pellet-core.jar;lib/pellet_jars/pellet-datatypes.jar;lib/pellet_jars/pellet-dig.jar;lib/pellet_jars/pellet-el.jar;lib/pellet_jars/pellet-explanation.jar;lib/pellet_jars/pellet-jena.jar;lib/pellet_jars/pellet-modularity.jar;lib/pellet_jars/pellet-owlapi.jar;lib/pellet_jars/pellet-pellint.jar;lib/pellet_jars/pellet-query.jar;lib/pellet_jars/pellet-rules.jar;lib/pellet_jars/pellet-test.jar;lib/pellet_jars/relaxngDatatype.jar;lib/pellet_jars/servlet.jar;lib/pellet_jars/xsdlib.jar;lib/matheval/meval.jar;\lib\postgresql-8.1-410.jdbc3.jar

set IIF_ARGS=

:getArg

if "%1"=="" goto run
set IIF_ARGS=%IIF_ARGS% %1
shift
goto getArg


:run 

java -Xmx%IIF_MEMORY% -classpath %IIF_CLASSPATH% org.iastate.ailab.qengine.core.QueryEngine %IIF_ARGS%
