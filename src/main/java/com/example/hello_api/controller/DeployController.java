package com.example.hello_api.controller;

import org.springframework.web.bind.annotation.*;

import com.example.hello_api.dto.DeployRequest;
import com.example.hello_api.dto.ScaleUpdateRequest;
import com.example.hello_api.service.KnativeService;

import io.kubernetes.client.openapi.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DeployController {

    private final KnativeService knativeService;

    public DeployController(KnativeService knativeService) {
        this.knativeService = knativeService;
    }

    @PostMapping("/deploy")
    public String deploy(@RequestBody DeployRequest request) {
        try {
            knativeService.deploy(
                    request.getName(),
                    request.getImage(),
                    request.getPort(),
                    request.getMinScale(),
                    request.getMaxScale(),
                    request.getTarget());
            return "Deployed successfully: " + request.getName();
        } catch (Exception e) {
            return "Deployment failed: " + e.getMessage();
        }
    }

    @GetMapping("/services")
    public Object listServices() {
        try {
            return knativeService.listServices();
        } catch (Exception e) {
            return "Failed to list services: " + e.getMessage();
        }
    }

    @GetMapping("/services/{name}")
    public ResponseEntity<?> getServiceByName(
            @PathVariable String name) {

        try {
            Object service = knativeService.getServiceByName(name);

            return ResponseEntity.ok(service);

        } catch (ApiException e) {

            if (e.getCode() == 404) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "message",
                                "Knative Service not found: " + name));
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Kubernetes API error",
                            "statusCode", e.getCode()));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Failed to read Knative Service"));
        }
    }

    @DeleteMapping("/services/{name}")
    public ResponseEntity<?> deleteService(
            @PathVariable String name) {

        try {
            knativeService.deleteService(name);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Knative Service deleted successfully",
                            "name", name));

        } catch (ApiException e) {

            if (e.getCode() == 404) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "message",
                                "Knative Service not found: " + name));
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Kubernetes API error",
                            "statusCode", e.getCode()));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Failed to delete Knative Service"));
        }
    }

    @PutMapping("/services/{name}/scale")
    public ResponseEntity<?> updateScale(
            @PathVariable String name,
            @RequestBody ScaleUpdateRequest request) {

        if (request.getMinScale() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "message",
                            "minScale is required"));
        }

        try {
            knativeService.updateMinScale(
                    name,
                    request.getMinScale());

            String mode = "0".equals(request.getMinScale())
                    ? "Scale-to-zero enabled"
                    : "Scale-to-zero disabled";

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Scale configuration updated successfully",
                            "name",
                            name,
                            "minScale",
                            request.getMinScale(),
                            "mode",
                            mode));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "message",
                            e.getMessage()));

        } catch (ApiException e) {

            if (e.getCode() == 404) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "message",
                                "Knative Service not found: " + name));
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Kubernetes API error",
                            "statusCode",
                            e.getCode(),
                            "details",
                            e.getResponseBody() != null
                                    ? e.getResponseBody()
                                    : "No details available"));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Failed to update scale configuration",
                            "details",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unknown error"));
        }
    }
}