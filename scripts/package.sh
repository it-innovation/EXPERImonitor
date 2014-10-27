#!/bin/sh

SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
ROOT="$SCRIPTPATH"/..
cd "$ROOT"

# install pre-requisites for packaging
apt-get install -y make
apt-get install -y python-setuptools
easy_install -U Sphinx
apt-get install -y zip

# build the docs
cd "$ROOT"/doc
make clean html |tee /tmp/releasedoc.log

# install the third-party libs (later removed from distribution)
cd "$ROOT"/thirdPartyLibs
./installLibraries.sh

# build
cd "$ROOT"
mvn clean
mvn package assembly:assembly -Preleasesrc |tee /tmp/releasesrc.log |grep -E 'WARNING|ERROR'
mvn package assembly:assembly -Preleasebin |tee /tmp/releasebin.log |grep -E 'WARNING|ERROR'

# zip
cd "$ROOT"/target/experimedia-arch-ecc-2.2-bin/bin
zip -d EccService-2.2.war WEB-INF/lib/owlim-lite-5.4.jar
cd "$ROOT"/target
zip -r experimedia-arch-ecc-2.2-bin.zip experimedia-arch-ecc-2.2-bin
