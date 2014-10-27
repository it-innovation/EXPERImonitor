#!/bin/bash

SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH"/..

set -eu

echo "Installing 3rd party libraries"
cd thirdPartyLibs
./installLibraries.sh
cd ..

echo "Building EXPERImonitor"
mvn clean install

echo Done