#!/bin/bash

while :; do
  if juju status | grep -q eccdashboard; then
    echo Waiting for old service to finish...
    juju destroy-service tomcat
    juju destroy-service eccdashboard
	juju destroy-service postgresql-eccdashboard
	juju destroy-service rabbitmq-server
	echo Updating charm
	juju upgrade-charm --repository=./charms eccdashboard	
  else
    break
  fi
done

juju deploy postgresql postgresql-eccdashboard
juju deploy rabbitmq-server

set -eu
if [ ! -d target ]; then
	mvn install
fi
rm -rf charms/precise/eccdashboard/deploy
mkdir -p charms/precise/eccdashboard/deploy/eccdashboard
(cd charms/precise/eccdashboard/deploy/eccdashboard && unzip ../../../../../target/prototype.war)
cp ../edm/resources/edm-metrics-postgres.sql charms/precise/eccdashboard/

juju deploy cs:~robert-ayres/precise/tomcat
juju deploy --repository=./charms local:eccdashboard eccdashboard
juju add-relation eccdashboard tomcat
juju add-relation eccdashboard:edm postgresql-eccdashboard:db
juju add-relation eccdashboard:rabbit rabbitmq-server

juju expose tomcat

juju debug-log
