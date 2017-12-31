/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Li Lin - Initial Contribution
 
 Mahdi Ben Alaya - interworking with oneM2M platform (Sensinov)
 Kais Ben Youssed - interworking with oneM2M platform (Sensinov)
 *****************************************************************************
 *
 */
package analytics;

import java.io.OutputStream;
import java.io.PrintStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class CommandLineParser {

	private static final String DEFAULT_MQTT_TOPICS = "iot-2/type/+/id/+/evt/temperature/fmt/+";
	private static final String DEFAULT_MQTT_SERVER_URI = "ssl://d932lf.messaging.internetofthings.ibmcloud.com:8883";
	private static final String DEFAULT_APP_ID = "a:d932lf:hammadi3";
	private static final String DEFAULT_API_KEY = "a-d932lf-drzlj06hnl";
	private static final String DEFAULT_AUTH_TOKEN = "y6E6a3W!u(NWeeD(DY";
	private static final int DEFAULT_PREDICTION_CYCLE = 10;
	private static final int DEFAULT_ZSCORE_WINDOW = 10;
	
	@Option(name="--appid", usage="Unique ID of the application")
	private String appId = DEFAULT_APP_ID;

	@Option(name="--uri", usage="MQTT Server URI")
	private String serverURI = DEFAULT_MQTT_SERVER_URI;

	@Option(name="--mqtopic", usage="IoTF MQTT topic")
	private String mqttTopic = DEFAULT_MQTT_TOPICS;
	
	@Option(name="--predictive-service-url", usage="Predictiv Analytics Service URL")
	private String pURL = "https://ibm-watson-ml.eu-gb.bluemix.net/pm/v1/score/nocycle20rebuid50?accesskey=mSNymX1ayiKdtyC5kObHMr0lDFyPFNnnOiT6XsH5ZSAz9Yaqc0WKx692OSIUZAOnpvelDBj2EWArRQzCnErs5G6xF7OPG2R5H0oB0w5syog=";
	
	@Option(name="--apikey", usage="IoTF API-Key")
	private String apiKey = DEFAULT_API_KEY;
	
	@Option(name="--authtoken", usage="IoTF Auth-Token")
	private String authToken = DEFAULT_AUTH_TOKEN;	
	
	@Option(name="--cycle", usage="Prediction cycle")
	private int cycle = DEFAULT_PREDICTION_CYCLE;	

	@Option(name="--window", usage="ZScore window")
	private int window = DEFAULT_ZSCORE_WINDOW;	
	
	@Option(name="--help", aliases="-h", usage="Show help")
	private boolean showHelp = false;
	
	private CmdLineParser parser = new CmdLineParser(this);
	
	public String getAppId() {
		return appId;
	}

	public String getServerURI() {
		return serverURI;
	}

	public String getMqttTopic() {
		return mqttTopic;
	}

	public String getApiKey() {
		return apiKey;
	}
	
	public String getAuthToken() {
		return authToken;
	}
	
	public int getPredictionCycle() {
		return cycle;
	}

	public int getZScoreWindow() {
		return window;
	}
	
	public void parse(String[] args) throws CmdLineException {
		parser.parseArgument(args);
		if (showHelp) {
			printUsage(System.out);
			System.exit(0);
		}
	}

	public void printUsage(OutputStream out) {
		PrintStream stream = new PrintStream(out);
		stream.println("java " + IoTSparkAsServiceSample.class.getName() + " [options...]");
		parser.printUsage(out);
	}

	public String getPredictiveServiceURL() {
		return this.pURL;
	}
}
