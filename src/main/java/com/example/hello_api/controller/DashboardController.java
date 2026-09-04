package com.example.hello_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.hello_api.service.DashboardService;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/services")
    public ResponseEntity<?> getServices() {

        try {

            return ResponseEntity.ok(
                    dashboardService.getAllServices());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "message",
                                    "Failed to retrieve dashboard services",

                                    "details",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Unknown error"));
        }
    }
}