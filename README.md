# iot-predictive-analytics-samples (Update in progress)

This project showcases how one can make use of the Predictive Analytics service, available on the IBM Bluemix, to determine the hidden patterns on the events published by IoT devices, on the IBM Watson IoT Platform. 

----

Design and Architecture
--------------------------

Following are the list of components used in this project,

* Device component - publishes the temperature events to the IBM Watson IoT Platform for every 2 seconds.

* Spark streaming component - Multiple receivers running in the Apache Spark service subscribes to these events and make a ReST call to the SPSS model deployed on Predictive Analysis

* SPSS Model - The SPSS stream is built on top of the SPSS streaming time series expert model. Based on the input data, it finds the most suitable time series forecast model and trains the model automatically during the scoring time. The first 50 data points are used for training and the model will adjust itself over time. The stream is deployed in Predictive Analytics service. Through API call, the service will return the next few forecasts based on the input. When real time data reading is received, the Spark streaming job gets the next few forecasts from Predictive Analytics service. It also calculates a score to indicate the degree of difference the actual reading compared to the forecast in its place obtained previously based on the values before it. Since the forecast is a trend indicator, a bigger difference than the normal range would indicate a sudden change of value.

* RTI component - The score can be used in RTI rule to determine when the alert will be raised. A larger value filters out smaller spikes and dips.

Instruction about how to run the sample can be found in their respective directories.

----

Recipe
-------------

Refer to the recipe for more information about the Predictive Analytics and the samples.

----
