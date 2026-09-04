package com.example.hello_api.model;

public class TrafficSummary {

    private String serviceName;
    private double requestsInWindow;
    private String trafficStatus;
    private String window;

    public TrafficSummary(
            String serviceName,
            double requestsInWindow,
            String trafficStatus,
            String window) {

        this.serviceName = serviceName;
        this.requestsInWindow = requestsInWindow;
        this.trafficStatus = trafficStatus;
        this.window = window;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getRequestsInWindow() {
        return requestsInWindow;
    }

    public String getTrafficStatus() {
        return trafficStatus;
    }

    public String getWindow() {
        return window;
    }
}