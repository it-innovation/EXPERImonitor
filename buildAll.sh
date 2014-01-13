#!/bin/bash

set -eu

echo Installing 3rd party libraries
cd thirdPartyLibs
./installLibraries.sh
cd ..

echo Building Experimedia-ECC
mvn clean install

echo Done