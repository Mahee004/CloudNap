package com.example.hello_api.controller;

import com.example.hello_api.service.TrafficService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/traffic")
public class TrafficController {

    private final TrafficService trafficService;

    public TrafficController(
            TrafficService trafficService) {

        this.trafficService = trafficService;
    }

    @GetMapping("/{serviceName}")
    public ResponseEntity<?> getTraffic(
            @PathVariable String serviceName) {

        try {

            return ResponseEntity.ok(
                    trafficService.getTraffic(
                            serviceName));

        } catch (Exception e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "message",
                                    "Failed to retrieve traffic information",

                                    "details",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Unknown error"));
        }
    }
}