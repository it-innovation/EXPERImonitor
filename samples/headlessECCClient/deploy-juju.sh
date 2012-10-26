#!/bin/bash

# Note: this script assumes you already have Rabbit and Postgresql deployed, e.g.

# juju deploy postgresql
# juju deploy rabbitmq-server

echo "Destroying existing service (if any)"
juju destroy-service headless

set -eu

echo "Building charm..."
if [ ! -d lib ]; then
	mvn install
	mvn dependency:copy-dependencies
fi
rm -rf charms/precise/headlesseccclient/code/lib/
cp -r lib charms/precise/headlesseccclient/code/lib

echo "Deploying charm..."
juju deploy --repository=./charms local:headlesseccclient headless
juju add-relation headless:localdb postgresql:db
juju add-relation headless:rabbit rabbitmq-server

echo "Tailing log... (CTRL-C to exit)"
juju debug-log
