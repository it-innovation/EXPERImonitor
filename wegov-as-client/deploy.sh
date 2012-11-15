#!/bin/bash

while :; do
  if juju status | grep -q wegov; then
    echo Waiting for old service to finish...
    juju destroy-service tomcat-wegov
    juju destroy-service wegov
	juju destroy-service postgresql-wegov
  else
    break
  fi
done

juju deploy postgresql postgresql-wegov

set -eu
rm -rf charms/precise/wegov/deploy
mkdir -p charms/precise/wegov/deploy/wegov
(cd charms/precise/wegov/deploy/wegov && unzip ../../../../../wegov-dashboard/target/prototype.war)
cp ../edm/resources/edm-metrics-postgres.sql charms/precise/wegov/
cp wegov-dashboard/src/main/resources/quartz/tables_postgres.sql charms/precise/wegov/
cp wegov-database-maintenance/target/wegov-database-maintenance-1.0-jar-with-dependencies.jar charms/precise/wegov/
cp wegov-tools/search-analysis/target/wegov-search-analysis-tool-2.0-jar-with-dependencies.jar charms/precise/wegov/

juju deploy cs:~robert-ayres/precise/tomcat tomcat-wegov
juju deploy --repository=. local:wegov wegov
juju add-relation wegov tomcat
juju add-relation wegov:edm postgresql:db
juju add-relation wegov:rabbit rabbitmq-server

juju expose tomcat

juju debug-log
