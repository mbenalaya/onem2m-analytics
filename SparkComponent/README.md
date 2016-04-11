# Spark Streaming application

The Spark Streaming application reads the sensor events from Watson IoT Platform, invokes the Predictive Analytics service with the sensor values in realtime, gets the next few forecasts from Predictive Analytics service and then calculates the z-score (aka, a standard score indicates how many standard deviations an element is from the mean) to indicate the degree of difference in the actual reading compared to the forecast readings.

A z-score can be calculated from the following formula,

    z = (X - μ) / σ

where z is the z-score, X is the value of the element, μ is the population mean, and σ is the standard deviation

Since the forecast is a trend indicator, a bigger difference than the normal range would indicate a sudden change of value. So in a way, the z-score is being used as an indicator of predict an outside the acceptable threshold event happening. Thus the z-score can be used in RTI rule to determine when an alert needs to be raised. A larger value filters out smaller spikes and dips.

----

## Recipe

The end to end flow using this Spark application is explained in detail in [this recipe ](https://developer.ibm.com/recipes/tutorials/engage-machine-learning-for-detecting-anomalous-behaviors-of-things/).

----

## Running out of the box sample in Notebook - IBM Bluemix Spark as a service environment

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
    Window   : WZScore window size. It means the WZScore will calculate the local ZScore based on the window size. Since local ZScore is only based on this window size, it will be more sensitive to the data changes. For example, a value of 10 will calculate the standard deviation based on last 10 data entries
    cycle    : Controls ZScore window. The model will give 50 predictions based on current data set, the further prediction goes, the un-accurate the prediction will be. so the cycle will let the user how many prediction will be used. For example, a cycle value of 20 means the code will only use 20 prediction entries from each prediction run as forecast.
    
----

### Building and running your own code

You can modify the existing Spark streaming application according to their usecase and run it, its very easy. Follow the steps below to do the same

* Clone the iot-predictive-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/iot-predictive-analytics-samples.git`
    
* Import the SparkComponentproject project into the Eclipse environment and make necessary changes.
    
* Run the maven build either via Eclipse or command line,

    `mvn clean package`

This will download all required dependencies and starts the building process. Once built, the sample can be located in the target directory. Post the jar IoTSparkAsServiceSample-1.0.0-SNAPSHOT-jar-with-dependencies.jar **on a publicly available URL, for example box, dropbox, etc..** if you want to run it using the Notebook.

* Go to the notebook, Modify the cell to upload the Streaming application jar that you built instead of the one available in the Github,

    `%AddJar <URL of IoTSparkAsServiceSample-1.0.0-SNAPSHOT-jar-with-dependencies.jar> -f  `


**Note:** Modify the URL of the IoTSparkAsServiceSample jar with the URL where you placed the built application (say box, dropbox, etc). In case of dropbox, you may have to change the last part of URL (so instead of ‘?dl=0′, you may have to change it to ‘?dl=1′)

Also, since the IoTSparkAsServiceSample-1.0.0-SNAPSHOT-jar-with-dependencies.jar is built with all the dependencies, you don’t need to specify the dependencies except for the spark-streaming-mqtt-security_2.10-1.3.0-0.0.1.jar.

* Keep the contents of remaining notebook cell as it is and start the streaming application.

----

### Running the sample using spark-submit script in Bluemix

Follow the steps [present here](https://console.ng.bluemix.net/docs/services/AnalyticsforApacheSpark/index-gentopic3.html#using_spark-submit) to run the application in Bluemix but outside of Notebook.

----

### Running the sample using non Bluemix Environment

Use the following command to run it on Linux.

    $SPARK_HOME/bin/spark-submit --class com.ibm.iot.iotspark.IoTSparkAsServiceSample --master spark://host:7077 --jars IoTSparkAsService/lib/spark-streaming-mqtt-security_2.10-1.3.0.jar  IoTSparkAsServiceSample-1.0.0-SNAPSHOT-jar-with-dependencies.jar --mqtopic iot-2/type/+/id/+/evt/temperature/fmt/+ --uri ssl://organizationid.messaging.internetofthings.ibmcloud.com:8883 --apikey <Your org key> --authtoken <Your Auth token> --appid <application id> --window 10 --cycle 10 --predictive-service-url "Predictive service url>
 
----
