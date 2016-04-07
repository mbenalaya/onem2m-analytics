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

import java.io.OutputStream;
import java.io.PrintStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class CommandLineParser {
	private static final String DEFAULT_MQTT_TOPICS = "iot-2/type/+/id/+/evt/+/fmt/+";
	private static final String DEFAULT_MQTT_SERVER_URI = "ssl://o672lp.messaging.internetofthings.ibmcloud.com:8883";
	private static final String DEFAULT_APP_ID = "a:o672lp:IoTSparkAsServiceSample123";
	private static final String DEFAULT_API_KEY = "a-o672lp-ftvqqaw5kn";
	private static final String DEFAULT_AUTH_TOKEN = "VPLjdf1o4aw0wML1)i";
	private static final int DEFAULT_PREDICTION_CYCLE = 1;
	private static final int DEFAULT_ZSCORE_WINDOW = 0;


	@Option(name="--appid", usage="Unique ID of the application")
	private String appId = DEFAULT_APP_ID;

	@Option(name="--uri", usage="MQTT Server URI")
	private String serverURI = DEFAULT_MQTT_SERVER_URI;

	@Option(name="--mqtopic", usage="IoTF MQTT topic")
	private String mqttTopic = DEFAULT_MQTT_TOPICS;
	
	@Option(name="--predictive-service-url", usage="Predictiv Analytics Service URL")
	private String pURL = null;
	
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
