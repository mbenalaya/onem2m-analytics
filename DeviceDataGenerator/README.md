# IoTDataGenerator

Sends a simulated sensor data to Watson IoT Platform.



### Prerequisites
To build and run the sample, you must have the following installed:

* [git](https://git-scm.com/)
* [maven](https://maven.apache.org/download.cgi)
* Java 7+

----

### Register Device in IBM Watson IoT Platform if not registered already

Follow the steps in [this recipe](https://developer.ibm.com/recipes/tutorials/how-to-register-devices-in-ibm-iot-foundation/) to register your device in Watson IoT Platform if not registered already. And copy the registration details, like the following,

* Organization-ID = [Your Organization ID]
* Device-Type = [Your Device Type]
* Device-ID = [Your Device ID]
* Authentication-Method = token
* Authentication-Token = [Your Device Token]

We need these details to connect the device to IBM Watson IoT Platform.

----

### Usage

    $ mvn exec:java -Dexec.mainClass="com.ibm.iot.iotdatagenerator.IoTDataGenerator"

### Build & Run the sample using Eclipse

You must have installed the [Eclipse Maven plugin](http://www.eclipse.org/m2e/), to import & run the samples in eclipse. Go to the next step, if you want to run manually.

* Clone the iot-predictive-analytics-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/iot-predictive-analytics-samples.git`
    
* Import the **DeviceDataGenerator** project into eclipse using the File->Import option in eclipse.
 
* Modify the device.prop file by entering the device registration details that you noted during device registration.

* Build and Run the IoTDataGenerator sample by right clicking on the project and selecting "Run as" option. Remember to pass the options to connect to Watson IoT Platform.

* Observe that the device connects to Watson IoT Platform and publishes events.

----

### Build & Run the sample outside eclipse

* Clone the iot-predictive-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/iot-predictive-analytics-samples.git`
    
* Navigate to the DeviceDataGenerator project, 

    `cd iot-predictive-analytics-samples\javaDeviceDataGenerator`
    
* Run the maven build as follows,

    `mvn clean package`

* Modify the **device.prop** file present in the target/classes directory by entering the device registration details that you noted during device registration in Watson IoT Platform.

* Run the maven command to publish the temperature data,

    `mvn exec:java -Dexec.mainClass="com.ibm.iot.iotdatagenerator.IoTDataGenerator"`

----

### Logging
`IoTDataGenerator` uses [log4j](http://logging.apache.org/log4j/2.x/) for logging, as do the [Paho](http://www.eclipse.org/paho/). If you want to customize logging, simply create your own `log4j.properties` file, and start up `IoTDataGenerator` as follows:

    $ java -Dlog4j.configuration=file:///path/to/log4j.properties -jar IoTDataGenerator-1.0.0-SNAPSHOT.jar [options...]

----

