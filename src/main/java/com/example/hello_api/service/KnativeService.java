package com.example.hello_api.service;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Config;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class KnativeService {

        private final ApiClient apiClient;

        public KnativeService(ApiClient apiClient) {
                this.apiClient = apiClient;
        }

        public Object deploy(String name, String image, int port,
                        String minScale, String maxScale, String target) throws Exception {

                CustomObjectsApi api = new CustomObjectsApi(apiClient);

                Map<String, Object> knativeService = buildKnativeServiceSpec(
                                name, image, port, minScale, maxScale, target);

                return api.createNamespacedCustomObject(
                                "serving.knative.dev",
                                "v1",
                                "default",
                                "services",
                                knativeService).execute();
        }

        @SuppressWarnings("unchecked")
        public List<Map<String, Object>> listServices() throws Exception {
                ApiClient client = Config.defaultClient();
                Configuration.setDefaultApiClient(client);

                CustomObjectsApi api = new CustomObjectsApi();

                Object result = api.listNamespacedCustomObject(
                                "serving.knative.dev",
                                "v1",
                                "default",
                                "services").execute();

                Map<String, Object> resultMap = (Map<String, Object>) result;
                List<Map<String, Object>> items = (List<Map<String, Object>>) resultMap.get("items");

                List<Map<String, Object>> simplified = new ArrayList<>();

                for (Map<String, Object> item : items) {
                        Map<String, Object> metadata = (Map<String, Object>) item.get("metadata");
                        Map<String, Object> status = (Map<String, Object>) item.get("status");

                        Map<String, Object> summary = new HashMap<>();
                        summary.put("name", metadata.get("name"));
                        summary.put("url", status != null ? status.get("url") : null);

                        boolean ready = false;
                        if (status != null && status.get("conditions") != null) {
                                List<Map<String, Object>> conditions = (List<Map<String, Object>>) status
                                                .get("conditions");
                                for (Map<String, Object> condition : conditions) {
                                        if ("Ready".equals(condition.get("type"))) {
                                                ready = "True".equals(condition.get("status"));
                                        }
                                }
                        }
                        summary.put("ready", ready);

                        simplified.add(summary);
                }

                return simplified;
        }

        public Object getServiceByName(String name) throws Exception {

                // Connect to the Kubernetes cluster used by kubectl
                ApiClient client = Config.defaultClient();
                Configuration.setDefaultApiClient(client);

                // Create the API object used for Custom Resources
                CustomObjectsApi api = new CustomObjectsApi();

                // Get one Knative Service from the default namespace
                return api.getNamespacedCustomObject(
                                "serving.knative.dev", // Knative API group
                                "v1", // API version
                                "default", // Kubernetes namespace
                                "services", // Resource type
                                name // Service name from the URL
                ).execute();
        }

        public Object deleteService(String name) throws Exception {

                // Connect to the Kubernetes cluster
                ApiClient client = Config.defaultClient();
                Configuration.setDefaultApiClient(client);

                // API used for Knative custom resources
                CustomObjectsApi api = new CustomObjectsApi();

                // Delete the Knative Service from the default namespace
                return api.deleteNamespacedCustomObject(
                                "serving.knative.dev",
                                "v1",
                                "default",
                                "services",
                                name).execute();
        }

        private Map<String, Object> buildKnativeServiceSpec(
                        String name, String image, int port,
                        String minScale, String maxScale, String target) {

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("name", name);
                metadata.put("namespace", "default");

                Map<String, Object> annotations = new HashMap<>();
                annotations.put("autoscaling.knative.dev/min-scale", minScale);
                annotations.put("autoscaling.knative.dev/max-scale", maxScale);
                annotations.put("autoscaling.knative.dev/target", target);

                Map<String, Object> templateMetadata = new HashMap<>();
                templateMetadata.put("annotations", annotations);

                Map<String, Object> containerPort = new HashMap<>();
                containerPort.put("containerPort", port);

                Map<String, Object> container = new HashMap<>();
                container.put("image", image);
                container.put("ports", new Object[] { containerPort });

                Map<String, Object> podSpec = new HashMap<>();
                podSpec.put("containers", new Object[] { container });

                Map<String, Object> template = new HashMap<>();
                template.put("metadata", templateMetadata);
                template.put("spec", podSpec);

                Map<String, Object> serviceSpec = new HashMap<>();
                serviceSpec.put("template", template);

                Map<String, Object> knativeService = new HashMap<>();
                knativeService.put("apiVersion", "serving.knative.dev/v1");
                knativeService.put("kind", "Service");
                knativeService.put("metadata", metadata);
                knativeService.put("spec", serviceSpec);

                return knativeService;
        }

        @SuppressWarnings("unchecked")
        public Object updateMinScale(
                        String name,
                        String minScale) throws Exception {

                // For this project, only allow 0 or 1
                if (!"0".equals(minScale) && !"1".equals(minScale)) {
                        throw new IllegalArgumentException(
                                        "minScale must be 0 or 1");
                }

                // Connect to Kubernetes
                ApiClient client = Config.defaultClient();
                Configuration.setDefaultApiClient(client);

                CustomObjectsApi api = new CustomObjectsApi();

                // Get the existing Knative Service
                Object result = api.getNamespacedCustomObject(
                                "serving.knative.dev",
                                "v1",
                                "default",
                                "services",
                                name).execute();

                Map<String, Object> service = (Map<String, Object>) result;

                // Access spec
                Map<String, Object> spec = (Map<String, Object>) service.get("spec");

                // Access spec.template
                Map<String, Object> template = (Map<String, Object>) spec.computeIfAbsent(
                                "template",
                                key -> new HashMap<String, Object>());

                // Access spec.template.metadata
                Map<String, Object> templateMetadata = (Map<String, Object>) template.computeIfAbsent(
                                "metadata",
                                key -> new HashMap<String, Object>());

                // Access spec.template.metadata.annotations
                Map<String, Object> annotations = (Map<String, Object>) templateMetadata.computeIfAbsent(
                                "annotations",
                                key -> new HashMap<String, Object>());

                // Change the Knative minimum scale
                annotations.put(
                                "autoscaling.knative.dev/min-scale",
                                minScale);

                // Status is managed by Knative, so do not send it during replacement
                service.remove("status");

                // Replace the service with the updated configuration
                return api.replaceNamespacedCustomObject(
                                "serving.knative.dev",
                                "v1",
                                "default",
                                "services",
                                name,
                                service).execute();
        }

}