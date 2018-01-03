# oneM2M-analytics

## Getting Started

### Requirements
* Apache Maven 3 to build the project
* JAVA 1.7 (at least) to run the project


### Clone and build from source
Clone the project and go to the folder onem2m-analytics
```sh
$ git clone https://github.com/mbenalaya/onem2m-analytics.git
$ cd onem2m-analytics
```
Build the project using the following command
```sh
$ mvn clean install
```
The binary and config files will be generated under the folder "target"

### Configure the analytics
Use the config.ini file to make your configuration

#### MQTT parameters
```sh
MQTT_TOPICS = iot-2/type/+/id/+/evt/event/fmt/+
MQTT_SERVER_URI = ssl://8riy9e.messaging.internetofthings.ibmcloud.com:8883
```
#### Watson IoT Platform parameters
```sh
APP_ID = a:8riy9e:oneM2M123
API_KEY = a-8riy9e-e2ywsxpahe
AUTH_TOKEN =  vx)RY+4MW-gqeUwkC8
```
#### Prediction parameters
```sh
PREDICTION_CYCLE = 10
ZSCORE_WINDOW = 10
PREDICTIVE_SERVICE_URL = https://ibm-watson-ml.eu-gb.bluemix.net/pm/v1/score/nocycle20rebuid50?accesskey=EzjIR1yqpeSLI1k8XXXO1x8hwYLRGn9Hb4/5XXgqYG5wWJAm8oHM3dFJPzSvZ0fKc1AbOE1UW5e5NZRAC6JLeJm4UhduKiR4fCfmGQLC1t8=
```
### Start the analytics
Execute the following command from the folde onem2m-analytics to start the analytics
```sh
$ mvn exec:java 
```
<!--
## Demonstration
## 1. [Start a oneM2M CSE](https://github.com/mbenalaya/onem2m-watson/blob/master/README.md#start-a-onem2m-cse)
## 2. [Start the oneM2M Watson IoT interworking](https://github.com/mbenalaya/onem2m-watson/blob/master/README.md#start-the-onem2m-watson-iot-interworking)
## 3. Start the oneM2M analytics
Follow the steps explained in the [Getting Started](https://github.com/mbenalaya/onem2m-analytics/blob/master/README.md##getting-started) section to run the analytics
## 4. [Visualize your data and predicition on Watson IoT Platform](https://github.com/mbenalaya/onem2m-watson/blob/master/README.md#visualize-your-data-on-watson-iot-platform)
-->
## License
Open sourced under the Eclipse Public License 1.0.
