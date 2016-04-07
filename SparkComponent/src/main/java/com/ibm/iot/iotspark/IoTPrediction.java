/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Li Lin - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iot.iotspark;

import java.io.Serializable;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

/**
 * 
 * A base class that maintains the forecasted scores and reduces the load to the Predictive Analytics service.
 * The Analytics serive by default returns 50 forecast values, this class stores the forecasted values and
 * for sometime (configurable using the parameter --cycle) before the next ReST invocation.
 *
 */
@SuppressWarnings("serial")
public abstract class IoTPrediction implements Serializable {
	private String accessURL;
	
	/**
	 * Access key required to call the Predictive Analytics service in Bluemix
	 * @param accesskey
	 */
	public void setAccessURL(String accessURL) {
		this.accessURL = accessURL;
	}

	public String getAccessURL() {
		return accessURL;
	}
	
    public  IoTPrediction() {
    	
    }
    
    /**
     * Makes ReST call to the Predictive Analytics service with the given payload and responds with the predicted score.
     * 
     * @param pURL
     * @param payload
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static java.lang.String post(java.lang.String pURL, java.lang.String payload) throws ClientProtocolException, IOException {

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(pURL);
        StringEntity input = new StringEntity(payload);
        input.setContentType("application/json");
        post.setEntity(input);

        HttpResponse response = client.execute(post);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder out = new StringBuilder();
        java.lang.String line;
        while ((line = rd.readLine()) != null) {
        //    System.out.println(line);
            out.append(line);
        }
        
        System.out.println(out.toString());   //Prints the string content read from input stream
        rd.close();
        return out.toString();
    }
    
    /*
     * Add new data entry into model data set for next prediction
     */
    public abstract void appendDataSet(JSONObject obj) throws JSONException;

    /*
     * Make prediction based on a input string. 
     * Each prediction algorithm will be different
     */
    public abstract String predict(String s) throws Exception;
    
    /*
     * Load historical data for modeler
     */
    public abstract void load(int number) throws FileNotFoundException, JSONException, Exception;
    
    /*
     * Load historical data for modeler and set modeler prediction cycle
     */
    public abstract void load(int number, int size) throws FileNotFoundException, JSONException, Exception;

}

