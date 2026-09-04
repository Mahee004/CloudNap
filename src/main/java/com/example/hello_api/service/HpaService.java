package com.example.hello_api.service;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import io.kubernetes.client.openapi.apis.AutoscalingV2Api;

import io.kubernetes.client.openapi.models.V2CrossVersionObjectReference;
import io.kubernetes.client.openapi.models.V2HorizontalPodAutoscaler;
import io.kubernetes.client.openapi.models.V2HorizontalPodAutoscalerSpec;
import io.kubernetes.client.openapi.models.V2MetricSpec;
import io.kubernetes.client.openapi.models.V2MetricTarget;
import io.kubernetes.client.openapi.models.V2ResourceMetricSource;
//import io.kubernetes.client.openapi.models.V2HorizontalPodAutoscaler;
import io.kubernetes.client.openapi.models.V2HorizontalPodAutoscalerList;

import org.springframework.stereotype.Service;

import com.example.hello_api.dto.HpaDeployRequest;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Service
public class HpaService {

        private final ApiClient apiClient;

        public HpaService(ApiClient apiClient) {
                this.apiClient = apiClient;
        }

        public V1Deployment createDeployment(
                        String name,
                        String image,
                        int port,
                        int replicas) throws Exception {

                AppsV1Api appsApi = new AppsV1Api(apiClient);

                // Labels identify the Pods belonging to this Deployment
                V1ObjectMeta podMetadata = new V1ObjectMeta()
                                .putLabelsItem("app", name);

                // CPU/memory settings.
                // CPU request is important later when HPA calculates utilization.
                V1ResourceRequirements resources = new V1ResourceRequirements()
                                .putRequestsItem(
                                                "cpu",
                                                Quantity.fromString("100m"))
                                .putRequestsItem(
                                                "memory",
                                                Quantity.fromString("128Mi"))
                                .putLimitsItem(
                                                "cpu",
                                                Quantity.fromString("500m"))
                                .putLimitsItem(
                                                "memory",
                                                Quantity.fromString("512Mi"));

                // The container that will run inside the Pod
                V1Container container = new V1Container()
                                .name("hello-api")
                                .image(image)
                                .ports(
                                                List.of(
                                                                new V1ContainerPort()
                                                                                .containerPort(port)))
                                .resources(resources);

                // Pod configuration
                V1PodSpec podSpec = new V1PodSpec()
                                .containers(List.of(container));

                // Pod template used by the Deployment
                V1PodTemplateSpec podTemplate = new V1PodTemplateSpec()
                                .metadata(podMetadata)
                                .spec(podSpec);

                // Deployment configuration
                V1DeploymentSpec deploymentSpec = new V1DeploymentSpec()
                                .replicas(replicas)
                                .selector(
                                                new V1LabelSelector()
                                                                .putMatchLabelsItem(
                                                                                "app",
                                                                                name))
                                .template(podTemplate);

                // Complete Deployment object
                V1Deployment deployment = new V1Deployment()
                                .apiVersion("apps/v1")
                                .kind("Deployment")
                                .metadata(
                                                new V1ObjectMeta()
                                                                .name(name)
                                                                .namespace("default"))
                                .spec(deploymentSpec);

                // Send the Deployment to Kubernetes
                return appsApi
                                .createNamespacedDeployment(
                                                "default",
                                                deployment)
                                .execute();
        }

        public V1Service createService(
                        String name,
                        int port) throws Exception {

                CoreV1Api coreApi = new CoreV1Api(apiClient);

                // Port configuration
                V1ServicePort servicePort = new V1ServicePort()
                                .port(80)
                                .targetPort(new IntOrString(port))
                                .protocol("TCP");

                // Service configuration
                V1ServiceSpec serviceSpec = new V1ServiceSpec()
                                .selector(Map.of("app", name))
                                .ports(List.of(servicePort))
                                .type("NodePort");

                // Complete Kubernetes Service object
                V1Service service = new V1Service()
                                .apiVersion("v1")
                                .kind("Service")
                                .metadata(
                                                new V1ObjectMeta()
                                                                .name(name + "-service")
                                                                .namespace("default"))
                                .spec(serviceSpec);

                // Send it to Kubernetes
                return coreApi
                                .createNamespacedService(
                                                "default",
                                                service)
                                .execute();
        }

