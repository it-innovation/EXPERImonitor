ECC Dashboard 
Visualises EM and EDC in the browser

Requirements:
	- java
	- maven
	- postgres server (for EDM), configured in ../ecc/src/edm/impl/src/main/resources/edm.properties
	- Rabbit MQ server (for EM)
	
Run:
	- Make sure EDM is configured in ../ecc/src/edm/impl/src/main/resources/edm.properties + script ../ecc/src/edm/resources/edm-metrics.sql was run
	- Build everything in ../ with mvn install
	- In this folder run: mvn clean jetty:run-war -Dmaven.test.skip=true
	
You should have it running at http://localhost:8080