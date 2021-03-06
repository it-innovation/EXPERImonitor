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
echo "*** Building docs"
cd "$ROOT"/doc
make clean html |tee /tmp/releasedoc.log

# install the third-party libs (later removed from distribution)
echo "*** Installing third-party libs"
cd "$ROOT"/thirdPartyLibs
./installLibraries.sh

# build
cd "$ROOT"
echo "*** maven clean"
mvn clean
echo "*** Building source package"
mvn --batch-mode package assembly:assembly -Preleasesrc |tee /tmp/releasesrc.log |grep -E 'WARNING|ERROR'
echo "*** Building binary package"
mvn --batch-mode package assembly:assembly -Preleasebin |tee /tmp/releasebin.log |grep -E 'WARNING|ERROR'

# zip
echo "Zipping"
cd "$ROOT"/target/experimedia-arch-ecc-2.2-bin/bin
zip -d EccService-2.2.war WEB-INF/lib/owlim-lite-5.4.jar
rm experimedia-arch-ecc-edm-test-2.2.jar  # this jar should not contain anything other than test code, but it does so we delete it here
cd "$ROOT"/target
zip -r experimedia-arch-ecc-2.2-bin.zip experimedia-arch-ecc-2.2-bin

echo
echo "Check /tmp/release*.log for further information."
echo "Third-party license information has been aggregated into target/experimedia-arch-ecc-2.2-src/licenses"
echo "To update the license file data in version control, execute the following command and then commit:"
echo "cp -a $ROOT/target/experimedia-arch-ecc-2.2-src/licenses $ROOT"
