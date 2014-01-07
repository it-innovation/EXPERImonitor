#!/bin/bash

echo Installing OWLIM Lite JAR in local Maven repository

mvn install:install-file -Dfile=owlim-lite-5.3.jar -DgroupId=com.ontotext -DartifactId=owlim-lite -Dversion=5.3 -Dpackaging=jar

echo Installing OWLIM PROV Store in local Maven repository

mvn install:install-file -Dfile=OWLimProvStore-0.9-SNAPSHOT.jar -DgroupId=uk.ac.soton.itinnovation.edmprov -DartifactId=OWLimProvStore -Dversion=0.9-SNAPSHOT -Dpackaging=jar

echo Done