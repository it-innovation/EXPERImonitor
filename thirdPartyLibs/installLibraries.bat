echo "Installing OWLIM Lite JAR in local Maven repository"

if exist {owlim-lite-5.4.jar} (
    call mvn install:install-file -Dfile=.\owlim-lite-5.4.jar -DgroupId=com.ontotext -DartifactId=owlim-lite -Dversion=5.4 -Dpackaging=jar
) else (
    echo "Could not find owlim-lite-5.4.jar, please place the file in this directory. See README.txt for more information."
)

rem OpenRDF WAR files are automatically installed using Vagrant; otherwise, please manually deploy these in Tomcat.

echo "Done."
