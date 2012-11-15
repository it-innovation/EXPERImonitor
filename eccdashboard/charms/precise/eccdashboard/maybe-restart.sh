#!/bin/bash

set -xu

cd /var/lib/tomcat7/

if [ -f edm.properties -a -f em.properties ]; then
  juju-log "Configuration done"
  if [ ! -d /var/lib/tomcat7/webapps/eccdashboard ]; then
    juju-log "Deploying web-app..."
    ln -s `pwd`/deploy/*.war /var/lib/tomcat7/webapps/ROOT.war
  fi
  juju-log "Restarting tomcat"
  /etc/init.d/tomcat7 restart
else
  juju-log "Not fully configured yet..."
fi
