echo "Installing OWLIM Lite JAR in local Maven repository"

call mvn install:install-file -Dfile=.\owlim-lite-5.4.jar -DgroupId=com.ontotext -DartifactId=owlimlite -Dversion=5.4 -Dpackaging=jar

echo "Done."