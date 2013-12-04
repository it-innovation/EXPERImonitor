echo "Installing OWLIM Lite JARs in local Maven repository"

call mvn install:install-file -Dfile=.\owlim-lite-5.3.jar -DgroupId=com.ontotext -DartifactId=owlim-lite -Dversion=5.3 -Dpackaging=jar

call mvn install:install-file -Dfile=.\owlim-lite-5.4.jar -DgroupId=com.ontotext -DartifactId=owlim-lite -Dversion=5.4 -Dpackaging=jar

echo "Done."