package com.example.hello_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.hello_api.service.ClusterService;

import java.util.Map;

@RestController
@RequestMapping("/api/cluster")
public class ClusterController {

    private final ClusterService clusterService;

    public ClusterController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @GetMapping("/nodes")
    public ResponseEntity<?> getNodes() {

        try {

            return ResponseEntity.ok(
                    clusterService.getNodes());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Failed to retrieve Kubernetes nodes",
                            "details",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unknown error"));
        }
    }
}