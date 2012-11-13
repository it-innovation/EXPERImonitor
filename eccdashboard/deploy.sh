#!/bin/bash

while :; do
  if juju status | grep -q tomcat; then
    echo Waiting for old service to finish...
    juju destroy-service tomcat
    juju destroy-service eccdashboard
  else
    break
  fi
done
# juju deploy --repository=/home/tal/experimedia/charms local:postgresql postgresql
# juju deploy rabbitmq-server

set -eu
rm -rf charms/precise/eccdashboard/deploy
mkdir -p charms/precise/eccdashboard/deploy/eccdashboard
(cd charms/precise/eccdashboard/deploy/eccdashboard && unzip ../../../../../target/prototype.war)
cp ../edm/resources/edm-metrics-postgres.sql charms/precise/eccdashboard/

juju deploy --constraints='mem=2G' cs:~robert-ayres/precise/tomcat
juju deploy --repository=./charms local:eccdashboard eccdashboard
juju add-relation eccdashboard tomcat
juju add-relation eccdashboard:edm postgresql:db
juju add-relation eccdashboard:rabbit rabbitmq-server

juju expose tomcat
