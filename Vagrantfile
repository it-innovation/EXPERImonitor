# -*- mode: ruby -*-
# vi: set ft=ruby :

# Deploy SAD into a VM with IP 10.0.0.11
# SAD service will be available at http://localhost:8081/SAD on host machine.
# Tomcat manager will be on http://localhost:8081/manager/html with username manager, password manager
# Tail the log file with: vagrant ssh -c "tail -f /var/log/tomcat7/catalina.out"

## Configuration for this script (this part is Ruby) ##

hostname = "SAD"
ram = "512"
# Deploying with a static IP like this means that VirtualBox will set up a virtual network card that listens on 10.0.0.0/48
# If you have other machines on that subnet then change the IP or you will have to delete the virtual card from the
# VirtualBox GUI afterwards.  The purpose of using a static IP is so that it can talk to the Rabbit/ECC deployed in the same way.
ip = "10.0.0.11"

rabbit_ip = "10.0.0.10"
#rabbit_ip = "rabbitmq.experimedia.eu"
uuid = "00000000-0000-0000-0000-000000000000"

# if deploying in Jetty:
#plugin_path = "..\\/sad-plugins"
#coordinator_path = "src\\/main\\/resources\\/coordinator.json"

# if deploying in Tomcat:
plugin_path = "/home/vagrant/experimedia-sad/sad-plugins"
coordinator_path = "webapps/SAD/WEB-INF/classes/coordinator.json"

## The following shell script is run once the VM is built (this part is bash) ##

$script = <<SCRIPT
apt-get update
#apt-get upgrade -y  # could upgrade base OS packages if you want

## Install dependencies ##

# sort out switching to Java 7 before installing Tomcat
apt-get install -y openjdk-7-jdk
update-alternatives --set java /usr/lib/jvm/java-7-openjdk-i386/jre/bin/java
rm /usr/lib/jvm/default-java
ln -s /usr/lib/jvm/java-1.7.0-openjdk-i386 /usr/lib/jvm/default-java
# install everything else we need
apt-get install -y git
apt-get install -y maven2
apt-get install -y postgresql-9.1  # needed for local EDM
apt-get install -y mongodb
apt-get install -y tomcat7
apt-get install -y sphinx-doc
apt-get install -y sphinx-common

## Get the SAD code ##
# (need to do this before PostgreSQL config)

# remove old code in case we are reprovisioning
rm -rf experimedia-sad

# get the latest SAD code from IT Innovation internal Git
git clone git://soave.it-innovation.soton.ac.uk/git/experimedia-sad
cd experimedia-sad
git checkout mvb-prov-master

# or get the code from GitHub  TODO: test this
#git clone https://github.com/it-innovation/SAD experimedia-sad
#cd experimedia-sad

# or copy SAD code from guest machine to host machine
#mkdir experimedia-sad
#rsync -a /vagrant/ experimedia-sad --exclude '.git' --exclude 'target'
#cd experimedia-sad

## Set up PostgreSQL (needed if using local EDM) ##

# create the PostgreSQL database for the client metric cache
sudo -u postgres createdb -T template0 agent-edm-metrics --encoding=UTF8 --locale=en_US.utf8
sudo -u postgres psql -d edm-metrics -f sad-service/src/main/resources/edm-metrics-postgres.sql

# set postgres user's password to "sofia"
sudo -u postgres psql --command="ALTER USER postgres WITH PASSWORD 'sofia';"

# set postgres user to need password for local connections (and reload)
echo "host all postgres 127.0.0.1/0 password" > /tmp/pg_hba.conf
sudo -u postgres cp /tmp/pg_hba.conf /etc/postgresql/9.1/main
service postgresql reload

## Set up Tomcat ##

# enable the tomcat manager webapp with username manager, password manager
echo "<?xml version='1.0' encoding='utf-8'?><tomcat-users><user rolename='manager-gui'/><user username='manager' password='manager' roles='manager-gui'/></tomcat-users>" > /etc/tomcat7/tomcat-users.xml
service tomcat7 restart

## SAD service configuration ##

# configure location of Rabbit
# configure uuid
# configure location of plugins
# configure coordinator.json location

#sed \
#  -e 's/127\.0\.0\.1/#{rabbit_ip}/' \
#  -e 's/00000000-0000-0000-0000-000000000000/#{uuid}/' \
#  -e 's/\\.\\.\\/sad-plugins/#{plugin_path}/' \
#  -e 's/src\\/main\\/resources\\/coordinator\\.json/#{coordinator_path}/' \
#  sad-service/src/main/resources/sadproperties.json \
#  > /tmp/sadproperties.json
#cp /tmp/sadproperties.json sad-service/src/main/resources/
#echo "**** SAD config:"

cat > sad-service/src/main/resources/sadproperties.json <<SADCONFIG
{
    "plugins": {
        "path": "#{plugin_path}"
    },
    "coordinator": {
        "path": "#{coordinator_path}",
        "reset_database_on_start": "y"
    },
    "basepath": "http://localhost:8080/SAD",
    "ecc": {
        "enabled": "y",
        "Rabbit_IP": "#{rabbit_ip}",
        "Rabbit_Port": "5672",
        "Monitor_ID": "#{uuid}",
        "Client_Name": "Social Analytics Dashboard"
    },
    "edm": {
        "enabled": "n",
        "dbURL": "localhost:5432",
        "dbName": "agent-edm-metrics",
        "dbUsername": "postgres",
        "dbPassword": "sofia",
        "dbType": "postgresql"
    }
}
SADCONFIG

# add in sample plugin visualisations
cp -a sad-plugins/basic-sns-stats/src/main/resources/visualise sad-service/src/main/webapp/visualise/basic-sns-stats
cp -a sad-plugins/facebook-collector/src/main/resources/visualise sad-service/src/main/webapp/visualise/facebook-collector
cp -a sad-plugins/twitter-searcher/src/main/resources/visualise sad-service/src/main/webapp/visualise/twitter-searcher

## Build ##

echo "**** Building SAD"
cd lib
./install_into_maven
cd ..
mvn install

## Deployment ##

# deploy the SAD into Tomcat
echo "**** Deploying SAD into Tomcat"
cp sad-service/target/SAD*.war /var/lib/tomcat7/webapps/SAD.war
echo "**** Finished: SAD deployed in Tomcat port running on port 8080.  Mapped to localhost:8081/SAD on host machine."

# or run the SAD using Jetty launched from maven
#cd sad-service
#mvn jetty:run-war

SCRIPT

## Configuration of the VM (Ruby again) ##

Vagrant.configure("2") do |config|

    # build off ubuntu 12.04 LTS (32 bit)
	config.vm.box = "precise32"
    config.vm.box_url = "http://files.vagrantup.com/precise32.box"
    config.vm.hostname = hostname
	
	# Forward host port 8081 to guest port 8080 for Tomcat
	config.vm.network :forwarded_port, host: 8081, guest: 8080
	## Forward host port 8081 to guest port 8081 for Jetty
	#config.vm.network :forwarded_port, host: 8081, guest: 8081
	
	# Set static private network address
	config.vm.network "private_network", ip: ip
	
    # configure virtualbox
    config.vm.provider :virtualbox do |vb|
        vb.customize [
            'modifyvm', :id,
            '--name', hostname,
            '--memory', ram
        ]
	end

	# Provision using shell script embedded above
	config.vm.provision :shell, :inline => $script

end
