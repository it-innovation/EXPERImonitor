# -*- mode: ruby -*-
# vi: set ft=ruby :

# To use, install vagrant and VirtualBox and type "vagrant up"

# The following environment variables can be used to control the behaviour:
# ECC_IP: the IP address for the ECC VM to use (default 10.0.0.10)
# ECC_UUID: the UUID for the dashboard to use (default 00000000-0000-0000-0000-000000000000)
# ECC_PORT: the port to map the ECC to on the host machine (default 8090)
# RABBIT_IP: the IP address to use for RabbitMQ (default 10.0.0.10)
# RABBIT_PORT: the port to map RabbitMQ to on the host machine (default 5682)
# RABBIT_MGT_PORT: the port to map RabbitMQ management interface to on the host machine (default 55682)
# ECC_GIT: if defined then ECC source is checked out from git and this branch used, otherwise uses source from host machine

## Configuration for this script (this part is Ruby) ##

hostname = "ECC"
ram = "512"

ecc_ip = (ENV['ECC_IP'] ? ENV['ECC_IP'] : '10.0.0.10')
ecc_uuid = (ENV['ECC_UUID'] ? ENV['ECC_UUID'] : '00000000-0000-0000-0000-000000000000')
ecc_port = (ENV['ECC_PORT'] ? ENV['ECC_PORT'] : '8090')
rabbit_ip = (ENV['RABBIT_IP'] ? ENV['RABBIT_IP'] : '10.0.0.10')
#rabbit_ip = "rabbitmq.experimedia.eu"
rabbit_port = (ENV['RABBIT_PORT'] ? ENV['RABBIT_PORT'] : '5682')
rabbit_mgt_port = (ENV['RABBIT_MGT_PORT'] ? ENV['RABBIT_MGT_PORT'] : '55682')
ecc_git = (ENV['ECC_GIT'] ? ENV['ECC_GIT'] : '')

info = "
ECC service is deployed on VM with IP #{ecc_ip}
ECC service is mapped to http://localhost:#{ecc_port}/ECC on host machine.
Tomcat manager is mapped to http://localhost:#{ecc_port}/manager/html with username manager, password manager
Using RabbitMQ deployed on #{rabbit_ip}
RabbitMQ AMQP bus mapped to http://localhost:#{rabbit_port} on host machine.
RabbitMQ management interface is mapped to http://localhost:#{rabbit_mgt_port} on host machine (username: guest / password: guest).
Tail the log file with: vagrant ssh -c 'tail -f /var/lib/tomcat7/logs/catalina.out'"

puts info

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

# remove old code to get a clean build in case we are re-provisioning
rm -rf experimedia-ecc

# get ECC code (need to do this before PostgreSQL config)
if [ '#{ecc_git}' != "" ]; then
	# get the latest ECC code from Git
	git clone git://soave.it-innovation.soton.ac.uk/git/experimedia-ecc
	cd experimedia-ecc
	# need -f to force it, otherwise it prints a warning and nothing happens
	git checkout -f #{ecc_git}  
	cd ..
else
	# or copy ECC code from guest machine to host machine
	mkdir experimedia-ecc
	rsync -a /vagrant/ experimedia-ecc --exclude '.git' --exclude 'target'
fi

## Set up PostgreSQL ##

# TODO: need to delete the DB first so this can work with re-provisioning

# create the database
sudo -u postgres createdb -T template0 edm-metrics --encoding=UTF8 --locale=en_US.utf8
sudo -u postgres psql -d edm-metrics -f experimedia-ecc/edm/resources/edm-metrics-postgres.sql

# set postgres user's password to "password"
sudo -u postgres psql --command="ALTER USER postgres WITH PASSWORD 'password';"

# set connection made by postgres user from command line to not need password
# set postgres user to need password for connections made via socket from localhost
# reload postgresql
echo "local all postgres trust" > /tmp/pg_hba.conf
echo "host all postgres 127.0.0.1/0 password" >> /tmp/pg_hba.conf
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
# can't do this at the moment, it is got from config.experimedia.eu or falls back on hard-coded values
# can alter it by hand in the GUI at start-up

## Build ##
echo "**** Building ECC"

cd experimedia-ecc
cd thirdPartyLibs && /bin/sh ./installLibraries.sh && cd ..
mvn install

## Deploy ##

# deploy the ECC into Tomcat
cp eccDash/target/*.war /var/lib/tomcat7/webapps/ECC.war
echo "**** Deploying ECC into Tomcat"

if [ '#{ecc_git}' != "" ]; then
	echo 'ECC built from git branch #{ecc_git}'
else
	echo 'ECC code copied from host machine'
fi

echo "Vagrant shell script completed."
echo "#{info}"

SCRIPT

## Back to Ruby again ##

Vagrant.configure("2") do |config|

    # build off ubuntu 12.04 LTS (32 bit)
	config.vm.box = "precise32"
    config.vm.box_url = "http://files.vagrantup.com/precise32.box"
    config.vm.hostname = hostname
	
	# Forward host port to guest port 8080 for Tomcat
	config.vm.network :forwarded_port, host: Integer(ecc_port), guest: 8080
	# Forward host port to guest port 5672 for RabbitMQ messages
	config.vm.network :forwarded_port, host: Integer(rabbit_port), guest: 5672
	# Forward host port to guest port 55672 for RabbitMQ management page
	config.vm.network :forwarded_port, host: Integer(rabbit_mgt_port), guest: 55672
	
	# Set static private network address.
	# Deploying with a static IP like this means that VirtualBox will set up a virtual network 
	# card that listens on e.g. 10.0.0.0/48
	# If you have other machines on that subnet then change the IP or you will have to delete 
	# the virtual card from the VirtualBox GUI afterwards.  The purpose of using a static IP 
	# is so that other services deployed in the same way can communicate.
	config.vm.network "private_network", ip: ecc_ip
	
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
