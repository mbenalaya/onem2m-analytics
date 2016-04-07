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
 *****************************************************************************
 *
 */
package com.ibm.iot.iotdatagenerator;

import java.io.OutputStream;
import java.io.PrintStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class CommandLineParser {
	/* The MQTT Client for IOTSpark will publish to a defined IoTF device.
     * The fake device in Jenny's chat
     * In this demo, it will publish to:
     * Server: o672lp.messaging.internetofthings.ibmcloud.com:1883
     * Client ID: d:o672lp:IoTAnalytic:Client67890
     * Topic: iot-2/evt/temperature/fmt/json
     */
	private static final String DEFAULT_MQTT_TOPICS = "iot-2/evt/temperature/fmt/json";
	private static final String DEFAULT_MQTT_SERVER_URI = "tcp://o672lp.messaging.internetofthings.ibmcloud.com:1883";
	private static final String DEFAULT_CLIENT_ID = "d:o672lp:IoTAnalytic:Client67890";
	private static final String DEFAULT_USER_NAME = "use-token-auth";
	private static final String DEFAULT_PASSWORD = "Client67890";
	private static final String DEFAULT_DATASET_PATH = "./testDataSet";

	@Option(name="--id", usage="IOTF Client ID")
	private String clientId = DEFAULT_CLIENT_ID;

	@Option(name="--uri", usage="IOTF Server URI")
	private String serverURI = DEFAULT_MQTT_SERVER_URI;
	
	@Option(name="--mqtopic", usage="IOTF MQTT topic")
	private String mqttTopic = DEFAULT_MQTT_TOPICS;
	
	@Option(name="--user", usage="IOTF UserName")
	private String user = DEFAULT_USER_NAME;
	
	@Option(name="--pwd", usage="IOTF Password")
	private String password = DEFAULT_PASSWORD;	

	@Option(name="--datapath", usage="test dataset file path")
	private String datapath = DEFAULT_DATASET_PATH;	
	
	@Option(name="--help", aliases="-h", usage="Show help")
	private boolean showHelp = false;
	
	private CmdLineParser parser = new CmdLineParser(this);
	
	public String getClientId() {
		return clientId;
	}

	public String getServerURI() {
		return serverURI;
	}

	public String getMqttTopic() {
		return mqttTopic;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getDataPath() {
		return datapath;
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
		stream.println("java " + IoTDataGenerator.class.getName() + " [options...]");
		parser.printUsage(out);
	}
}
