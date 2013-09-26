ECC Virtual Machine with Apache WebDAV service using Vagrant
------------------------------------------------------------

The ECC_vagrant folder should contain the following:

davlock:
A file that sets the path for the WebDAV lock file

Vagrantfile:
A file containing the script that sets up the virtual machine with all dependencies

vhost:
The virtual hosts file used by the apache server

passwords.dav:
A file that is used to store a list of WebDAV user accounts and encrypted password

To install the ECC virtual machine with WebDAV
-----------------------------------------------
1. Open Virtual Box
2. Open a command line in the ECC_vagrant directory
3. Type: 'vagrant up'
4. When installation is finished type: 'vagrant ssh' to access the VM

Adding Users to the WebDAV Service
-------------------------------------
The virtual machine has two WebDAV user accounts pre-configured:

username: davuser
password: password

username: davuser2
password: password2

To add a user to the WebDAV users list use the following command, replacing the word username for the desired username:

sudo htdigest /var/www/WebDAV/digestpasswd.dav webdavdigest username

You will be prompted for a password and asked to retype the password.

Using the WebDAV service
------------------------
The command line WebDAV client Cadaver has been pre-installed for testing purposes, this can be accessed by typing:

cadaver http://192.168.0.155/webdav
Username: username
Password: password

Type 'help' to view all available commands

Alternatively the WebDAV directory can be accessed using the host machine by using a WebDAV client and entering the URL:

http://localhost:1235/webdav
Username: username
Password: password

