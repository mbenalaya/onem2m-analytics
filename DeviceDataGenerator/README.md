# IoTDataGenerator

Send sensor data with prediction to IBM RealTime Insight.

## Usage

    $ java -jar IoTDataGenerator-1.0.0-SNAPSHOT.jar [options...]

Where `options` are:

     --datapath VAL : test dataset file path
     --help (-h)    : Show help
     --id VAL       : IOTF Client ID
     --mqtopic VAL  : IOTF MQTT topic
     --pwd VAL      : IOTF Password
     --uri VAL      : IOTF Server URI
     --user VAL     : IOTF UserName


If you don't specify any command-line options, it uses the following defaults:

    id:       d:o672lp:IoTAnalytic:Client67890
    user:     use-token-auth
    pwd:      Client67890
    mqtopic:  iot-2/evt/temperature/fmt/json
    uri:      tcp://o672lp.messaging.internetofthings.ibmcloud.com:1883
    datapath: ./testDataSet

***Note***: you can't run the code using some of the default settings without creating a device as following: 
   
    Device Type:     IoTAnalytic
    Device Password: Client67890
    The Server URI has to point to your organization.

## Logging
`IoTDataGenerator` uses [log4j](http://logging.apache.org/log4j/2.x/) for logging, as do the [Paho](http://www.eclipse.org/paho/). If you want to customize logging, simply create your own `log4j.properties` file, and start up `IoTDataGenerator` as follows:

    $ java -Dlog4j.configuration=file:///path/to/log4j.properties -jar IoTDataGenerator-1.0.0-SNAPSHOT.jar [options...]

