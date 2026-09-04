package com.example.hello_api.controller;

import io.kubernetes.client.openapi.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.hello_api.dto.HpaDeployRequest;
import com.example.hello_api.service.HpaService;

import java.util.Map;

@RestController
@RequestMapping("/api/hpa")
public class HpaController {

        private final HpaService hpaService;

        public HpaController(HpaService hpaService) {
                this.hpaService = hpaService;
        }

        @PostMapping("/deployment")
        public ResponseEntity<?> createDeployment(
                        @RequestBody HpaDeployRequest request) {

                try {

                        hpaService.createDeployment(
                                        request.getName(),
                                        request.getImage(),
                                        request.getPort(),
                                        request.getMinReplicas());

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "message",
                                                        "Deployment created successfully",
                                                        "name",
                                                        request.getName(),
                                                        "replicas",
                                                        request.getMinReplicas()));

                } catch (ApiException e) {

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(
                                                        Map.of(
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
                                        .body(
                                                        Map.of(
                                                                        "message",
                                                                        "Failed to create Deployment",
                                                                        "details",
                                                                        e.getMessage() != null
                                                                                        ? e.getMessage()
                                                                                        : "Unknown error"));
                }
        }

        @PostMapping("/service")
        public ResponseEntity<?> createService(
                        @RequestBody HpaDeployRequest request) {

                try {

                        hpaService.createService(
                                        request.getName(),
                                        request.getPort());

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "message",
                                                        "Kubernetes Service created successfully",
                                                        "name",
                                                        request.getName() + "-service"));

                } catch (ApiException e) {

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(
                                                        Map.of(
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
                                        .body(
                                                        Map.of(
                                                                        "message",
                                                                        "Failed to create Kubernetes Service",
                                                                        "details",
                                                                        e.getMessage() != null
                                                                                        ? e.getMessage()
                                                                                        : "Unknown error"));
                }
        }

        @PostMapping("/autoscaler")
        public ResponseEntity<?> createHpa(
                        @RequestBody HpaDeployRequest request) {

                try {

                        hpaService.createHpa(
                                        request.getName(),
                                        request.getMinReplicas(),
                                        request.getMaxReplicas(),
                                        request.getTargetCpu());

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "message",
                                                        "HPA created successfully",
                                                        "name",
                                                        request.getName(),
                                                        "minReplicas",
                                                        request.getMinReplicas(),
                                                        "maxReplicas",
                                                        request.getMaxReplicas(),
                                                        "targetCpu",
                                                        request.getTargetCpu()));

                } catch (ApiException e) {

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(
                                                        Map.of(
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
                                        .body(
                                                        Map.of(
                                                                        "message",
                                                                        "Failed to create HPA",
                                                                        "details",
                                                                        e.getMessage() != null
                                                                                        ? e.getMessage()
                                                                                        : "Unknown error"));
                }
        }

        @PostMapping("/deploy")
        public ResponseEntity<?> deploy(
                        @RequestBody HpaDeployRequest request) {

                try {

                        hpaService.deploy(request);

                        return ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(Map.of(
                                                        "message", "HPA application deployed successfully",
                                                        "name", request.getName(),
                                                        "serviceName", request.getName() + "-service",
                                                        "minReplicas", request.getMinReplicas(),
                                                        "maxReplicas", request.getMaxReplicas(),
                                                        "targetCpu", request.getTargetCpu()));

                } catch (ApiException e) {

                        if (e.getCode() == 409) {
                                return ResponseEntity
                                                .status(HttpStatus.CONFLICT)
                                                .body(Map.of(
                                                                "message",
                                                                "A Kubernetes resource with this name already exists",
                                                                "name",
                                                                request.getName()));
                        }

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of(
                                                        "message", "Kubernetes API error",
                                                        "statusCode", e.getCode(),
                                                        "details",
                                                        e.getResponseBody() != null
                                                                        ? e.getResponseBody()
                                                                        : "No details available"));

                } catch (Exception e) {

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of(
                                                        "message",
                                                        "Failed to deploy HPA application",
                                                        "details",
                                                        e.getMessage() != null
                                                                        ? e.getMessage()
                                                                        : "Unknown error"));
                }
        }

        @GetMapping("/services")
        public ResponseEntity<?> getHpaServices() {

                try {

                        return ResponseEntity.ok(
                                        hpaService.listHpaServices());

                } catch (ApiException e) {

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of(
                                                        "message", "Kubernetes API error",
                                                        "statusCode", e.getCode(),
                                                        "details",
                                                        e.getResponseBody() != null
                                                                        ? e.getResponseBody()
                                                                        : "No details available"));

                } catch (Exception e) {

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of(
                                                        "message", "Failed to retrieve HPA services",
                                                        "details",
                                                        e.getMessage() != null
                                                                        ? e.getMessage()
                                                                        : "Unknown error"));
                }
        }
}