# Spark Streaming application

The Spark Streaming application subscribes to IoT device events in realtime and make a ReST call to the SPSS model deployed on Predictive Analysis service to detect a temperature change before it hits the danger zone. And publishes the result back to Watson IoT Platform, so that RTI can alert if required. 

Also, The application uses few parameters like, window and cycle to control the number of invocation to Predictive Analytics service. For every invocation, the model will give 50 predictions based on current data set and Spark application will use this data to indicate the degree of difference between the actual reading and the forecasted value.  

## Usage

This application is designed to run on the Apache Spark as a service on Bluemix. Use the following notebook code to run the application.



     %AddJar https://github.com/sathipal/spark-streaming-mqtt-with-security_2.10-1.3.0/releases/download/0.0.1/spark-streaming-mqtt-security_2.10-1.3.0-0.0.1.jar
     %AddJar https://www.dropbox.com/s/3n8yumh6tr6r294/IoTSparkAsServiceSample-2.0.0-SNAPSHOT-jar-with-dependencies.jar?dl=1
     
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
    
