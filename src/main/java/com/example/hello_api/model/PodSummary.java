package com.example.hello_api.model;

public class PodSummary {

    private String service;
    private int podCount;
    private int runningPods;

    public PodSummary(
            String service,
            int podCount,
            int runningPods) {
        this.service = service;
        this.podCount = podCount;
        this.runningPods = runningPods;
    }

    public String getService() {
        return service;
    }

    public int getPodCount() {
        return podCount;
    }

    public int getRunningPods() {
        return runningPods;
    }
}