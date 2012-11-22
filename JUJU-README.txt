This file describes the steps necessary to deploy both ECC Dashboard and WeGov in Openstack using juju

1. Make sure juju is bootstrapped and "juju status" returns something like:

machines:
  0:
    agent-state: running
    dns-name: 192.168.0.7
    instance-id: f0b8f237-aac6-49a0-9766-d0103edc9138
    instance-state: running

2. Maven and java are installed

3. Run script buildAll.sh in this folder

4. Run script deployDashboard.sh in this folder to deploy ECC Dashboard

5. Run script deployWegov.sh in this folder to deploy WeGov

Associate 95.211.183.145 with wegov

Make sure your browser does not block pop up windows

Login to WeGov with user name: user, password: test

Allow it to access your location

Click on "Advanced search"

Click on Facebook radio button. A Facebook window should pop up, login with valid facebook account, for example:

	m.tastecard@me.com
	/R$]a:M+2H7+9P

and allow access.

Enter event id: 122119071195720

Limit to ~100 posts, do not collect comments

Click Run Now or Schedule to run.