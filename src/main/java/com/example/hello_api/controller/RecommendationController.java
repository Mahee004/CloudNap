package com.example.hello_api.controller;

import com.example.hello_api.service.RecommendationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(
            RecommendationService recommendationService) {

        this.recommendationService = recommendationService;
    }

    @GetMapping("/{serviceName}")
    public ResponseEntity<?> getRecommendation(
            @PathVariable String serviceName) {

        try {

            return ResponseEntity.ok(
                    recommendationService
                            .getRecommendation(serviceName));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()));

        } catch (Exception e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "message",
                                    "Failed to generate recommendation",

                                    "details",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Unknown error"));
        }
    }
}