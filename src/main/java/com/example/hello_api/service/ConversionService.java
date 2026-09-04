package com.example.hello_api.service;

import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V2HorizontalPodAutoscaler;

import java.util.Map;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.hello_api.dto.HpaDeployRequest;

@Service
public class ConversionService {

    private final HpaService hpaService;
    private final KnativeService knativeService;

    public ConversionService(
            HpaService hpaService,
            KnativeService knativeService) {
        this.hpaService = hpaService;
        this.knativeService = knativeService;
    }

    public void convertHpaToKnative(String name)
            throws Exception {

        // ------------------------------------
        // 1. Read existing Deployment
        // ------------------------------------

        V1Deployment deployment = hpaService.getDeployment(name);

        V1Container container = deployment
                .getSpec()
                .getTemplate()
                .getSpec()
                .getContainers()
                .get(0);

        String image = container.getImage();

        // ------------------------------------
        // 2. Find container port
        // ------------------------------------

        int port = 8080;

        if (container.getPorts() != null
                && !container.getPorts().isEmpty()
                && container.getPorts().get(0).getContainerPort() != null) {

            port = container
                    .getPorts()
                    .get(0)
                    .getContainerPort();
        }

        // ------------------------------------
        // 3. Read existing HPA
        // ------------------------------------

        V2HorizontalPodAutoscaler hpa = hpaService.getHpa(name);

        int maxScale = hpa
                .getSpec()
                .getMaxReplicas();

        // ------------------------------------
        // 4. Create Knative Service
        // ------------------------------------

        knativeService.deploy(
                name,
                image,
                port,
                "0",
                String.valueOf(maxScale),
                "100");

        // ------------------------------------
        // 5. Remove old HPA application
        // ------------------------------------

        hpaService.deleteHpaApplication(name);
    }

    @SuppressWarnings("unchecked")
    public void convertKnativeToHpa(String name) throws Exception {

        // ------------------------------------
        // 1. Read the existing Knative Service
        // ------------------------------------

        Object result = knativeService.getServiceByName(name);

        Map<String, Object> service = (Map<String, Object>) result;

        // ------------------------------------
        // 2. Go to spec.template
        // ------------------------------------

        Map<String, Object> spec = (Map<String, Object>) service.get("spec");

        Map<String, Object> template = (Map<String, Object>) spec.get("template");

        // ------------------------------------
        // 3. Read container information
        // ------------------------------------

        Map<String, Object> podSpec = (Map<String, Object>) template.get("spec");

        List<Map<String, Object>> containers = (List<Map<String, Object>>) podSpec.get("containers");

        if (containers == null || containers.isEmpty()) {
            throw new IllegalStateException(
                    "Knative Service has no containers");
        }

        Map<String, Object> container = containers.get(0);

        String image = (String) container.get("image");

        // Default Spring Boot port
        int port = 8080;

        List<Map<String, Object>> ports = (List<Map<String, Object>>) container.get("ports");

        if (ports != null && !ports.isEmpty()) {

            Object portValue = ports.get(0).get("containerPort");

            if (portValue instanceof Number number) {
                port = number.intValue();
            }
        }

        // ------------------------------------
        // 4. Read Knative max-scale
        // ------------------------------------

        int maxReplicas = 5;

        Map<String, Object> templateMetadata = (Map<String, Object>) template.get("metadata");

        if (templateMetadata != null) {

            Map<String, Object> annotations = (Map<String, Object>) templateMetadata.get("annotations");

            if (annotations != null) {

                Object maxScaleValue = annotations.get(
                        "autoscaling.knative.dev/max-scale");

                if (maxScaleValue != null) {

                    int value = Integer.parseInt(
                            maxScaleValue.toString());

                    // Knative max-scale 0 means unlimited.
                    // For our HPA demo use maximum 5 instead.
                    if (value > 0) {
                        maxReplicas = value;
                    }
                }
            }
        }

        // ------------------------------------
        // 5. Prepare HPA request
        // ------------------------------------

        HpaDeployRequest request = new HpaDeployRequest();

        request.setName(name);
        request.setImage(image);
        request.setPort(port);

        // HPA version does not scale to zero
        request.setMinReplicas(1);

        request.setMaxReplicas(maxReplicas);

        // CPU target for our HPA configuration
        request.setTargetCpu(50);

        // ------------------------------------
        // 6. Create HPA architecture FIRST
        // ------------------------------------

        hpaService.deploy(request);

        // ------------------------------------
        // 7. Delete Knative only after success
        // ------------------------------------

        knativeService.deleteService(name);
    }
}