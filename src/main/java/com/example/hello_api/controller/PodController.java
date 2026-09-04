package com.example.hello_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.hello_api.model.PodSummary;
import com.example.hello_api.service.PodService;

import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class PodController {

    private final PodService podService;

    public PodController(PodService podService) {
        this.podService = podService;
    }

    @GetMapping("/{name}/pods")
    public ResponseEntity<?> getPods(
            @PathVariable String name) {

        try {

            PodSummary result = podService.getPodsForService(name);

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Failed to retrieve pod information",
                            "details",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unknown error"));
        }
    }
}