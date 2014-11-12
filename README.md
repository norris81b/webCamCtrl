webCamCtrl
==========

Controls Panasonic KX-DP602 PTZ Camera

This project is a web archive designed to control Pan/Tilt/Zoom cameras. This initial version is taylored to control
the Panasonic KX-DP602, but the hope is that it will be capable of contolling other cameras. The KX-DP602 is an analog
camera, this project only controls the PTZ features, video processing and distribution will need to be handled elsewhere.

Included with the project are the necessary Eclipse project files for ease of development. However, I am using ant to
create the war file for deployment into an application server, currently JBoss AS7.

The Panasonic KX-DP602 is controlled via the RS-232 protocol. However, due to the complexities of getting Java to work
with RS-232 ports on all platforms, this initial version sends/receives commands/responses via network socket and requires
an additional application to translate the commands to RS232. The ser2net application that is included with most Linux
distributions fits the bill nicely. 

Getting Started
---------------

Prerequisites
-------------
 - Jboss AS7 
 - ser2net or other application that can translate network socket to RS232

Running
-------
- Edit the /etc/ser2net.conf (or equivalent) configuration file and setup an associaton between port 3002 and
the RS232 port
- Restart ser2net (or equivalent)
- copy webCamCtrl.war to the <JBOSS_HOME>/standalone/deployments directory
- from a PC/Mac web browser, open url http://<hostname>:8080/webCamCtrl/index.jsp
OR
- from a mobile device, open url http://<hostname>:8080/webCamCtrl/indexMobile.jsp
