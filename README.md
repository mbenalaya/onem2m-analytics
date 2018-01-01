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
The binary, config and starting script will be generated under the folder "target"

### Configure the analytics
Use the config.ini file to make your configuration

#### MQTT parameters
```sh
MQTT_TOPICS = iot-2/type/+/id/+/evt/event/fmt/+
MQTT_SERVER_URI = ssl://d932lf.messaging.internetofthings.ibmcloud.com:8883
```
#### Watson IoT Platform parameters
```sh
APP_ID = a:d932lf:oneM2M123
API_KEY = a-d932lf-pw0hqouwfb
AUTH_TOKEN = 2Q!9lYAi1QgltK)0D@
```
#### Prediction parameters
```sh
PREDICTION_CYCLE = 10
ZSCORE_WINDOW = 10
PREDICTIVE_SERVICE_URL = https://ibm-watson-ml.eu-gb.bluemix.net/pm/v1/score/nocycle20rebuid50?accesskey=/032nxDVhkS6mCwAbCXrNR2UvzjCck/7E/Kgci2LS1/owzOSQ1q8Bmum2EAGlvgJpvelDBj2EWArRQzCnErs5G6xF7OPG2R5H0oB0w5syog=
```
### Start the analytics
Execute the following script to start the analytics
```sh
$ ./start.sh
```
## Demonstration
<!--
## 1. [Start a oneM2M CSE](https://github.com/mbenalaya/onem2m-watson/blob/master/README.md#start-a-onem2m-cse)
## 2. [Start the oneM2M Watson IoT interworking](https://github.com/mbenalaya/onem2m-watson/blob/master/README.md#start-the-onem2m-watson-iot-interworking)
## 3. Start the oneM2M analytics
Follow the steps explained in the [Getting Started](https://github.com/mbenalaya/onem2m-analytics/blob/master/README.md##getting-started) section to run the analytics
## 4. [Visualize your data and predicition on Watson IoT Platform](https://github.com/mbenalaya/onem2m-watson/blob/master/README.md#visualize-your-data-on-watson-iot-platform)
-->
## License
Open sourced under the Eclipse Public License 1.0.
