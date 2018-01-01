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
  
 Mahdi Ben Alaya - interworking with oneM2M platform (Sensinov)
 Kais Ben Youssef - interworking with oneM2M platform (Sensinov)
 *****************************************************************************
 *
 */
package core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import scala.Tuple2;
import mqtt.SimpleClient;

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

import config.Parameters;

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
public class Core implements Serializable {
	  private final static String PROPERTIES_FILE_NAME = "config.ini";
	  static Logger logger = Logger.getLogger(Core.class.getName());
	  
	  
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
					if (forecast != null && !forecast.equals("{}")) { 
						int qos = 0;
						/**
						 * Let us use the following topic to publish the predicted score, so that
						 * we can view the same in RTI
						 * 
						 * iot-2/type/device_type/id/device_id/evt/result/fmt/json
						 */
						String publishTopic = "iot-2/type/"+ event.getDeviceType() + "/id/" + event.getDeviceId() + "/evt/result/fmt/json";
						SimpleClient client = SimpleClient.getSimpleClient();
						//Connect if its not connected already
						
			
						client.connect(serverURI, clientId, apiKey, authToken);
			
						client.publish(publishTopic, qos,forecast.getBytes());
				
				    }
				}
			} catch (Throwable e) {
				e.printStackTrace(); 
			}
			
			return Optional.of(state);
		}
	     
	};
	    
	private Core() {

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
  		
  		/**
  		 * Let us verify the Watson IoT and Predictive Analytics service connectivity with the data given 
  		 */
  		SimpleClient client = SimpleClient.getSimpleClient();

  		//Connect if its not connected already
  		System.out.println("Testing the connectivity to Watson IoT Platform ... ");
		client.connect(serverURI, clientId, apiKey, authToken);
		//client.disconnect();
		
		System.out.println("Able to connect to Watson IoT Platform successfully !!");
		
		// Testing the connectivity to Predictive Analytics service
		System.out.println("Testing the connectivity to Predictive Analytics service ... ");
		if(IoTPredictionNonPeriodic.testScoring(this.predictiveServiceURL) == false) {
			return;
			// don't run the job as we are not able to access the Predictive Analytics service
		}
		  
	    Logger.getLogger("org").setLevel(Level.OFF);
	    Logger.getLogger("akka").setLevel(Level.OFF);
	
	    
	    if(sc == null) {
		    SparkConf sparkConf = new SparkConf().setAppName("IoTSparkAsServiceSample").setMaster("local[*]");
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
					//System.out.println(payload);
	            	String[] parts = payload.split(" " , 2);
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
	   
	   Properties props = new Properties();
		try {
			props.load(new FileInputStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(1);
		}
		new Parameters(props);
	   
     
       try {
    	 
           System.out.println("MQTT subscribe topics:" + Parameters.mqttTopics);
           System.out.println("MQTT uri:" + Parameters.watsonUri);
     	   System.out.println("MQTT appid:" + Parameters.watsonAppID);
     	  System.out.println("MQTT apikey:" + Parameters.watsonApiKey);
     	  System.out.println("MQTT authtoken:" + Parameters.watsonToken);
			
     	 
			
           Core sample = new Core();
           
           sample.zScoreWindow = Parameters.predictionCycle;
           sample.wZScoreWindow = Parameters.zscoreWindow;
           sample.predictiveServiceURL = Parameters.predictiveServiceUrl;
    
           /**
            * The Spart streaming MQTT util will require the Watson IoT Platform details so that it can receive the data
            * from Watson IoT Platform.
            */

           sample.runPrediction(Parameters.mqttTopics, Parameters.watsonUri, Parameters.watsonAppID, Parameters.watsonApiKey, Parameters.watsonToken, null, 4);
           
         

			
       } catch (FileNotFoundException fe) {
           fe.printStackTrace(System.err);
       } catch (MqttException e) {
           e.printStackTrace(System.err);
       } catch (Exception e) {
           e.printStackTrace(System.err); 
       } catch (Throwable te) {
           te.printStackTrace(System.err);
       }
   }

}
