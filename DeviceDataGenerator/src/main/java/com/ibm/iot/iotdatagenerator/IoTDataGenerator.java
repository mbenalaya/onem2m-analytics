package com.ibm.iot.iotdatagenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.kohsuke.args4j.CmdLineException;

import org.apache.wink.json4j.JSONObject;

import com.ibm.iot.iotdatagenerator.CommandLineParser;


@SuppressWarnings("serial")
public class IoTDataGenerator implements Serializable {
  static Logger logger = Logger.getLogger(IoTDataGenerator.class.getName());
  private static SimpleClient mqttClient = new SimpleClient();
 
  private static String mqtopic;

  private IoTDataGenerator() {

  }
  
  

  /**
   * @param args
   */
   public static void main(String[] args) {
       CommandLineParser parser = null;
       try {
	       parser = new CommandLineParser();
	       parser.parse(args);
	       mqttClient.connect(parser.getServerURI(), parser.getClientId(), parser.getUser(), parser.getPassword());
	       mqtopic = parser.getMqttTopic();
	       System.out.println("MQTT publish topics:" + mqtopic);
	
	       File file = new File(parser.getDataPath());
	       FileReader fileReader = new FileReader(file);
	
	       BufferedReader br = new BufferedReader(fileReader);
	
	       String line = null;
	
	       // if no more lines the readLine() returns null
	       while ((line = br.readLine()) != null) {
	           //send line
	           int qos = 0;
             String topic = mqtopic;

             try {
                 JSONObject msg = new JSONObject(line);
                 double temperature = msg.getDouble("temperature");
                 double forecast = msg.getDouble("forecast");
                 double zscore = msg.getDouble("zscore");
                 double wzscore = msg.getDouble("wzscore");
                 if (Math.abs(zscore) > 3.0 && Math.abs(wzscore) > 3.0) {
                     topic = topic.replaceAll("/fmt/", "-alert/fmt/");
                 }
             } catch (Exception e) {
             } 
             
             System.out.println(line);
	         mqttClient.publish(topic,qos,line.getBytes());
	         Thread.sleep(4000); // Wait 4 seconds
	       }
	       br.close();
	   } catch (MqttException me) {
	       me.printStackTrace(System.err);
	   } catch (CmdLineException ce) {
	       System.err.println(ce.getMessage());
	       parser.printUsage(System.err);
	   } catch(InterruptedException ex) {
	       ex.printStackTrace(System.err); 
	   } catch(FileNotFoundException fe) {
	       fe.printStackTrace(System.err); 
	   } catch (Exception e) {
	       e.printStackTrace(System.err); 
	   } catch (Throwable te) {
	       te.printStackTrace(System.err);
	   }
   }

}
