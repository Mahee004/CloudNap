package com.example.hello_api.service;

import com.example.hello_api.model.NodeSummary;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeCondition;
import io.kubernetes.client.openapi.models.V1NodeList;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClusterService {

    private final ApiClient apiClient;

    public ClusterService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<NodeSummary> getNodes() throws Exception {

        // Use the Kubernetes ApiClient created in KubernetesConfig
        CoreV1Api api = new CoreV1Api(apiClient);

        // Get all nodes in the cluster
        V1NodeList nodeList = api
                .listNode()
                .execute();

        List<NodeSummary> result = new ArrayList<>();

        for (V1Node node : nodeList.getItems()) {

            String name = "Unknown";

            if (node.getMetadata() != null
                    && node.getMetadata().getName() != null) {

                name = node.getMetadata().getName();
            }

            String status = getNodeStatus(node);

            int cpu = 0;
            double memoryGiB = 0.0;

            if (node.getStatus() != null
                    && node.getStatus().getCapacity() != null) {

                // -------------------------
                // CPU capacity
                // -------------------------

                Quantity cpuQuantity = node.getStatus()
                        .getCapacity()
                        .get("cpu");

                if (cpuQuantity != null) {

                    cpu = cpuQuantity
                            .getNumber()
                            .intValue();
                }

                // -------------------------
                // Memory capacity
                // -------------------------

                Quantity memoryQuantity = node.getStatus()
                        .getCapacity()
                        .get("memory");

                if (memoryQuantity != null) {

                    double memoryBytes = memoryQuantity
                            .getNumber()
                            .doubleValue();

                    memoryGiB = memoryBytes /
                            (1024.0 * 1024.0 * 1024.0);

                    // Round to 2 decimal places
                    memoryGiB = Math.round(memoryGiB * 100.0) / 100.0;
                }
            }

            result.add(
                    new NodeSummary(
                            name,
                            status,
                            cpu,
                            memoryGiB));
        }

        return result;
    }

    private String getNodeStatus(V1Node node) {

        if (node.getStatus() == null
                || node.getStatus().getConditions() == null) {

            return "Unknown";
        }

        for (V1NodeCondition condition : node.getStatus().getConditions()) {

            if ("Ready".equals(condition.getType())) {

                if ("True".equals(condition.getStatus())) {
                    return "Ready";
                }

                return "NotReady";
            }
        }

        return "Unknown";
    }
}