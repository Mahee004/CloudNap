package com.example.hello_api.model;

public class RecommendationSummary {

    private String serviceName;
    private String currentType;

    private String trafficStatus;
    private double requestsInWindow;
    private String window;

    private String recommendedType;
    private boolean actionRequired;

    private String reason;

    public RecommendationSummary(
            String serviceName,
            String currentType,
            String trafficStatus,
            double requestsInWindow,
            String window,
            String recommendedType,
            boolean actionRequired,
            String reason) {

        this.serviceName = serviceName;
        this.currentType = currentType;
        this.trafficStatus = trafficStatus;
        this.requestsInWindow = requestsInWindow;
        this.window = window;
        this.recommendedType = recommendedType;
        this.actionRequired = actionRequired;
        this.reason = reason;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getCurrentType() {
        return currentType;
    }

    public String getTrafficStatus() {
        return trafficStatus;
    }

    public double getRequestsInWindow() {
        return requestsInWindow;
    }

    public String getWindow() {
        return window;
    }

    public String getRecommendedType() {
        return recommendedType;
    }

    public boolean isActionRequired() {
        return actionRequired;
    }

    public String getReason() {
        return reason;
    }
}