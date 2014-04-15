#!/bin/bash

echo Installing OWLIM Lite JAR in local Maven repository

mvn install:install-file -Dfile=owlim-lite-5.4.jar -DgroupId=com.ontotext -DartifactId=owlim-lite -Dversion=5.4 -Dpackaging=jar

#echo Installing OWLIM PROV Store in local Maven repository

#mvn install:install-file -Dfile=OWLimProvStore-0.9-SNAPSHOT.jar -DgroupId=uk.ac.soton.itinnovation.edmprov -DartifactId=OWLimProvStore -Dversion=0.9-SNAPSHOT -Dpackaging=jar

# OpenRDF WAR files are automatically installed using Vagrant; otherwise, please manually deploy these in Tomcat.

echo Done
