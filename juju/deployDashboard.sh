#!/bin/bash

set -eu
echo Deploying Experimedia-ecc

while :; do
  if juju status | grep -q eccdashboard; then
	
	echo Updating charm
	juju upgrade-charm --repository=eccdashboard/charms eccdashboard
		
    echo Waiting for old service to finish...
    juju destroy-service tomcat
    juju destroy-service eccdashboard
	juju destroy-service postgresql-eccdashboard
	juju destroy-service rabbitmq-server
	
  else
    break
  fi
done

set -eu

rm -rf eccdashboard/charms/precise/eccdashboard/deploy
mkdir -p eccdashboard/charms/precise/eccdashboard/deploy/eccdashboard
(cd eccdashboard/charms/precise/eccdashboard/deploy/eccdashboard && unzip ../../../../../target/prototype.war)
cp edm/resources/edm-metrics-postgres.sql eccdashboard/charms/precise/eccdashboard/

juju deploy --constraints='mem=2G' cs:~robert-ayres/precise/tomcat
juju deploy --repository=eccdashboard/charms local:eccdashboard eccdashboard
juju add-relation eccdashboard tomcat
#juju deploy postgresql postgresql-eccdashboard
juju deploy --repository=charm-postgres local:postgresql postgresql-eccdashboard
juju add-relation eccdashboard:edm postgresql-eccdashboard:db-admin
#juju deploy rabbitmq-server
juju deploy --repository=charm-rabbitmq-tal local:rabbitmq-server
juju expose rabbitmq-server
juju set rabbitmq-server management_plugin=True
juju add-relation eccdashboard:rabbit rabbitmq-server

juju expose tomcat

juju debug-log