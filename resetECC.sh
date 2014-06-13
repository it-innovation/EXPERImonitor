#!/bin/sh

echo "Resetting the ECC"

SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")

# stop tomcat
sudo service tomcat7 stop
# drop ECC's database and regenerate a clean one
sudo -u postgres dropdb edm-metrics
sudo -u postgres createdb -T template0 edm-metrics --encoding=UTF8 --locale=en_US.utf8
sudo -u postgres psql -d edm-metrics -f "$SCRIPTPATH"/edm/resources/edm-metrics-postgres.sql
# remove the unpacked ECC from tomcat to ensure we get a clean version redeployed
rm -rf /var/lib/tomcat7/webapps/ECC
rm -rf /var/cache/tomcat7/Catalina/localhost/ECC
# start tomcat
sudo service tomcat7 start
