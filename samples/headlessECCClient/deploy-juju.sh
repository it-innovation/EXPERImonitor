#!/bin/bash

# Note: this script assumes you already have Rabbit and Postgresql deployed, e.g.

# juju deploy postgresql
# juju deploy rabbitmq-server

echo "Destroying existing service (if any)"
juju destroy-service headless

set -eu

if [ ! -d lib/dependency ]; then
	echo "Building charm..."
	mvn install
	mvn dependency:copy-dependencies
fi
rm -rf charms/precise/headlesseccclient/code/lib/
cp -r lib charms/precise/headlesseccclient/code/lib

while :; do
  if juju status | grep -q headless; then
    echo Waiting for old service to finish...
  else
    break
  fi
done

echo "Deploying charm..."
juju deploy --repository=./charms local:headlesseccclient headless
juju add-relation headless:localdb postgresql:db
juju add-relation headless:rabbit rabbitmq-server

echo "Tailing log... (CTRL-C to exit)"
#juju debug-log

unit=`juju status | grep "\<headless/\(.*\):$" | sed 's/\s*\(.*\):/\1/'`
for x in seq 10; do
  sleep 5
  juju ssh $unit tail -f /var/lib/juju/units/headless-\*/charm/code/headless.log || echo Not ready
done
