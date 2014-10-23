Introduction to this client
---------------------------

This client is an experimental development; not all of the functionality found in other clients demonstrated in the EXPERImonitor API is available here. At present, this client is capable of:

  * Connecting to the EXPERImonitor
  * Discovery phase capable (1 example metric is provided)
  * Set-up phase capable
  * Live monitoring phase capable (PUSH only)
  * Tear-down phase capable

Limitations of this client include:

  * No pulling implemented (you can easily switch this on however)
  * No EDMAgent support (this will be updated soon)
  * No Post Report phase support (see above note)

Installing Android SDK (and support)
------------------------------------
This client is currently only available as a NetBeans 7.x project and is most easily built and run by first installing:

 * Android SDK tools (latest is recommended; should support OS 4.1.2), see: http://developer.android.com/sdk/index.html
 * NBAndroid NetBeans plugin, see: http://www.nbandroid.org/

Once you have installed both the Android SDK and NBAndroid plugin, you need to set up the following:

  * Set up your Android SDK paths (see Android documentation)
  * Create a virtual Android device using the AVD Manager

Building the Android sample
---------------------------
Having installed the above software, you now need compile the EXPERImonitor (Maven) project so that you can copy the appropriate EXPERImonitor JAR files into the 'libs' directory of the sample folder. Do to this:

  1. Naviate to the root of the EXPERImonitor API

  2. Type 'mvn clean install'

  3. Search for and copy the following EXPERImonitor JAR files into '<root>\samples\basicAndroidClient\libs':
      * experimedia-arch-ecc-amqpAPI-impl-2.2.jar
      * experimedia-arch-ecc-amqpAPI-spec-2.2.jar
      * experimedia-arch-ecc-common-dataModel-experiment-2.2.jar
      * experimedia-arch-ecc-common-dataModel-metrics-2.2.jar
      * experimedia-arch-ecc-common-dataModel-monitor-2.2.jar
      * experimedia-arch-ecc-common-logging-spec-2.2.jar
      * experimedia-arch-ecc-em-factory-2.2.jar
      * experimedia-arch-ecc-em-impl-2.2.jar
      * experimedia-arch-ecc-em-spec-2.2.jar
      * experimedia-arch-ecc-samples-shared-2.2.jar

  4. Add the following third party libraries:
      * amqp-client-2.8.6.jar
      * base64.2.3.8.jar
      * commons-cli-1.1.jar
      * commons-io-1.2.jar
      * gson-2.2.2.jar

  5. Open the basicAndroidClient in NetBeans

  6. Clean and Build the basicAndroidClient (the result should be a debug APK)


Running the Android sample (on your desktop)
--------------------------------------------
Having built your APK (and if you have installed the NBAndroid plugin, you should be able to run your client. The recommended testing procedure for this is:

  1. Start up your RabbitMQ service
  2. Start up your PostgreSQL service
  3. Start up your Tomcat Apache server
  4. Deploy the EXPERImonitor dashboard in the Apache server
  5. Run the Android client (through Netbeans)
  6. Enter the IP of your RabbitMQ server (an 'external' IP is required) in the Android UI
  7. Click 'Connect' on the Android UI
  8. Move through the experiment process (using the dashboard) until you get to Live Monitoring
  9. Move the slider bar on the Android UI - you should see the data appear in the Live monitoring view

