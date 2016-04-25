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
package com.ibm.iot.iotdatagenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.device.DeviceClient;

@SuppressWarnings("serial")
public class IoTDataGenerator implements Serializable {
	private final static String PROPERTIES_FILE_NAME = "/device.properties";
	private final static String DATASET_FILE_NAME = "/testDataSet";
	
	private IoTDataGenerator() {

	}
  
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws IOException, ParseException {
		
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(IoTDataGenerator.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		DeviceClient myClient = null;
		try {
			//Instantiate and connect to IBM Watson IoT Platform
			myClient = new DeviceClient(props);
			myClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
			
		do {
			InputStream is = IoTDataGenerator.class.getResourceAsStream(DATASET_FILE_NAME);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			JsonParser parser = new JsonParser();
			// if no more lines the readLine() returns null
			while ((line = br.readLine()) != null) {
				// the line will be {"name":"datacenter","temperature":17.47}
				// The Watson IoT Java client library will add the timestamp while publishing
				JsonObject event = parser.parse(line).getAsJsonObject();
				event.addProperty("timestamp", dateFormatGmt.format(new Date()).toString());
				
				System.out.println(event);
				// publish the temperature with QoS 2
				myClient.publishEvent("temperature", event, 2);
				try {
					Thread.sleep(1800);
				} catch (InterruptedException e) {
				} 
			}
			br.close();
		} while(true); // publish ever
	
	}

}
