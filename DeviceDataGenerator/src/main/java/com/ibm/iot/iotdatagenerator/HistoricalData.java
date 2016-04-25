package com.ibm.iot.iotdatagenerator;


import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class HistoricalData
{
	private final static String DATASET_FILE_NAME = "/datacenter1700_nocycle10rebuild50zscorewindow10.csv";
	
   public static void main(String [] args) throws JsonSyntaxException, IOException
   {
	   	Database backupAndRestoreDB;
	   	
	   	CloudantClient client = new CloudantClient("https://86d043e2-f688-4b33-9b2d-65537f831214-bluemix:3577f8262a62fd0f62b" +
	   			"f1bc1394271f8a63e199d58c5b81768da6a3d32e74656@" +
	   			"86d043e2-f688-4b33-9b2d-65537f831214-bluemix.cloudant.com", "86d043e2-f688-4b33-9b2d-65537f831214-bluemix", 
	   			"3577f8262a62fd0f62bf1bc1394271f8a63e199d58c5b81768da6a3d32e74656");
	   	System.out.println("Connected to Cloudant");
		System.out.println("Server Version: " + client.serverVersion());

		backupAndRestoreDB = client.database("recipedb", true);

	   
	   	SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		JsonParser parser = new JsonParser();
		Date date = new Date(116, 01, 01);
		date.setDate(18);
		date.setHours(0);
		date.setMinutes(0);
		int time = 2;
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
