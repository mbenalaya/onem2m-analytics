/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Jenny Wang - Initial Contribution
 Li Lin - Initial Contribution
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iot.iotspark;

import java.io.FileNotFoundException;
import java.io.Serializable;

import java.util.List;
import java.util.Properties;

import scala.Tuple2;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import org.apache.spark.streaming.mqtt.MQTTUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.kohsuke.args4j.CmdLineException;


import com.google.common.base.Optional;

/**
 * 
 * The Spark Streaming application subscribes to IoT device events in realtime and make a ReST call 
 * to the SPSS model deployed on Predictive Analysis service to detect a temperature change before 
 * it hits the danger zone. 
 * 
 * And publishes the result back to Watson IoT Platform, so that RTI can alert if required. 
 *
 */
@SuppressWarnings("serial")
public class IoTSparkAsServiceSample implements Serializable {
	  static Logger logger = Logger.getLogger(IoTSparkAsServiceSample.class.getName());
	  
	  /**
	   * Watson IoT Platform Parameters
	   */
	  private String serverURI;
	  private String clientId;
	  private String apiKey;
	  private String authToken;
	  private String predictiveServiceURL;
	  
	  /**
	   * the model will give 50 predictions in furture based on current data set, the further prediction goes, 
	   * the un-accurate the prediction will be. so the cycle will let the user how many prediction will be used. 
	   * For example, --cycle 20 means the code will only use 20 prediction entries from each prediction run as forecast
	   */
	  private int zScoreWindow = 0;
	  
	  /**
	   * The window is the WZScore window size. It means the WZScore will calculate the local zscore based on the window size. 
	   * Since local zscore is only based on this window size, it will be more sensitive to the data changes. 
	   * 
	   * For example --window 10 will calculate the standard deviation based on last 10 data entries 
	   */
	  private int wZScoreWindow = 0;
	  
	  
	 /**
	  * A class that holds the IoT device event
	  */
	  private static class IoTEvent implements Serializable {
			private String deviceId;
			private String deviceType;
			private String event;
			
			public IoTEvent(String deviceType, String deviceId, String event) {
				super();
				this.deviceId = deviceId;
				this.deviceType = deviceType;
				this.event = event;
			}
	
			public String getDeviceId() {
				return deviceId;
			}
	
			public String getDeviceType() {
				return deviceType;
			}
	
			public String getEvent() {
				return event;
			}

			@Override
			public String toString() {
				return "IoTEvent [deviceId=" + deviceId + ", deviceType="
						+ deviceType + ", event=" + event + "]";
			}
			
	  }

	  /**
	   * A class that holds the Prediction state
	   */
	  private static class State implements Serializable {
		  private IoTPrediction prediction;
		
		  private State(int zScoreWindow, int wZSsoreWindow, String accessKey) {
				prediction = new IoTPredictionNonPeriodic();
		        prediction.setAccessURL(accessKey);
		        try {
					prediction.load(zScoreWindow, wZSsoreWindow);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
		  }
	
		  public IoTPrediction getPrediction() {
				return prediction;
		  }
	
		  @Override
		  public String toString() {
				return "State [prediction=" + prediction.toString() + "]";
		  }
		
	}
  
	/**
	 * UpdateStateBykey function that carries the state over the batchs.  
	 */
	private Function2<List<IoTEvent>, Optional<State>, Optional<State>> ENTRY_EXTRACTOR =
			new Function2<List<IoTEvent>, Optional<State>, Optional<State>>() {

		@Override
		public Optional<State> call(List<IoTEvent> readings, Optional<State> predictionState)
				throws Exception {

			/**
			 * Return if there is no new data from the device
			 */
			if(readings == null || readings.size() == 0) {
				return predictionState;
			}
			
			State state = predictionState.orNull();
			try {
				/**
				 * Create the state if its the first time and not defined already
				 */
				if(state == null) {
					state = predictionState.or(new State(zScoreWindow, wZScoreWindow, predictiveServiceURL));
				}
				
				/**
				 * If there are multiple events (device sends too fast compared to the Spark Streaming job batch interval),
				 * loop through one by one and predict the score
				 */
				for(IoTEvent event : readings) {
					String forecast = state.getPrediction().predict(event.getEvent());
					if (forecast != null ) { 
						int qos = 2;
						/**
						 * Let us use the following topic to publish the predicted score, so that
						 * we can view the same in RTI
						 * 
						 * iot-2/type/device_type/id/device_id/evt/result/fmt/json
						 */
				                
						String publishTopic = "iot-2/type/" + event.getDeviceType() + "/id/" + event.getDeviceId() + "/evt/result/fmt/json";
						SimpleClient client = SimpleClient.getSimpleClient();
						//Connect if its not connected already
						client.connect(serverURI, clientId, apiKey, authToken);
						SimpleClient.getSimpleClient().publish(publishTopic, qos,forecast.getBytes());
				    }
				}
			} catch (Throwable e) {
				e.printStackTrace(); 
			}
			
			return Optional.of(state);
		}
	     
	};
	    
