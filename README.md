# iot-predictive-analytics-samples

The project is split into two parts,

First part showcases how one can make use of the Predictive Analytics service, available on the IBM Bluemix, to determine the hidden patterns on the events published by IoT devices in realtime.

Second part takes the advantage of the data (historical data) produced by the part 1 and analyze/visualize them using Spark and Notebook.

----

Design and Architecture
--------------------------

####Part#1

Following are the list of components used in the part#1 of this project, 

![Alt text](./Diagrams/high-level-diagram.PNG?raw=true "High Level Architecture - Part1")

* [Device component](https://github.com/ibm-messaging/iot-predictive-analytics-samples/tree/master/DeviceDataGenerator) - publishes the temperature events to the IBM Watson IoT Platform for every 2 seconds.

* [Spark streaming component](https://github.com/ibm-messaging/iot-predictive-analytics-samples/tree/master/SparkComponent) - Multiple receivers running in the Apache Spark service subscribes to these events and make a ReST call to the SPSS model deployed on Predictive Analysis

* [SPSS Model](https://github.com/ibm-messaging/iot-predictive-analytics-samples/blob/master/SPSSModel/nocycle20rebuid50.str) - The SPSS stream is built on top of the SPSS streaming time series expert model. Based on the input data, it finds the most suitable time series forecast model and trains the model automatically during the scoring time. The first 50 data points are used for training and the model will adjust itself over time. The stream is deployed in Predictive Analytics service. Through API call, the service will return the next few forecasts based on the input. When real time data reading is received, the Spark streaming job gets the next few forecasts from Predictive Analytics service. It also calculates a A ZScore (aka, a [standard score](https://en.wikipedia.org/wiki/Standard_score) indicates how many standard deviations an element is from the mean) to indicate the degree of difference in the actual reading compared to the forecast readings. Since the forecast is a trend indicator, a bigger difference than the normal range would indicate a sudden change of value.

* RTI component - The ZScore will be used in RTI rule to determine when the alert will be raised. A larger value filters out smaller spikes and dips.

####Part#2

Following are the list of components used in the part#2 of this project, 

![Alt text](./Diagrams/recipe2-alone-architecture.PNG?raw=true "High Level Architecture - Part2")

As shown, the [Node-RED application](https://github.com/ibm-watson-iot/predictive-analytics-samples/tree/master/Node-RED) will subscribe to the results (which contains the temperature, forecast temperature, zscore and wzscore values) from the Watson IoT Platform and store them into a Cloudant NoSQL DB. This Cloundat NoSQL DB will act as a historical data storage.

Once the Cloudant NoSQL DB is filled with enough data, this recipe will use the [Jupyter Notebook](http://nbviewer.jupyter.org/github/jupyter/notebook/blob/master/docs/source/examples/Notebook/What%20is%20the%20Jupyter%20Notebook.ipynb) to load the data into the [Spark engine](http://spark.apache.org/) and use [Spark SQL](http://spark.apache.org/docs/latest/sql-programming-guide.html), other graphical libraries to analyze the data and show the results in charts or graphs.

Instruction about how to run the sample can be found in their respective directories.

----

Recipe
-------------

* Refer to the part1 recipe [Engage Machine Learning for detecting anomalous behaviors of things](https://developer.ibm.com/recipes/tutorials/engage-machine-learning-for-detecting-anomalous-behaviors-of-things/) for more information about the Predictive Analytics and the samples.

----

### License
-----------------------

The library is shipped with Eclipse Public License and refer to the [License file] (https://github.com/ibm-messaging/iot-predictive-analytics-samples/blob/master/LICENSE) for more information about the licensing.

----
