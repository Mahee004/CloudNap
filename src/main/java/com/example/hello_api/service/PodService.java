package com.example.hello_api.service;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;

import org.springframework.stereotype.Service;

import com.example.hello_api.model.PodSummary;

@Service
public class PodService {

    private final ApiClient apiClient;

    public PodService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private PodSummary getPodsByLabel(
            String serviceName,
            String labelSelector) throws Exception {

        CoreV1Api api = new CoreV1Api(apiClient);

        V1PodList podList = api
                .listNamespacedPod("default")
                .labelSelector(labelSelector)
                .execute();

        int podCount = podList.getItems().size();
        int runningPods = 0;

        for (V1Pod pod : podList.getItems()) {

            if (pod.getStatus() != null
                    && "Running".equals(pod.getStatus().getPhase())) {

                runningPods++;
            }
        }

        return new PodSummary(
                serviceName,
                podCount,
                runningPods);
    }

    // For Knative
    public PodSummary getKnativePods(
            String serviceName) throws Exception {

        return getPodsByLabel(
                serviceName,
                "serving.knative.dev/service=" + serviceName);
    }

    // For normal Kubernetes Deployment + HPA
    public PodSummary getHpaPods(
            String serviceName) throws Exception {

        return getPodsByLabel(
                serviceName,
                "app=" + serviceName);
    }

    public PodSummary getPodsForService(String serviceName) throws Exception {
        return getKnativePods(serviceName);
    }
}