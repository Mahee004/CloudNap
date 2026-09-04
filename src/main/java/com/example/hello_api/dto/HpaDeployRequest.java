package com.example.hello_api.dto;

public class HpaDeployRequest {

    private String name;
    private String image;
    private int port = 8080;
    private int minReplicas = 1;
    private int maxReplicas = 5;
    private int targetCpu = 50;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMinReplicas() {
        return minReplicas;
    }

    public void setMinReplicas(int minReplicas) {
        this.minReplicas = minReplicas;
    }

    public int getMaxReplicas() {
        return maxReplicas;
    }

    public void setMaxReplicas(int maxReplicas) {
        this.maxReplicas = maxReplicas;
    }

    public int getTargetCpu() {
        return targetCpu;
    }

    public void setTargetCpu(int targetCpu) {
        this.targetCpu = targetCpu;
    }

}