#!/bin/sh

# Run this script as root to stop tomcat, clear out PostgreSQL DB and start tomcat.
# It is mostly useful for developers.

echo "Resetting the EXPERImonitor"

SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH"/..

# stop tomcat
service tomcat7 stop
# drop EXPERImonitor's database and regenerate a clean one
sudo -u postgres dropdb edm-metrics
sudo -u postgres createdb -T template0 edm-metrics --encoding=UTF8 --locale=en_US.utf8
sudo -u postgres psql -d edm-metrics -f edm/resources/edm-metrics-postgres.sql
# remove the unpacked EXPERImonitor from tomcat to ensure we get a clean version redeployed
rm -rf /var/lib/tomcat7/webapps/ECC
rm -rf /var/cache/tomcat7/Catalina/localhost/ECC
# start tomcat
service tomcat7 start
