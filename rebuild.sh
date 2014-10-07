#!/bin/sh

# For developers using vagrant
# Run this script as root to stop tomcat, clear out PostgreSQL DB and start tomcat.
# Also resynch with code on host machine and rebuilds.

echo "Resetting the ECC"

SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
cd $SCRIPTPATH

# stop tomcat
service tomcat7 stop
# drop ECC's database and regenerate a clean one
sudo -u postgres dropdb edm-metrics
sudo -u postgres createdb -T template0 edm-metrics --encoding=UTF8 --locale=en_US.utf8
sudo -u postgres psql -d edm-metrics -f edm/resources/edm-metrics-postgres.sql

#TODO: clean up sesame!

# remove the unpacked ECC from tomcat to ensure we get a clean version redeployed
rm -rf /var/lib/tomcat7/webapps/ECC
rm -rf /var/cache/tomcat7/Catalina/localhost/ECC
# resync code from host machine and do clean build
rsync -a /vagrant/ ../experimedia-ecc --exclude '.git' --exclude 'target'
mvn clean install
cp eccService/target/*.war /var/lib/tomcat7/webapps/ECC.war
# start tomcat
service tomcat7 start
