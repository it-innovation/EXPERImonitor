#!/bin/bash

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
rm -rf charms/precise/wegov/deploy
mkdir -p charms/precise/wegov/deploy/wegov
(cd charms/precise/wegov/deploy/wegov && unzip ../../../../../wegov-dashboard/target/prototype.war)
cp ../edm/resources/edm-metrics-postgres.sql charms/precise/wegov/
cp wegov-dashboard/src/main/resources/quartz/tables_postgres.sql charms/precise/wegov/
cp wegov-database-maintenance/target/wegov-database-maintenance-1.0-jar-with-dependencies.jar charms/precise/wegov/
cp wegov-tools/search-analysis/target/wegov-search-analysis-tool-2.0-jar-with-dependencies.jar charms/precise/wegov/

juju deploy cs:~robert-ayres/precise/tomcat tomcat-wegov
juju deploy --repository=./charms local:wegov wegov
juju add-relation wegov tomcat-wegov
juju add-relation wegov:rabbit rabbitmq-server
juju deploy postgresql postgresql-wegov
juju add-relation wegov:edm postgresql-wegov:db
#juju deploy rabbitmq-server


juju expose tomcat-wegov

juju debug-log
