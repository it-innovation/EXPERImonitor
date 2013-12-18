#!/bin/bash

set -eu

echo Installing OWLIM Lite JAR in local Maven repository

mvn install:install-file -Dfile=.\owlim-lite-5.3.jar -DgroupId=com.ontotext -DartifactId=owlim-lite -Dversion=5.3 -Dpackaging=jar

echo Done