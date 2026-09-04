package com.example.hello_api.controller;

import io.kubernetes.client.openapi.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.hello_api.service.ConversionService;

import java.util.Map;

@RestController
@RequestMapping("/api/convert")
public class ConversionController {

        private final ConversionService conversionService;

        public ConversionController(
                        ConversionService conversionService) {
                this.conversionService = conversionService;
        }

        @PostMapping("/{name}/to-knative")
        public ResponseEntity<?> convertToKnative(
                        @PathVariable String name) {

                try {

                        conversionService.convertHpaToKnative(name);

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "message",
                                                        "Service converted from HPA to Scale-to-Zero successfully",
                                                        "name",
                                                        name,
                                                        "type",
                                                        "SCALE_TO_ZERO"));

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
                                                                        "Conversion failed",
                                                                        "details",
                                                                        e.getMessage() != null
                                                                                        ? e.getMessage()
                                                                                        : "Unknown error"));
                }
        }

        @PostMapping("/{name}/to-hpa")
        public ResponseEntity<?> convertToHpa(
                        @PathVariable String name) {

                try {

                        conversionService.convertKnativeToHpa(name);

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "message",
                                                        "Service converted from Scale-to-Zero to HPA successfully",
                                                        "name",
                                                        name,
                                                        "type",
                                                        "HPA"));

                } catch (ApiException e) {

                        if (e.getCode() == 404) {
                                return ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body(
                                                                Map.of(
                                                                                "message",
                                                                                "Knative Service not found",
                                                                                "name",
                                                                                name));
                        }

                        if (e.getCode() == 409) {
                                return ResponseEntity
                                                .status(HttpStatus.CONFLICT)
                                                .body(
                                                                Map.of(
                                                                                "message",
                                                                                "HPA resources already exist for this service",
                                                                                "name",
                                                                                name));
                        }

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
                                                                        "Conversion failed",
                                                                        "details",
                                                                        e.getMessage() != null
                                                                                        ? e.getMessage()
                                                                                        : "Unknown error"));
                }
        }
}