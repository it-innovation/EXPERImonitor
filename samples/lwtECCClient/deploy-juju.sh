#!/bin/bash

# Note: this script assumes you already have Rabbit deployed!

while :; do
  if juju status | grep -q headless; then

	echo Updating charm
	juju upgrade-charm --repository=./charms headless

    echo Waiting for old service to finish...
	juju destroy-service headless
	juju destroy-service postgresql-headless

  else
    break
  fi
done

cp ../../edm/resources/edm-metrics-postgres.sql charms/precise/headlesseccclient/

juju deploy postgresql postgresql-headless

set -eu

if [ ! -d lib/dependency ]; then
	echo "Building charm..."
	mvn install
	mvn dependency:copy-dependencies
fi

rm -rf charms/precise/headlesseccclient/code/lib/
cp -r lib charms/precise/headlesseccclient/code/lib

echo "Deploying charm..."

juju deploy --repository=./charms local:headlesseccclient headless
juju add-relation headless:localdb postgresql-headless:db
juju add-relation headless:rabbit rabbitmq-server

echo "Tailing log... (CTRL-C to exit)"
juju debug-log

#unit=`juju status | grep "\<headless/\(.*\):$" | sed 's/\s*\(.*\):/\1/'`
#for x in seq 10; do
#  sleep 5
#  juju ssh $unit tail -f /var/lib/juju/units/headless-\*/charm/code/headless.log || echo Not ready
#done
