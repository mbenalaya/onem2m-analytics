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
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wink.json4j.JSON;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

@SuppressWarnings("serial")
public class IoTPredictionNonPeriodic extends IoTPrediction {
	
	/**
	 * A formatter for ISO 8601 compliant timestamps.
	 */
	protected static final DateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private JSONObject data = null;
    private int cycle = 1;
    private int windowsize = 0;
    
    private JSONArray pa = new JSONArray();
	private int count = 0;
	private String pdt;

	private IoTZScore zscoreObj = null;
	
    IoTPredictionNonPeriodic() {
    	System.out.println("Creating new instance of IoTPredictionNonPeriodic");
    	zscoreObj = new IoTZScore();
    }
    
    /**
     * This is a test method used for verifying the configuration parameters.
     * @param predictiveServiceURL 
     * @throws Exception 
     * @throws JSONException 
     * @throws FileNotFoundException 
     */
    public static boolean testScoring(String predictiveServiceURL) throws FileNotFoundException, JSONException, Exception {
    	IoTPredictionNonPeriodic p = new IoTPredictionNonPeriodic();
    	p.load(10, 10);
    	p.setAccessURL(predictiveServiceURL);
    	String ret = post(p.getAccessURL(), p.data.toString());
    	
    	if(ret.contains("Failed to score")) {
    		System.out.println("Looks like an issue with the URL, Please check the AccessKey and context id!!");
    		return false;
    	} else {
    		System.out.println("Connection to Predictive Analytics service is proper and able to invoke the service successfully");
    		return true;
    	}
    }

    /*
     * Load historical data that required by the modeler and build a JSON object.
     * 
     */
    public void load(int number) throws FileNotFoundException, JSONException, Exception {
        try {
	        InputStream dataStream = this.getClass().getResourceAsStream("/historicaldata.json");
	        data = new JSONObject(dataStream);
		    cycle = number;
    	} catch(Exception e) {
    		e.printStackTrace();
    		// Ignore - the exception might be thrown incase if there is no historic data
    	}
    }

    /*
     * Load historical data that required by the modeler and build a JSON object.
     * 
     */
    public  void load(int number, int size) throws FileNotFoundException, JSONException, Exception {
        try {
        	InputStream dataStream = this.getClass().getResourceAsStream("/historicaldata.json");
	        data = new JSONObject(dataStream);
		    windowsize = size;
		    zscoreObj.setWindowSize(size);
		    cycle = number;
    	} catch(Exception e) {
    		e.printStackTrace();
    		// Ignore - the exception might be thrown incase if there is no historic data
    	}
    }
        
    public void appendDataSet(JSONObject obj) throws JSONException {
        double d = obj.getDouble("temperature");

        try {
	        JSONArray ja = (JSONArray) data.get("data");
	        int snum = ja.length();
	        JSONArray na = new JSONArray();
	        //na.put(name);
	        na.put(d);
	        na.put(snum+1);
	        ja.put(na);
        } catch(Exception e) {
        	// Ignore - the exception might be thrown incase if there is no historic data
        }        
        return;
    	
    }

