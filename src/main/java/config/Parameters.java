/*******************************************************************************
 * Copyright (c) 2017 Sensinov (www.sensinov.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package config;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class Parameters {
	public static String watsonApiKey;
	public static String watsonToken;
	public static String watsonUri;
	public static String watsonAppID;
	public static String mqttTopics;
	public static int predictionCycle;
	public static int zscoreWindow;
	public static String predictiveServiceUrl;

	
	public Parameters(Properties props){
				
		watsonApiKey = props.getProperty("API_KEY");
		watsonToken = props.getProperty("AUTH_TOKEN");
		watsonUri = props.getProperty("MQTT_SERVER_URI");
		watsonAppID=props.getProperty("APP_ID");
		mqttTopics=props.getProperty("MQTT_TOPICS");
		predictionCycle=Integer.parseInt(props.getProperty("PREDICTION_CYCLE"));
		zscoreWindow=Integer.parseInt(props.getProperty("ZSCORE_WINDOW"));
		predictiveServiceUrl=props.getProperty("PREDICTIVE_SERVICE_URL");
				
		
		}
	
	
}