package com.example.hello_api;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Config;

import java.util.HashMap;
import java.util.Map;

public class KnativeDeployer {

    public static void main(String[] args) throws Exception {
        // 1. Connect using the same kubeconfig kubectl uses
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CustomObjectsApi api = new CustomObjectsApi();

        // 2. Build the Knative Service object (same structure as your YAML)
        Map<String, Object> knativeService = buildKnativeServiceSpec(
                "hello-api-from-code", // metadata.name
                "mahee004/hello-api:v1", // image
                8080, // containerPort
                "0", "5", "1" // minScale, maxScale, target
        );

        // 3. Send it to the Kubernetes API
        Object result = api.createNamespacedCustomObject(
                "serving.knative.dev", // group
                "v1", // version
                "default", // namespace
                "services", // plural (CRD name)
                knativeService).execute();
        System.out.println("Created Knative Service: " + result);
    }

    private static Map<String, Object> buildKnativeServiceSpec(
            String name, String image, int port,
            String minScale, String maxScale, String target) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", name);
        metadata.put("namespace", "default");

        Map<String, Object> annotations = new HashMap<>();
        annotations.put("autoscaling.knative.dev/minScale", minScale);
        annotations.put("autoscaling.knative.dev/maxScale", maxScale);
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
}