package com.ibm.iot.iotspark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;

import org.apache.wink.json4j.JSON;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;


@SuppressWarnings("serial")
public class IoTPredictionPeriodic extends IoTPrediction{
    /* comments out the following 2 lines after testing */
	private final static String DataPath = "/usr/llin/data/historicaldata.json.labTemp";
	private static JSONObject data = new JSONObject();
	private static FileOutputStream fos;
	private static FileChannel fileChannel;
	private int cycle = 1;
	private int count = 1;
	private int index = 1;
	private int windowsize = 0;

	private JSONArray pa = new JSONArray();
	
	/*
	 * Load historical data that required by the modeler and build a JSON object.
	 *
	 */
	public  void load(int number) throws FileNotFoundException, JSONException, Exception {
	    // load historical data
	    File initialFile = new File(DataPath);
	    InputStream dataStream = new FileInputStream(initialFile);
	    data = new JSONObject(dataStream);
	        File fout = new File("out.txt");
	        fos = new FileOutputStream(fout);
	        fileChannel = fos.getChannel();
	
	        cycle =number;
	        writeFile("name,temperature,forecast,zscore,index\n", fileChannel);
	}

	/*
	 * Load historical data that required by the modeler and build a JSON object.
	 *
	 */
	public  void load(int number, int size) throws FileNotFoundException, JSONException, Exception {
	    // load historical data
	    File initialFile = new File(DataPath);
	    InputStream dataStream = new FileInputStream(initialFile);
	    data = new JSONObject(dataStream);
	    File fout = new File("out.txt");
	    fos = new FileOutputStream(fout);
	    fileChannel = fos.getChannel();
	    windowsize = size;
	    IoTZScore.setWindowSize(size);
	    cycle = number;
	    writeFile("name,temperature,forecast,zscore,wzscore,index\n", fileChannel);
	}

	public void appendDataSet(JSONObject obj) throws JSONException {
	    String name = obj.getString("name");
	    double d = obj.getDouble("temperature");
	
	    JSONArray ja = (JSONArray) data.get("data");
	    int snum = ja.length();
	
	    JSONArray na = new JSONArray();
	    na.put(name);
	    na.put(d);
	    na.put(snum+1);
	    ja.put(na);
	
	    return;
	
	}

