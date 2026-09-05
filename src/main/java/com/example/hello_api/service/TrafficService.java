package com.example.hello_api.service;

import com.example.hello_api.model.TrafficSummary;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class TrafficService {

        private final RestClient restClient;
        private final String window;

        public TrafficService(
                        @Value("${prometheus.base-url}") String prometheusBaseUrl,
                        @Value("${traffic.window:1m}") String window) {

                this.restClient = RestClient.builder()
                                .baseUrl(prometheusBaseUrl)
                                .build();

                this.window = window;
        }

        @SuppressWarnings("unchecked")
        public TrafficSummary getTraffic(String serviceName) {

                // Escape the service name before putting it into PromQL.
                String safeServiceName = serviceName
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"");

                // Ask Prometheus:
                // "How many /hello requests happened during the window?"
                String query = "sum(" +
                                "increase(" +
                                "http_server_requests_seconds_count{" +
                                "service_name=\"" + safeServiceName + "\"" +
                                "}[" + window + "]" +
                                ")" +
                                ")";

                Map<String, Object> response = restClient
                                .get()
                                .uri(
                                                "/api/v1/query?query={query}",
                                                query)
                                .retrieve()
                                .body(Map.class);

                if (response == null
                                || !"success".equals(response.get("status"))) {

                        throw new IllegalStateException(
                                        "Prometheus query failed");
                }

                Map<String, Object> data = (Map<String, Object>) response.get("data");

                List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("result");

                double requests = 0.0;
                String trafficStatus;

                if (results == null || results.isEmpty()) {

                        // Prometheus has no matching metric.
                        trafficStatus = "NO_DATA";

                } else {

                        List<Object> value = (List<Object>) results
                                        .get(0)
                                        .get("value");

                        if (value != null && value.size() >= 2) {

                                requests = Double.parseDouble(
                                                value.get(1).toString());
                        }

                        requests = Math.round(requests * 100.0) / 100.0;

                        trafficStatus = requests > 0
                                        ? "ACTIVE"
                                        : "IDLE";
                }

                return new TrafficSummary(
                                serviceName,
                                requests,
                                trafficStatus,
                                window);
        }
}