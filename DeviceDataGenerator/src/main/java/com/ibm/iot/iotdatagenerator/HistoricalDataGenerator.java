/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iot.iotdatagenerator;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * <p>This class quickly fills the Cloudant NoSQL DB with the resultant data that will
 * act as a historical data. </br>
 * 
 * One can use the Jupyter Notebook to analyze the data in Spark. Refer to the 
 * <a href="https://github.com/ibm-watson-iot/predictive-analytics-samples/blob/master/Notebook/TimeseriesDataAnalysis.ipynb">Notebook</a> for more information.</p> 
 */
public class HistoricalDataGenerator
{
	private final static String DATASET_FILE_NAME = "/datacenter1700_nocycle10rebuild50zscorewindow10.csv";
	
   public static void main(String [] args) throws JsonSyntaxException, IOException
   {
	   	Database backupAndRestoreDB;
	   	
	   	StringBuilder sb = new StringBuilder();
	   	
		if(args.length < 2) {
			System.err.println("Please run the application with Cloudant DB username & Password as follows\n");
			System.out.println("BackupAndRestoreApplicationSample <username> <password>");
			System.exit(-1);
		}
		
		String username = args[0];
		String password = args[1];

		
	   	sb.append("https://")
		  .append(username)
		  .append(":")
		  .append(password)
		  .append("@")
		  .append(username)
		  .append(".cloudant.com");
		
        CloudantClient client = new CloudantClient(sb.toString(), username, password);
        
	   	System.out.println("Connected to Cloudant");
		System.out.println("Server Version: " + client.serverVersion());

		backupAndRestoreDB = client.database("recipedb", true);

	   
	   	SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		JsonParser parser = new JsonParser();
		// January 01 2016
		Date date = new Date(116, 01, 01);
		// set the date to Jan 18th
		date.setDate(18);
		date.setHours(0);
		date.setMinutes(0);
		int time = 2;
		long count = 0;
		for(int i = 0; i < 20; i++) {
			InputStream is = IoTDataGenerator.class.getResourceAsStream(DATASET_FILE_NAME);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			// if no more lines the readLine() returns null
			while ((line = br.readLine()) != null) {
				//name,temperature,forecast,zscore,wzscore,index
				// datacenter,17.47,17.339300385234175,0.06446157222846693,0.06446157222846693,2
				JsonObject json = new JsonObject();
				String[] payload = line.split(",");
	
				date.setMinutes(time);
				time = time + 2;
				if(time > 60) {
					time = 0;
				}
	
				
				Data data = new Data(payload[0], Double.parseDouble(payload[1]), 
						Double.parseDouble(payload[2]), Double.parseDouble(payload[3]),
						Double.parseDouble(payload[4]),
						dateFormatGmt.format(date).toString(),Integer.parseInt((payload[5])));
				

				Response response = backupAndRestoreDB.save(data);
				if(count % 100 == 0) {
					System.out.println("Updated "+ count + " documents so far..");
				}
			}
		}
   }
   
   private static class Data {
	   private String name;
	   private double temperature;
	   private double forecast;
	   private double zscore;
	   private double wzscore;
	   private String timestamp;
	   
	public Data(String name, double temperature, double forecast,
			double zscore, double wzscore, String timestamp, int index) {
		super();
		this.name = name;
		this.temperature = temperature;
		this.forecast = forecast;
		this.zscore = zscore;
		this.wzscore = wzscore;
		this.timestamp = timestamp;
	}
	   
	   
   }
   
   
}
