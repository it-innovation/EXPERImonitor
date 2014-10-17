#!/bin/sh

SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH"/..

cd thirdPartyLibs
./installLibraries.sh
mvn package assembly:assembly -Preleasesrc
mvn package assembly:assembly -Preleasebin
cd target/experimedia-arch-ecc-2.1-bin/bin
zip -d EccService-2.1.war WEB-INF/lib/owlim-lite-5.4.jar
cd ../..
zip -r experimedia-arch-ecc-2.1-bin.zip experimedia-arch-ecc-2.1-bin
