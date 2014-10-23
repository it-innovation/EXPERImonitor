#!/bin/sh

cd thirdPartyLibs
./installLibraries.sh
mvn package assembly:assembly -Preleasesrc
mvn package assembly:assembly -Preleasebin
cd target/experimedia-arch-ecc-2.2-bin/bin
zip -d EccService-2.2.war WEB-INF/lib/owlim-lite-5.4.jar
cd ../..
zip -r experimedia-arch-ecc-2.2-bin.zip experimedia-arch-ecc-2.2-bin
