# CloudNap

A scale-to-zero management dashboard for Kubernetes. CloudNap watches live traffic to a service, compares it against how the service is currently deployed (Knative Scale-to-Zero vs. HPA), and recommends — or applies — a switch between the two.

## Why

Kubernetes gives you two common ways to handle variable load:

- **Knative (Scale-to-Zero)** — scales down to 0 pods when idle, saving resources, but pays a cold-start cost when traffic returns.
- **HPA (Horizontal Pod Autoscaler)** — keeps at least one pod warm at all times, so there's no cold start, but it costs resources even when idle.

Neither is right all the time. CloudNap measures real traffic through Prometheus and recommends whichever mode fits the current load, so a service isn't paying the cold-start tax under real usage, or burning resources while idle.

## Architecture

```
┌─────────────┐      ┌──────────────────┐      ┌─────────────┐
│   React     │ ───▶ │   Spring Boot    │ ───▶ │  Prometheus │
│  Dashboard  │ ◀─── │     Backend      │ ◀─── │             │
└─────────────┘      └──────────────────┘      └─────────────┘
                              │                        ▲
                              ▼                        │
                       ┌─────────────┐          scrapes /actuator/prometheus
                       │  Kubernetes │          on Knative + HPA pods
                       │     API     │
                       └─────────────┘
                              │
                ┌─────────────┴─────────────┐
                ▼                            ▼
        Knative Services              Deployments + HPA
        (scale-to-zero)              (always-on replicas)
```

The backend reads live cluster state (services, pods, nodes) directly from the Kubernetes API, and reads request volume from Prometheus. A small rule engine combines the two into a recommendation.

## Recommendation rules

| Current mode | Traffic | Recommendation |
|---|---|---|
| HPA | Idle | Switch to Scale-to-Zero |
| HPA | Active | Keep HPA |
| Scale-to-Zero | Idle | Keep Scale-to-Zero |
| Scale-to-Zero | Active, below threshold | Keep Scale-to-Zero (Knative autoscaling can absorb it) |
| Scale-to-Zero | Active, at/above threshold | Switch to HPA (avoid repeated cold starts) |
| Any | No traffic data yet | No recommendation |

The "sustained traffic" threshold is a request-count-per-window value, configurable in `RecommendationService.java`.

## Tech stack

- **Backend**: Spring Boot (Java 21), Kubernetes Java client, Micrometer/Actuator for metrics
- **Frontend**: React + Vite
- **Cluster**: Kubernetes (tested on Docker Desktop's built-in `kind`-based cluster)
- **Autoscaling**: Knative Serving (Scale-to-Zero), Horizontal Pod Autoscaler + metrics-server
- **Monitoring**: Prometheus (self-hosted, scraping app metrics via `kubernetes_sd_configs` pod discovery)

## Project structure

```
scale-to-zero/
├── src/main/java/com/example/hello_api/
│   ├── controller/     REST endpoints (dashboard, cluster, traffic, recommendations, conversion)
│   ├── service/        Business logic (Knative, HPA, Pod, Traffic, Recommendation services)
│   ├── model/          Response DTOs (NodeSummary, PodSummary, TrafficSummary, RecommendationSummary)
│   └── config/         Kubernetes API client configuration
├── frontend/
│   └── src/
│       ├── components/ ServiceCard, NodeCard, Navbar
│       ├── pages/       Dashboard
│       └── services/    api.js — fetch wrappers for the backend
└── k8s/
    ├── knative-service.yaml       Knative (Scale-to-Zero) sample service
    ├── deployment.yaml            Base Deployment
    ├── service.yaml               Base Service
    ├── hpa-deployment.yaml        HPA-mode Deployment
    ├── hpa-service.yaml           HPA-mode Service
    ├── hpa.yaml                   HorizontalPodAutoscaler
    ├── monitoring-stack.yaml      Prometheus (namespace, RBAC, config, deployment)
    └── components.yaml            metrics-server (required for HPA)
```

## Prerequisites

- Docker Desktop with **Kubernetes enabled** (Settings → Kubernetes → Enable Kubernetes)
- `kubectl` configured to the `docker-desktop` context
- Java 21 and Maven (or use the included `./mvnw` wrapper)
- Node.js + npm

## One-time cluster setup

These only need to be run once per cluster (they don't persist across `kind` cluster resets).

**1. Install metrics-server** (required for HPA to read CPU usage):
```
kubectl apply -f k8s/components.yaml
```

**2. Install Knative Serving:**
```
kubectl apply -f https://github.com/knative/serving/releases/download/knative-v1.15.0/serving-crds.yaml
kubectl apply -f https://github.com/knative/serving/releases/download/knative-v1.15.0/serving-core.yaml
```

**3. Install Kourier (Knative's networking layer):**
```
kubectl apply -f https://github.com/knative/net-kourier/releases/download/knative-v1.15.0/kourier.yaml
kubectl patch configmap/config-network --namespace knative-serving --type merge --patch "{\"data\":{\"ingress-class\":\"kourier.ingress.networking.knative.dev\"}}"
```

**4. Deploy the monitoring stack (Prometheus):**
```
kubectl apply -f k8s/monitoring-stack.yaml
```

**5. Deploy the sample services:**
```
kubectl apply -f k8s/knative-service.yaml
kubectl apply -f k8s/hpa-deployment.yaml
kubectl apply -f k8s/hpa-service.yaml
kubectl apply -f k8s/hpa.yaml
```

**Verify everything is healthy:**
```
kubectl get pods -n knative-serving
kubectl get pods -n kourier-system
kubectl get pods -n monitoring
kubectl get ksvc
kubectl get deployments
```

## Running the app

Three terminals, run side by side:

**1. Port-forward Prometheus** (the backend queries it over HTTP):
```
kubectl port-forward service/prometheus -n monitoring 9090:9090
```

**2. Run the backend:**
```
./mvnw spring-boot:run
```
Runs on `http://localhost:8080`.

**3. Run the frontend:**
```
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:5173` (or whatever port Vite prints).

## API reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard/services` | List all detected services (Knative + HPA) with pod counts |
| `GET` | `/api/cluster/nodes` | List cluster nodes with CPU/memory capacity |
| `GET` | `/api/traffic/{serviceName}` | Recent request volume for a service, from Prometheus |
| `GET` | `/api/recommendations/{serviceName}` | Traffic + current mode → scaling recommendation |
| `POST` | `/api/convert/{serviceName}/to-hpa` | Convert a Knative service to HPA mode |
| `POST` | `/api/convert/{serviceName}/to-knative` | Convert an HPA service to Scale-to-Zero mode |

## Generating test traffic

Prometheus only sees traffic once the app actually receives requests. Port-forward to a pod directly and hit the `/hello` endpoint (the metric Prometheus/`TrafficService` filters on):

```
kubectl get pods -l app=<label>          # or -l serving.knative.dev/service=<name> for Knative
kubectl port-forward <pod-name> 8081:8080
```

In a second terminal:
```
for /L %i in (1,1,30) do curl http://localhost:8081/hello
```

Then check:
```
curl http://localhost:8080/api/recommendations/<service-name>
```

## Known limitations

- The recommendation threshold is a static value, not adaptive to historical patterns yet.
- Prometheus scrape jobs currently match on fixed service-name regexes in `monitoring-stack.yaml`; a new service needs either a matching name or a new scrape job to be monitored.
- No authentication on the API — intended for local/cluster-internal use, not public exposure.
