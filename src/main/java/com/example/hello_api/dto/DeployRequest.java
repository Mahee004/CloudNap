package com.example.hello_api.dto;

public class DeployRequest {
    private String name;
    private String image;
    private int port = 8080;
    private String minScale = "0";
    private String maxScale = "5";
    private String target = "1";

    // Getters and setters (Spring needs these to convert JSON <-> Java object)
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

    public String getMinScale() {
        return minScale;
    }

    public void setMinScale(String minScale) {
        this.minScale = minScale;
    }

    public String getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(String maxScale) {
        this.maxScale = maxScale;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}