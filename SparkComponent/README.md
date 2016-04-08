# Spark Streaming application

The Spark Streaming application subscribes to IoT device events in realtime and make a ReST call to the SPSS model deployed on Predictive Analysis service to detect a temperature change before it hits the danger zone. And publishes the result back to Watson IoT Platform, so that RTI can alert if required. 

### Build & Run the sample outside eclipse

* * Clone the iot-predictive-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/iot-predictive-analytics-samples.git`
    
* Navigate to the SparkComponent project, 

    `cd iot-predictive-analytics-samples\SparkComponent`
    
* Run the maven build as follows,

    `mvn clean package`

This will download all required dependencies and starts the building process. Once built, the sample can be located in the target directory. Post the jar IoTSparkAsServiceSample-1.0.0-SNAPSHOT-jar-with-dependencies.jar **on a publicly available URL, for example box, dropbox, etc..** if you want to run it using the Notebook.

----

## Running the sample in Notebook - IBM Bluemix Spark as a service environment

This application is designed to run on the Apache Spark as a service on Bluemix. Use the following notebook code to run the application.



     %AddJar https://github.com/sathipal/spark-streaming-mqtt-with-security_2.10-1.3.0/releases/download/0.0.1/spark-streaming-mqtt-security_2.10-1.3.0-0.0.1.jar
     %AddJar http://central.maven.org/maven2/org/apache/wink/wink-json4j/1.4/wink-json4j-1.4.jar
     %AddJar https://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/org.eclipse.paho.client.mqttv3/1.0.2/org.eclipse.paho.client.mqttv3-1.0.2.jar
     %AddJar http://central.maven.org/maven2/org/apache/commons/commons-math/2.2/commons-math-2.2.jar
     %AddJar http://repo1.maven.org/maven2/args4j/args4j/2.0.12/args4j-2.0.12.jar
     %AddJar https://github.com/ibm-messaging/iot-predictive-analytics-samples/releases/download/0.0.1/IoTSparkAsServiceSample-1.0.0-SNAPSHOT.jar
     
     import com.ibm.iot.iotspark.IoTSparkAsServiceSample
     
     //Watson IoT Platform related parameters
     IoTSparkAsServiceSample.setConfig("appid","a:coi0nz:sample123tg")
     IoTSparkAsServiceSample.setConfig("uri","ssl://coi0nz.messaging.internetofthings.ibmcloud.com:8883")
     IoTSparkAsServiceSample.setConfig("mqtopic","iot-2/type/+/id/+/evt/temperature/fmt/+")
     IoTSparkAsServiceSample.setConfig("apikey","a-coi0nz-g1appit53j")
     IoTSparkAsServiceSample.setConfig("authtoken","TQW4aawpx7u4Fqkbhr")
     
     // Predictive Service related parameters
     IoTSparkAsServiceSample.setConfig("window","10")
     IoTSparkAsServiceSample.setConfig("cycle","10")
     IoTSparkAsServiceSample.setConfig("predictive-service-url","https://palbyp.pmservice.ibmcloud.com/pm/v1/score/anamolydetection?accesskey=hnFUDkIzzsGe0YVE+5juW/C0vIgU0rxsMqy4S6I/6cCPnygUVORP2EmOkIkbyyXqHxGxQ3pIogjgEOjN0TGDTcL0h32gVzPkwMbmHXNpi+F3907R6Hs2aoSILF3lpXYVTyyJ2wQjjJXz+oZYxTKsn7GaDzwM1qkFBxscCMvJRHk=")
     
     // Start the Streaming job
     IoTSparkAsServiceSample.startStreaming(sc, 2)

Where,

    appid    : MQTT Client ID for this application
    uri      : Watson IoT Platform organization
    mqtopic  : MQTT Topic to subscribe to IoT device events
    apikey   : Watson IoT Platform API Key
    authtoken: Watson IoT Platform Auth token
    Window   : WZScore window size. It means the WZScore will calculate the local zscore based on the window size. Since local zscore is only based on this window size, it will be more sensitive to the data changes. For example, a value of 10 will calculate the standard deviation based on last 10 data entries
    cycle    : Controls Zscore window. The model will give 50 predictions based on current data set, the further prediction goes, the un-accurate the prediction will be. so the cycle will let the user how many prediction will be used. For example, a cycle value of 20 means the code will only use 20 prediction entries from each prediction run as forecast.
    
**Note:** In case if you want to run the application with your custom (modified) jar, modify the URL "https://github.com/ibm-messaging/iot-predictive-analytics-samples/releases/download/0.0.1/IoTSparkAsServiceSample-1.0.0-SNAPSHOT.jar" with your custom build jar url.

----

### Running the sample using spark-submit script in Bluemix

Follow the stpes [present here](https://console.ng.bluemix.net/docs/services/AnalyticsforApacheSpark/index-gentopic3.html#using_spark-submit) to run the application in Bluemix but outside of Notebook.

----

### Running the sample using non Bluemix Environment

Use the following command to run it on Linux.

    $SPARK_HOME/bin/spark-submit --class com.ibm.iot.iotspark.IoTSparkAsServiceSample --master spark://host:7077 --jars IoTSparkAsService/lib/spark-streaming-mqtt-security_2.10-1.3.0.jar  IoTSparkAsServiceSample-1.0.0-SNAPSHOT-jar-with-dependencies.jar --mqtopic iot-2/type/+/id/+/evt/temperature/fmt/+ --uri ssl://organizationid.messaging.internetofthings.ibmcloud.com:8883 --apikey <Your org key> --authtoken <Your Auth token> --appid <application id> --window 10 --cycle 10 --predictive-service-url "Predictive service url>
 
----