	private IoTSparkAsServiceSample() {

    }
  
	/**
	 * Main Function that creates the Spark streaming Job with a 2 second batch interval.
	 * 
	 * @param mqtopic
	 * @param brokerUrl
	 * @param appID
	 * @param apiKey
	 * @param authToken
	 * @param sc
	 * @param interval 
	 * @throws Throwable
	 */
  	public void runPrediction(String mqtopic, String brokerUrl, String appID, 
  			String apiKey, String authToken, SparkContext sc, int interval) throws Throwable {

  		this.serverURI = brokerUrl;
  		this.apiKey = apiKey;
  		this.authToken = authToken;
  		this.clientId = appID;
		  
	    Logger.getLogger("org").setLevel(Level.OFF);
	    Logger.getLogger("akka").setLevel(Level.OFF);
	
	    
	    if(sc == null) {
		    SparkConf sparkConf = new SparkConf().setAppName("IoTSparkAsServiceSample");
		    sc = new SparkContext(sparkConf);
	    }
	    
	    JavaSparkContext jsc = JavaSparkContext.fromSparkContext(sc);
	
	    System.out.println("+++ print out the received data now:");
	    // Create the context with 2 seconds batch size
	    JavaStreamingContext jssc = new JavaStreamingContext(jsc, Durations.seconds(interval));
	    
	    //jssc.checkpoint("hdfs://9.124.102.159:9000/application/DataCollectionEngine");
	    jssc.checkpoint(".");
	    
	    // Create direct MQTT stream with topic
	    JavaReceiverInputDStream<String> messages = MQTTUtils.createStream(jssc, brokerUrl, mqtopic, appID, apiKey, authToken);
	    
	    JavaPairDStream<String, IoTEvent> mappedStream = messages.mapToPair(
	            new PairFunction<String, String, IoTEvent>() {
	
	            public Tuple2<String, IoTEvent> call(String payload) {
	            	
	            	String[] parts = payload.split(" ");
	            	String deviceType = parts[0].split("/")[2];	// DeviceType
					String deviceId = parts[0].split("/")[4];	// DeviceId
		        	
		        	return new Tuple2(deviceId, new IoTEvent(deviceType, deviceId, parts[1]));
	              }
	     });
	    
	    /**
	     * Output will be like the following,
	     * 
	     * (Device01,State [prediction={"wzscore":0.0,"name":"datacenter","temperature":17.47,"forecast":17.53,"zscore":-0.060000000000002274}])
	     */
	    JavaPairDStream<String, State> updatedLines =  mappedStream.updateStateByKey(ENTRY_EXTRACTOR);
	   
	    updatedLines.print();
	
	    jssc.start();
	    jssc.awaitTermination();
	  }
  
  /**
   * Let us use the properties if we are using the Notebook to set the list of Watson IoT Platform Properties
   */
  private static Properties arguments;
  
  public static void setConfig(String key, String value) {
	  if(arguments == null) {
		  arguments = new Properties();
	  }
	  arguments.setProperty(key, value);
  }

