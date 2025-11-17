package com.system.service;

public class Config {
    // MQTT Configuration
    public static final String MQTT_BROKER_URL = "tcp://server.iotdataserver.com:1883";
    public static final String MQTT_USERNAME = "emqx";
    public static final String MQTT_PASSWORD = "Public!@#";
    public static final String MQTT_TOPIC_BASE = "/iotds/leonnel/"; // Topic will be: /iotds/leonnel/{macaddress}/gpsdata

    // Location update intervals in milliseconds
    // When device is moving (speed > 1 m/s or ~3.6 km/h)
    public static final long LOCATION_UPDATE_INTERVAL_MOVING = 2 * 60 * 1000; // 2 minutes

    // When device is stationary (speed < 1 m/s)
    public static final long LOCATION_UPDATE_INTERVAL_STATIONARY = 10 * 60 * 1000; // 10 minutes

    // Fastest location update interval (limit)
    public static final long LOCATION_FASTEST_INTERVAL = 1 * 60 * 1000; // 1 minute

    // Speed threshold to determine if device is moving (m/s)
    public static final float SPEED_THRESHOLD_MOVING = 1.0f; // ~3.6 km/h
}
