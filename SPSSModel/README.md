Description of the Model
------------------------

The SPSS stream is built on top of the SPSS streaming time series expert model. Based on the input data, it finds the most suitable time series forecast model and trains the model automatically during the scoring time. The first 50 data points are used for training and the model will adjust itself over time. The stream is deployed in Predictive Analytics service. Through API call, the service will return the next few forecasts based on the input.

When real time data reading is received, the Spark streaming job gets the next few forecasts from Predictive Analytics service. It also calculates a score to indicate the degree of difference the actual reading compared to the forecast in its place obtained previously based on the values before it. Since the forecast is a trend indicator, a bigger difference than the normal range would indicate a sudden change of value.

The score can be used in RTI rule to determine when the alert will be raised. A larger value filters out smaller spikes and dips.

----

Postman Instruction
-------------------
