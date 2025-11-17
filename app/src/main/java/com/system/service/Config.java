package com.system.service;

public class Config {
    // MQTT Configuration
    public static final String MQTT_BROKER_URL = "tcp://server.iotdataserver.com:1883";
    public static final String MQTT_USERNAME = "emqx";
    public static final String MQTT_PASSWORD = "Public!@#";
    public static final String MQTT_TOPIC_BASE = "/iotds/leonnel/"; // Topic will be: /iotds/leonnel/{macaddress}/gpsdata

    // Location update interval in milliseconds (default: 5 minutes)
    public static final long LOCATION_UPDATE_INTERVAL = 5 * 60 * 1000;

    // Fastest location update interval in milliseconds (default: 2 minutes)
    public static final long LOCATION_FASTEST_INTERVAL = 2 * 60 * 1000;
}
