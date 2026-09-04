package com.example.hello_api.service;

import com.example.hello_api.model.RecommendationSummary;
import com.example.hello_api.model.TrafficSummary;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class RecommendationService {

        // Requests-per-window threshold above which a Scale-to-Zero
        // service is considered to have "sustained" traffic worth
        // switching to HPA for. Tune this based on real usage patterns.
        private static final double HIGH_TRAFFIC_THRESHOLD = 50.0;

        private final TrafficService trafficService;
        private final DashboardService dashboardService;

        public RecommendationService(
                        TrafficService trafficService,
                        DashboardService dashboardService) {

                this.trafficService = trafficService;
                this.dashboardService = dashboardService;
        }

        public RecommendationSummary getRecommendation(
                        String serviceName) throws Exception {

                // ----------------------------------
                // 1. Find the service
                // ----------------------------------

                List<Map<String, Object>> services = dashboardService.getAllServices();

                Map<String, Object> currentService = services.stream()
                                .filter(service -> Objects.equals(
                                                service.get("name"),
                                                serviceName))
                                .findFirst()
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "Service not found: "
                                                                                + serviceName));

                String currentType = currentService
                                .get("type")
                                .toString();

                // ----------------------------------
                // 2. Get traffic information
                // ----------------------------------

                TrafficSummary traffic = trafficService.getTraffic(serviceName);

                String trafficStatus = traffic.getTrafficStatus();

                String recommendedType = currentType;

                boolean actionRequired = false;

                String reason;

                // ----------------------------------
                // 3. No Prometheus metric
                // ----------------------------------

                if ("NO_DATA".equals(trafficStatus)) {

                        reason = "Traffic metrics are not available for this service yet. "
                                        + "No scaling recommendation can be made safely.";

                }

                // ----------------------------------
                // 4. HPA + IDLE
                // ----------------------------------

                else if ("HPA".equals(currentType)
                                && "IDLE".equals(trafficStatus)) {

                        recommendedType = "SCALE_TO_ZERO";

                        actionRequired = true;

                        reason = "No recent requests were detected. "
                                        + "The service is using HPA, which keeps at least "
                                        + "the minimum number of replicas running. "
                                        + "Scale-to-Zero can reduce idle resource usage.";
                }

                // ----------------------------------
                // 5. HPA + ACTIVE
                // ----------------------------------

                else if ("HPA".equals(currentType)
                                && "ACTIVE".equals(trafficStatus)) {

                        recommendedType = "HPA";

                        reason = "Recent traffic was detected. "
                                        + "The current HPA configuration is suitable "
                                        + "for an actively used service.";
                }

                // ----------------------------------
                // 6. Knative + IDLE
                // ----------------------------------

                else if ("SCALE_TO_ZERO".equals(currentType)
                                && "IDLE".equals(trafficStatus)) {

                        recommendedType = "SCALE_TO_ZERO";

                        reason = "No recent traffic was detected and the service "
                                        + "already supports Scale-to-Zero. "
                                        + "Keep the current scaling configuration.";
                }

                // ----------------------------------
                // 7. Knative + ACTIVE
                // ----------------------------------

                else if ("SCALE_TO_ZERO".equals(currentType)
                                && "ACTIVE".equals(trafficStatus)) {

                        if (traffic.getRequestsInWindow() >= HIGH_TRAFFIC_THRESHOLD) {

                                // Sustained, heavy traffic -> HPA avoids repeated
                                // cold starts and keeps warm replicas ready.

                                recommendedType = "HPA";

                                actionRequired = true;

                                reason = "Sustained traffic was detected ("
                                                + traffic.getRequestsInWindow()
                                                + " requests in " + traffic.getWindow() + "). "
                                                + "Switching to HPA avoids repeated cold starts "
                                                + "and keeps replicas warm for this level of demand.";

                        } else {

                                // Traffic exists but is still light enough for
                                // Knative's autoscaler to absorb comfortably.

                                recommendedType = "SCALE_TO_ZERO";

                                reason = "Recent traffic was detected ("
                                                + traffic.getRequestsInWindow()
                                                + " requests in " + traffic.getWindow() + "), "
                                                + "but the volume is still low. Knative can "
                                                + "handle this level of traffic with autoscaling. "
                                                + "More sustained traffic is needed before "
                                                + "recommending a switch to HPA.";
                        }
                }

                // ----------------------------------
                // Unexpected situation
                // ----------------------------------

                else {

                        reason = "There is not enough information to make "
                                        + "a scaling recommendation.";
                }

                return new RecommendationSummary(
                                serviceName,
                                currentType,
                                trafficStatus,
                                traffic.getRequestsInWindow(),
                                traffic.getWindow(),
                                recommendedType,
                                actionRequired,
                                reason);
        }
}