    /*
     * Make prediction based on a input string. The string has to be 
     * in JSON format
     */
    public String predict(String s) throws Exception {
    	// Workers will have to create the data object now
    	if(data == null) {
    		load(this.cycle, this.windowsize);
    	}
    	
       Double forecast = 0.0;
       JSONObject pdt = new JSONObject();
       
       if (s == null)
           return null;
      
       // The Watson IoT Java client library sends the data like {d: {payload}}
       JSONObject obj = (JSONObject)JSON.parse(s);
       if(obj.has("d")) {
    	   obj = obj.getJSONObject("d");
       }
       //put device temperature string into prediction dataset
       if (obj.has("name")&&obj.get("name") != null && 
           obj.has("temperature") && obj.get("temperature") != null )  {
           appendDataSet(obj);
           
           /*
            * if count is 1, do prediction. the co-related forecast is 1.
            * else get forecast from the previous prediction using count value
            */
           if (this.count == 1) {
        	   
	           String ret = post(this.getAccessURL(), data.toString());
	
	          /* return from rest call will be the following:
	           * [{"header":["TEMPERATURE","field3","$TI_TimeIndex","$TI_TimeLabel","$TI_Cycle","$TI_Period","$TI_Future","$TS-TEMPERATURE","$TSLCI-TEMPERATURE","$TSUCI-TEMPERATURE"],
                   * [{"header":["TEMPRATURE","SEQ","$TI_TimeIndex","$TI_TimeLabel","$TI_Period","$TI_Future","$TS-TEMPRATURE","$TSLCI-TEMPRATURE","$TSUCI-TEMPRATURE"]
	           *   "data":[
                   *      [17.69,1,1,"Period 1",1,0,17.68999888446639,17.583592583375612,17.79640518555717],
                   *      [17.66,2,2,"Period 2",2,0,17.689999999958516,17.583593698867737,17.796406301049295],
                   *      [17.69,3,3,"Period 3",3,0,17.660001115575092,17.553594814484313,17.76640741666587]
	           *      .....
	           *   ]
	           * }]
	           *
	           */
	
	           //remove the first "[" and last "]" from return string
	           if (ret.length() > 0 && ret.charAt(0) == '[' && ret.charAt(ret.length()-1) == ']') {
	               ret = ret.substring(1, ret.length()-1);
	           }
	           JSONObject prediction = new JSONObject(ret);	
	           pa = (JSONArray) prediction.get("data");
	
	           if (pa.length() < cycle) {
	               System.out.println("Wrong prediction output with " + pa.length() + " entries, while prediction cycle is " + cycle);
	               throw new JSONException("Wrong prediction output with " + pa.length() + " entries, while prediction cycle is " + cycle);
	          } 
          }
           
          //get corresponded prediction from array
          if (pa.isNull(this.count-1) == true) {
        	  System.out.println("the JSONArray is empty with index " + (this.count-1));
        	  
          } else {
	          JSONArray pj = pa.getJSONArray(this.count-1);
	          String pstr = pj.toString();
	          System.out.println("the string is *" + pstr + "*"); 
	          String[] parts = pstr.split(",");
	          //$TS-TEMPERATURE is the 7th in the array
	          //However 5th will be splitted into 2
	          if (parts.length < 7) {
	              forecast = 0.0;
	          } else {
	              forecast = Double.parseDouble(parts[6]);
	          }
	
	          double current = obj.getDouble("temperature");
              Double zscore = zscoreObj.zScore(current-forecast);
             
              pdt = new JSONObject(obj);
              pdt.put("forecast", forecast);
              if(zscore == null || zscore.isNaN() || zscore == 0) {
            	  // For some reason the Apache wink is not including the decimal point if its 0
        		  // and this creates a problem in defining the schema correctly in the notebook
        		  // i.e it picks up the data as long insteadof double by looking at one record
        		  // and fails eventually, so use a workaround as shown below,
            	  pdt.put("zscore", 0.000000000000001);
              } else {
            	// Same work-around here as well
        		  if(!zscore.toString().contains(".")) {
        			  zscore = Double.parseDouble(zscore + ".000000000000001");
        		  }
            	  pdt.put("zscore", zscore.doubleValue());
              }
              
              if (windowsize > 0) {
            	  Double wzscore = zscoreObj.windowZScore(current-forecast);
            	  if(wzscore == null || wzscore.isNaN() || wzscore == 0) {
            		  // For some reason the Apache wink is not including the decimal point if its 0
            		  // and this creates a problem in defining the schema correctly in the notebook
            		  // i.e it picks up the data as long insteadof double by looking at one record
            		  // and fails eventually, so use a workaround as shown below,
            		  pdt.put("wzscore", 0.000000000000001);
            	  } else {
            		  // Same work-around here as well
            		  if(!wzscore.toString().contains(".")) {
            			  wzscore = Double.parseDouble(wzscore + ".000000000000001");
            		  }
            		  pdt.put("wzscore", wzscore.doubleValue());
            	  }
              } 
              System.out.println("[" + this.count +"] JSON to RTI:" + pdt.toString());
          }
          
          
          //update count
          this.count++;
          
          if (this.count > cycle) {
        	  //set count back to 1
        	  this.count = this.count-cycle;
          }
          this.pdt = pdt.toString();
          return pdt.toString();
          
       } 

       return null;
    }

    public String toString() {
    	return this.pdt;
    }
    /*
     * For testing
     *
     *
    public static void main(String[] args) {

      final List<String> input = Arrays.asList(
    		  "{\"name\":\"datacenter\",\"temperature\":17.47}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.47}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.44}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.44}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.38}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.44}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.47}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.5}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.5}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.5}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.5}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.5}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.56}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.56}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.53}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.5}",
		      "{\"name\":\"datacenter\",\"temperature\":17.53}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.5}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.53}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.47}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.47}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.47}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.47}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.41}",
    		  "{\"name\":\"datacenter\",\"temperature\":17.42}"
      );
      int cycle = Integer.parseInt(args[0]);
      System.out.println("prediction cycle is " + cycle);
      IoTPrediction prediction = new IoTPrediction();

      try {
    	  prediction.load(cycle);
    	  
    	  for (int i = 0; i < input.size(); i++) {
              //String pdt = prediction.predict(newline);
              System.out.println("input index:" + i);
              prediction.predict(input.get(i));
    	  }
          fos.close();
      } catch (JSONException e) {
          e.printStackTrace();
      } catch (Exception ex) {
          ex.printStackTrace();
      }
    }
    */    

}

