#!/bin/sh

sudo service tomcat7 stop
sudo -u postgres dropdb edm-metrics
sudo -u postgres createdb -T template0 edm-metrics --encoding=UTF8 --locale=en_US.utf8
sudo -u postgres psql -d edm-metrics -f edm/resources/edm-metrics-postgres.sql
sudo service tomcat7 start