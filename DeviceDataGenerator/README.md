# IoTDataGenerator

Sends a simulated sensor data to Watson IoT Platform.


### Register Device in IBM Watson IoT Platform if not registered already

Follow the steps in [this recipe](https://developer.ibm.com/recipes/tutorials/how-to-register-devices-in-ibm-iot-foundation/) to register your device in Watson IoT Platform if not registered already. And copy the registration details, like the following,

* Organization-ID = [Your Organization ID]
* Device-Type = [Your Device Type]
* Device-ID = [Your Device ID]
* Authentication-Method = token
* Authentication-Token = [Your Device Token]


### Usage

    $ java -jar IoTDataGenerator-1.0.0-SNAPSHOT.jar [options...]

Where `options` are:

     --datapath VAL : test dataset file path
     --help (-h)    : Show help
     --id VAL       : Watson IoT Platform Client ID
     --mqtopic VAL  : Watson IoT Platform MQTT topic
     --pwd VAL      : Device Password
     --uri VAL      : Watson IoT Platform Server URI

Refer to Watson IoT Platform [documentation](http://iotf.readthedocs.org/en/latest/devices/mqtt.html) for more information about the parameters and how to form them.

### Example Invocation

    java -jar IoTDataGenerator-1.0.0-SNAPSHOT.jar --id d:doi0nz:iotsample-deviceType:Device01 --mqtopic iot-2/evt/temperature/fmt/json --pwd password --uri ssl://doi0nz.messaging.internetofthings.ibmcloud.com:8883 --user use-token-auth --datapath ./testDataSet

### Logging
`IoTDataGenerator` uses [log4j](http://logging.apache.org/log4j/2.x/) for logging, as do the [Paho](http://www.eclipse.org/paho/). If you want to customize logging, simply create your own `log4j.properties` file, and start up `IoTDataGenerator` as follows:

    $ java -Dlog4j.configuration=file:///path/to/log4j.properties -jar IoTDataGenerator-1.0.0-SNAPSHOT.jar [options...]

----