        public V2HorizontalPodAutoscaler createHpa(
                        String name,
                        int minReplicas,
                        int maxReplicas,
                        int targetCpu) throws Exception {

                AutoscalingV2Api autoscalingApi = new AutoscalingV2Api(apiClient);

                // Which resource should HPA scale?
                V2CrossVersionObjectReference target = new V2CrossVersionObjectReference()
                                .apiVersion("apps/v1")
                                .kind("Deployment")
                                .name(name);

                // CPU target, for example 50%
                V2MetricTarget metricTarget = new V2MetricTarget()
                                .type("Utilization")
                                .averageUtilization(targetCpu);

                // CPU metric
                V2ResourceMetricSource cpuMetric = new V2ResourceMetricSource()
                                .name("cpu")
                                .target(metricTarget);

                V2MetricSpec metricSpec = new V2MetricSpec()
                                .type("Resource")
                                .resource(cpuMetric);

                // HPA rules
                V2HorizontalPodAutoscalerSpec hpaSpec = new V2HorizontalPodAutoscalerSpec()
                                .scaleTargetRef(target)
                                .minReplicas(minReplicas)
                                .maxReplicas(maxReplicas)
                                .metrics(List.of(metricSpec));

                // Complete HPA object
                V2HorizontalPodAutoscaler hpa = new V2HorizontalPodAutoscaler()
                                .apiVersion("autoscaling/v2")
                                .kind("HorizontalPodAutoscaler")
                                .metadata(
                                                new V1ObjectMeta()
                                                                .name(name)
                                                                .namespace("default"))
                                .spec(hpaSpec);

                // Send HPA to Kubernetes
                return autoscalingApi
                                .createNamespacedHorizontalPodAutoscaler(
                                                "default",
                                                hpa)
                                .execute();
        }

        public void deploy(HpaDeployRequest request) throws Exception {

                // 1. Create the Deployment and initial Pod
                createDeployment(
                                request.getName(),
                                request.getImage(),
                                request.getPort(),
                                request.getMinReplicas());

                // 2. Create networking for those Pods
                createService(
                                request.getName(),
                                request.getPort());

                // 3. Create the Horizontal Pod Autoscaler
                createHpa(
                                request.getName(),
                                request.getMinReplicas(),
                                request.getMaxReplicas(),
                                request.getTargetCpu());
        }

        public List<Map<String, Object>> listHpaServices() throws Exception {

                ApiClient client = Config.defaultClient();
                Configuration.setDefaultApiClient(client);

                AutoscalingV2Api autoscalingApi = new AutoscalingV2Api();

                // Get every HPA in the default namespace
                V2HorizontalPodAutoscalerList hpaList = autoscalingApi
                                .listNamespacedHorizontalPodAutoscaler("default")
                                .execute();

                List<Map<String, Object>> result = new ArrayList<>();

                for (V2HorizontalPodAutoscaler hpa : hpaList.getItems()) {

                        Map<String, Object> item = new LinkedHashMap<>();

                        // HPA name
                        item.put(
                                        "name",
                                        hpa.getMetadata().getName());

                        // For our project the HPA targets a Deployment
                        item.put(
                                        "targetDeployment",
                                        hpa.getSpec()
                                                        .getScaleTargetRef()
                                                        .getName());

                        item.put(
                                        "minReplicas",
                                        hpa.getSpec().getMinReplicas());

                        item.put(
                                        "maxReplicas",
                                        hpa.getSpec().getMaxReplicas());

                        if (hpa.getStatus() != null) {

                                item.put(
                                                "currentReplicas",
                                                hpa.getStatus().getCurrentReplicas());

                                item.put(
                                                "desiredReplicas",
                                                hpa.getStatus().getDesiredReplicas());
                        }

                        item.put("type", "HPA");

                        result.add(item);
                }

                return result;
        }

        public V1Deployment getDeployment(String name) throws Exception {

                ApiClient client = Config.defaultClient();
                Configuration.setDefaultApiClient(client);

                AppsV1Api appsApi = new AppsV1Api();

                return appsApi
                                .readNamespacedDeployment(name, "default")
                                .execute();
        }

        public V2HorizontalPodAutoscaler getHpa(String name)
                        throws Exception {

                ApiClient client = Config.defaultClient();
                Configuration.setDefaultApiClient(client);

                AutoscalingV2Api autoscalingApi = new AutoscalingV2Api();

                return autoscalingApi
                                .readNamespacedHorizontalPodAutoscaler(
                                                name,
                                                "default")
                                .execute();
        }

        public void deleteHpaApplication(String name)
                        throws Exception {

                ApiClient client = Config.defaultClient();
                Configuration.setDefaultApiClient(client);

                AppsV1Api appsApi = new AppsV1Api();
                CoreV1Api coreApi = new CoreV1Api();
                AutoscalingV2Api autoscalingApi = new AutoscalingV2Api();

                // Remove the HPA
                autoscalingApi
                                .deleteNamespacedHorizontalPodAutoscaler(
                                                name,
                                                "default")
                                .execute();

                // Remove its Kubernetes Service
                coreApi
                                .deleteNamespacedService(
                                                name + "-service",
                                                "default")
                                .execute();

                // Remove Deployment and its Pods
                appsApi
                                .deleteNamespacedDeployment(
                                                name,
                                                "default")
                                .execute();
        }
}