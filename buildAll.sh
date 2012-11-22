#!/bin/bash

set -eu
echo Building Experimedia-ecc
mvn clean install

echo Building ECC Dashboard
cd eccdashboard
mvn clean install

echo Building WeGov
cd ../wegov-as-client

mvn install:install-file -Dfile=./lib/entities29.jar -DgroupId=com.mindprod -DartifactId=entities -Dversion=1.0 -Dpackaging=jar  -DgeneratePom=true

mvn install:install-file -Dfile=./lib/java_fathom-1.1.0.jar -DgroupId=com.representqueens -DartifactId=entities -Dversion=1.1.0 -Dpackaging=jar  -DgeneratePom=true

mvn install:install-file -Dfile=./lib/weka.jar -DgroupId=weka -DartifactId=entities -Dversion=1.0 -Dpackaging=jar  -DgeneratePom=true

mvn clean install

cd wegov-database-maintenance
mvn install
mvn assembly:single

cd ../wegov-tools/search-analysis
mvn install
mvn assembly:single

cd ../../../
echo Done