package com.example.hello_api.model;

public class NodeSummary {

    private String name;
    private String status;
    private int cpu;
    private double memoryGiB;

    public NodeSummary(
            String name,
            String status,
            int cpu,
            double memoryGiB) {
        this.name = name;
        this.status = status;
        this.cpu = cpu;
        this.memoryGiB = memoryGiB;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getCpu() {
        return cpu;
    }

    public double getMemoryGiB() {
        return memoryGiB;
    }
}