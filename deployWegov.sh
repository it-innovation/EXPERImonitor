#!/bin/bash

set -eu
echo Deploying WeGov

while :; do
  if juju status | grep -q wegov; then
    echo Waiting for old service to finish...
    juju destroy-service tomcat-wegov
    juju destroy-service wegov
	juju destroy-service postgresql-wegov
	#juju destroy-service rabbitmq-server
  else
    break
  fi
done

set -eu
rm -rf wegov-as-client/charms/precise/wegov/deploy
mkdir -p wegov-as-client/charms/precise/wegov/deploy/wegov
(cd wegov-as-client/charms/precise/wegov/deploy/wegov && unzip ../../../../../wegov-dashboard/target/prototype.war)
cp edm/resources/edm-metrics-postgres.sql wegov-as-client/charms/precise/wegov/
cp wegov-as-client/wegov-dashboard/src/main/resources/quartz/tables_postgres.sql wegov-as-client/charms/precise/wegov/
cp wegov-as-client/wegov-database-maintenance/target/wegov-database-maintenance-1.0-jar-with-dependencies.jar wegov-as-client/charms/precise/wegov/
cp wegov-as-client/wegov-tools/search-analysis/target/wegov-search-analysis-tool-2.0-jar-with-dependencies.jar wegov-as-client/charms/precise/wegov/

juju deploy --constraints='mem=2G' cs:~robert-ayres/precise/tomcat tomcat-wegov
juju deploy --repository=wegov-as-client/charms local:wegov wegov
juju add-relation wegov tomcat-wegov
#juju deploy postgresql postgresql-wegov
juju deploy --repository=charm-postgres local:postgresql postgresql-wegov
juju add-relation wegov:edm postgresql-wegov:db-admin
#juju deploy rabbitmq-server
juju add-relation wegov:rabbit rabbitmq-server

juju expose tomcat-wegov

juju debug-log