  /**
   * Let us use the properties if we are using the Notebook to set the list of Watson IoT Platform Properties
   */
  public static void startStreaming(SparkContext sc, int interval) {
      try {
    	  System.out.println("MQTT subscribe topics:" + arguments.getProperty("mqtopic"));
    	  System.out.println("MQTT uri:" + arguments.getProperty("uri"));
    	  System.out.println("MQTT appid:" + arguments.getProperty("appid"));
    	  System.out.println("MQTT apikey:" + arguments.getProperty("apikey"));
    	  System.out.println("MQTT authtoken:" + arguments.getProperty("authtoken"));
    	  
    	  IoTSparkAsServiceSample sample = new IoTSparkAsServiceSample();
    	  
   	   	  // Prediction cycle
   	   	  sample.zScoreWindow = Integer.parseInt(arguments.getProperty("cycle"));
   	   	  //ZScore window
   	   	  sample.wZScoreWindow = Integer.parseInt(arguments.getProperty("window"));;
   	   	  sample.predictiveServiceURL = arguments.getProperty("predictive-service-url");
          
          // Watson IoT Platform parameters to read the temperature events from the IoT Device
	      sample.runPrediction(arguments.getProperty("mqtopic"), arguments.getProperty("uri"), 
	    		   arguments.getProperty("appid"), arguments.getProperty("apikey"), arguments.getProperty("authtoken"), sc, interval);
	      
      } catch (FileNotFoundException fe) {
          fe.printStackTrace(System.err);
      } catch (MqttException e) {
          e.printStackTrace(System.err);
      } catch (CmdLineException e) {
          System.err.println(e.getMessage());
      } catch (Exception e) {
          e.printStackTrace(System.err); 
      } catch (Throwable te) {
          te.printStackTrace(System.err);
      }
  }

  /**
   * Standalone (outside bluemix) users, also the Bluemix users using spark-submit.sh command can use the following command
   * to run this application.
   * /root/bin/sparks/spark-1.4.1-bin-hadoop2.4/bin/spark-submit --class com.ibm.iot.iotspark.IoTSparkAsServiceSample 
   * --master spark://host:7077 --jars IoTSparkAsService/lib/spark-streaming-mqtt-security_2.10-1.3.0.jar 
   * IoTSparkAsServiceSample-2.0.0-SNAPSHOT-jar-with-dependencies.jar 
   * --mqtopic iot-2/type/+/id/+/evt/temperature/fmt/+ 
   * --uri ssl://organizationid.messaging.internetofthings.ibmcloud.com:8883 
   * --apikey <Your org key> 
   * --authtoken <Your Auth token>
   * --appid <application id> 
   * --window 10 
   * --cycle 10 
   * --predictive-service-url "Predictive service url>
   * @param args
   */
   public static void main(String[] args) {
       CommandLineParser parser = null;
       try {
    	   int index = 0;
    	   
    	   /**
    	    * The Spark service submit script in Bluemix passes an extra parameters, and hence we need
    	    * to skip the first one.
    	    */
    	   if(!args[0].startsWith("--")) {
    		   index = 1;
    	   }
    	   String[] reducedArgs = new String[args.length - index];
    	   for(int i = index; i < args.length; i++) {
    		   reducedArgs[i-index] = args[i];
    	   }
           parser = new CommandLineParser();
           
           parser.parse(reducedArgs);

           System.out.println("MQTT subscribe topics:" + parser.getMqttTopic());
         
           IoTSparkAsServiceSample sample = new IoTSparkAsServiceSample();
           
           sample.zScoreWindow = parser.getPredictionCycle();
           sample.wZScoreWindow = parser.getZScoreWindow();
           sample.predictiveServiceURL = parser.getPredictiveServiceURL();
    
           /**
            * The Spart streaming MQTT util will require the Watson IoT Platform details so that it can receive the data
            * from Watson IoT Platform.
            */
           sample.runPrediction(parser.getMqttTopic(), parser.getServerURI(), parser.getAppId(), parser.getApiKey(), parser.getAuthToken(), null, 4);
       } catch (FileNotFoundException fe) {
           fe.printStackTrace(System.err);
       } catch (MqttException e) {
           e.printStackTrace(System.err);
       } catch (CmdLineException e) {
           System.err.println(e.getMessage());
           parser.printUsage(System.err);
       } catch (Exception e) {
           e.printStackTrace(System.err); 
       } catch (Throwable te) {
           te.printStackTrace(System.err);
       }
   }

}