	/*
	 * Make prediction based on a input string. The string has to be
	 * in JSON format
	 */
	public String predict(String s) throws Exception {
	    Double forecast = 0.0;
	    JSONObject pdt = new JSONObject();
	
	    if (s == null)
	        return null;
	
	    //put device temperature string into prediction dataset
	    JSONObject obj = (JSONObject)JSON.parse(s);
	    if (obj.has("name")&&obj.get("name") != null &&
	        obj.has("temperature") && obj.get("temperature") != null )  {
	        appendDataSet(obj);
	
	        /*
	         * if count is 1, do prediction. the co-related forecast is 1.
	         * else get forecast from the previous prediction using count value
	         */
	        if (this.count == 1) {	
	            //System.out.println(data.toString());
	            String ret = post(getAccessURL(), data.toString());
	
	            /* return from rest call will be the following:
	             * [{"header":["TEMPERATURE","field3","$TI_TimeIndex","$TI_TimeLabel","$TI_Cycle","$TI_Period","$TI_Future","$TS-TEMPERATURE","$TSLCI-TEMPERATURE","$TSUCI-TEMPERATURE"],
	             *   "data":[
	             *      [null,null,683,"Cycle 5, Period 11",5,11,1,107.05262853557957,106.21945108364707,107.88580598751207],
	             *      [null,null,684,"Cycle 5, Period 12",5,12,1,107.50214097296886,106.66893819492448,108.33534375101324],
	             *      [null,null,685,"Cycle 5, Period 13",5,13,1,107.52252232154218,106.68929421812965,108.35575042495472],
	             *      [null,null,686,"Cycle 5, Period 14",5,14,1,107.3271619786097,106.49390855057267,108.16041540664672],
	             *      .....
	             *   ]
	             * }]
	             *
	             */
	
                 //remove the first "[" and last "]" from return string
                 if (ret.length() > 0 && ret.charAt(0) == '[' && ret.charAt(ret.length()-1) == ']')
                     ret = ret.substring(1, ret.length()-1);
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
	             //System.out.println("the string is *" + pstr + "*");
	             String[] parts = pstr.split(",");
	             //$TS-TEMPERATURE is the 7th in the array
	             //However 5th will be splitted into 2
	             if (parts.length < 7) {
	                 forecast = 0.0;
	             } else {
	                 forecast = Double.parseDouble(parts[8]);
	             }
	
	             double current = obj.getDouble("temperature");
	             double zscore = IoTZScore.zScore(current-forecast);
	
	             pdt = new JSONObject();
	             pdt.put("name", obj.getString("name"));
	             pdt.put("temperature", current);
	             pdt.put("forecast", forecast);
	             pdt.put("zscore", zscore);
	             if (windowsize > 0) {
	                 double wzscore = IoTZScore.windowZScore(current-forecast);
	                 pdt.put("wzscore", wzscore);
	                 writeFile(obj.getString("name") + ',' + current + ',' + forecast + ',' + zscore + ',' + wzscore + ',' + index + '\n', fileChannel);
	             } else {
	                 writeFile(obj.getString("name") + ',' + current + ',' + forecast + ',' + zscore + ','+ index + '\n', fileChannel);
	             }
	             System.out.println("[" + this.count +"] JSON to RTI:" + pdt.toString());
	             index++;
	         }
	
	
	         //update count
	         this.count++;	
	         if (this.count > cycle) {
	             //set count back to 1
	              this.count = this.count-cycle;
	         }
	         return pdt.toString();
	
	     }	
	     return null;
	}
	
	
	/*
	 * For testing
	 *
	public static void main(String[] args) {	
	    final List<String> input = Arrays.asList(
            "{\"name\":\"ENGINE3\",\"temperature\":97.2}",
            "{\"name\":\"ENGINE3\",\"temperature\":98.07037087}",
            "{\"name\":\"ENGINE3\",\"temperature\":98.46987298}",
            "{\"name\":\"ENGINE3\",\"temperature\":99.16446609}",
            "{\"name\":\"ENGINE3\",\"temperature\":100.5}",
            "{\"name\":\"ENGINE3\",\"temperature\":101.3059048}",
            "{\"name\":\"ENGINE3\",\"temperature\":103}",
            "{\"name\":\"ENGINE3\",\"temperature\":103.7940952}",
            "{\"name\":\"ENGINE3\",\"temperature\":105.1}",
            "{\"name\":\"ENGINE3\",\"temperature\":106.4355339}",
            "{\"name\":\"ENGINE3\",\"temperature\":107.230127}",
            "{\"name\":\"ENGINE3\",\"temperature\":107.4296291}",
            "{\"name\":\"ENGINE3\",\"temperature\":107.5}",
            "{\"name\":\"ENGINE3\",\"temperature\":107.1296291}",
            "{\"name\":\"ENGINE3\",\"temperature\":106.530127}",
            "{\"name\":\"ENGINE3\",\"temperature\":106.2355339}",
            "{\"name\":\"ENGINE3\",\"temperature\":105}",
            "{\"name\":\"ENGINE3\",\"temperature\":103.7940952}",
            "{\"name\":\"ENGINE3\",\"temperature\":102.2}",
            "{\"name\":\"ENGINE3\",\"temperature\":101.7059048}",
            "{\"name\":\"ENGINE3\",\"temperature\":100}",
            "{\"name\":\"ENGINE3\",\"temperature\":98.66446609}",
            "{\"name\":\"ENGINE3\",\"temperature\":97.96987298}",
            "{\"name\":\"ENGINE3\",\"temperature\":98.17037087}"
	    );
	    int pcycle = Integer.parseInt(args[0]);
	    System.out.println("prediction cycle is " + pcycle);
	    IoTPredictionPeriodic prediction = new IoTPredictionPeriodic();
	
	    try {
	        prediction.load(pcycle, 10);
	
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
	
