How to use the database export tool
-----------------------------------

1. Build the complete EXPERImonitor API (for more information, see the top-level README.txt file)
	* You should find the 'experimedia-arch-ecc-samples-databaseExport-2.2.jar' in the 'target' folder in this directory after a successful build


2. This Java console application depends on the following libraries:

	 joda-time-2.1.jar
	 postgresql-9.1-901.jdbc4.jar
	 slf4j-api-1.7.7.jar
	 experimedia-arch-ecc-common-dataModel-experiment-2.2.jar
	 experimedia-arch-ecc-common-dataModel-metrics-2.2.jar
	 experimedia-arch-ecc-edm-factory-2.2.jar
	 experimedia-arch-ecc-edm-spec-2.2.jar
	 experimedia-arch-ecc-edm-impl-metrics-2.2.jar
	 experimedia-arch-ecc-samples-shared-2.2.jar

	* ... Copy these into the same folder containing 'experimedia-arch-ecc-samples-databaseExport-2.2.jar'


3. Ensure the edm.properties file (found in this folder) has been correctly configured for your PostgreSQL database

	* ... Copy this file into the folder containing your JAR files


4. Run the database export utility using either:

	* runExporter.bat (Windows)
	* runExporter.sh (Linux)

	* ... Copy the appropriate script into the folder containing your JAR files, then execute.


5. All (non-empty) experiments stored in the EXPERImonitor database will be exported into a folder named 'exportedData'
