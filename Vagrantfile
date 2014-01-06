# -*- mode: ruby -*-
# vi: set ft=ruby :

# To use, install vagrant and VirtualBox and type "vagrant up"

# ECC service will be available at http://localhost:8080/ECC on host machine.
# RabbitMQ AMQP bus will be available on port http://localhost:5672 on host machine and http://10.0.0.10:5672.
# RabbitMQ management interface will be available at http://localhost:55672 on host machine (username: guest / password: guest).
# Tomcat manager will be on http://localhost:8080/manager/html with username manager, password manager
# Tail the log file with: vagrant ssh -c "tail -f /var/lib/tomcat7/logs/catalina.out"

## Configuration for this script (this part is Ruby) ##

hostname = "ECC"
ram = "512"
# Deploying with a static IP like this means that VirtualBox will set up a virtual network card that listens on 10.0.0.0/48
# If you have other machines on that subnet then change the IP or you will have to delete the virtual card from the
# VirtualBox GUI afterwards.  The purpose of using a static IP is so that other services deployed in the same way can 
# communicate.
ip = "10.0.0.10"

rabbit_ip = ip
#rabbit_ip = "rabbitmq.experimedia.eu"
uuid = "00000000-0000-0000-0000-000000000000"

## The following shell script is run once the VM is built (this part is bash) ##

$script = <<SCRIPT
apt-get update
#apt-get upgrade -y  # could upgrade base OS packages if you want

## Install dependencies ##

apt-get install -y git
apt-get install -y maven2
apt-get install -y tomcat7
apt-get install -y tomcat7-admin
apt-get install -y openjdk-6-jdk
apt-get install -y postgresql-9.1
apt-get install -y rabbitmq-server

## Get the ECC code ##

# remove old code in case we are reprovisioning
rm -rf experimedia-ecc

# get the latest ECC code from Git
#git clone git://soave.it-innovation.soton.ac.uk/git/experimedia-ecc
#cd experimedia-ecc
#git checkout sgc-master
#cd ..

# or copy ECC code from guest machine to host machine
# (need to do this before PostgreSQL config)
mkdir experimedia-ecc
rsync -a /vagrant/ experimedia-ecc --exclude '.git' --exclude 'target'

## Set up PostgreSQL ##

# create the database
sudo -u postgres createdb -T template0 edm-metrics --encoding=UTF8 --locale=en_US.utf8
sudo -u postgres psql -d edm-metrics -f experimedia-ecc/edm/resources/edm-metrics-postgres.sql

# set postgres user's password to "password"
sudo -u postgres psql --command="ALTER USER postgres WITH PASSWORD 'password';"

# set postgres user to need password for local connections (and reload)
echo "host all postgres 127.0.0.1/0 password" > /tmp/pg_hba.conf
sudo -u postgres cp /tmp/pg_hba.conf /etc/postgresql/9.1/main
service postgresql reload

## Set up RabbitMQ ##

# enable rabbitmq management plugin so we can see what's happening
/usr/lib/rabbitmq/lib/rabbitmq_server-2.7.1/sbin/rabbitmq-plugins enable rabbitmq_management
service rabbitmq-server restart

## Set up Tomcat ##

# enable the tomcat manager webapp with username manager, password manager
echo "<?xml version='1.0' encoding='utf-8'?><tomcat-users><user rolename='manager-gui'/><user username='manager' password='manager' roles='manager-gui'/></tomcat-users>" > /etc/tomcat7/tomcat-users.xml
service tomcat7 restart

## Configure ECC Service ##

# configure location of Rabbit
# configure uuid
sed \
  -e 's/^Rabbit_IP=127\\.0\\.0\\.1/Rabbit_IP=#{rabbit_ip}/' \
  -e 's/^Monitor_ID=00000000-0000-0000-0000-000000000000/Monitor_ID=#{uuid}/' \
  experimedia-ecc/eccDash/src/main/webapp/WEB-INF/em.properties \
  > /tmp/em.properties
cp /tmp/em.properties experimedia-ecc/eccDash/src/main/webapp/WEB-INF/
echo "**** ECC config:"
cat experimedia-ecc/eccDash/src/main/webapp/WEB-INF/em.properties

## Build ##

echo "**** Building ECC"
cd experimedia-ecc
cd thirdPartyLibs && ./installLibraries.sh && cd ..
mvn install

## Deploy ##

# deploy the ECC into Tomcat
echo "**** Deploying ECC into Tomcat"
cp eccDash/target/*.war /var/lib/tomcat7/webapps/ECC.war

echo "**** Finished: ECC deployed in Tomcat port running on port 8080.  Mapped to localhost:8080/ECC on host machine."
SCRIPT

Vagrant.configure("2") do |config|

    # build off ubuntu 12.04 LTS (32 bit)
	config.vm.box = "precise32"
    config.vm.box_url = "http://files.vagrantup.com/precise32.box"
    config.vm.hostname = hostname
	
	# Forward host port 8080 to guest port 8080 for Tomcat
	config.vm.network :forwarded_port, host: 8080, guest: 8080
	# Forward host port 5672 to guest port 5672 for RabbitMQ messages
	config.vm.network :forwarded_port, host: 5672, guest: 5672
	# Forward host port 55672 to guest port 55672 for RabbitMQ management page
	config.vm.network :forwarded_port, host: 55672, guest: 55672
	
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
