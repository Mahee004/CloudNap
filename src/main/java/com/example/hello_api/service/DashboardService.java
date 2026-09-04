package com.example.hello_api.service;

import org.springframework.stereotype.Service;

import com.example.hello_api.model.PodSummary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final KnativeService knativeService;
    private final HpaService hpaService;
    private final PodService podService;

    public DashboardService(
            KnativeService knativeService,
            HpaService hpaService,
            PodService podService) {

        this.knativeService = knativeService;
        this.hpaService = hpaService;
        this.podService = podService;
    }

    public List<Map<String, Object>> getAllServices()
            throws Exception {

        List<Map<String, Object>> result = new ArrayList<>();

        // -------------------------
        // KNATIVE
        // -------------------------

        List<Map<String, Object>> knativeServices = knativeService.listServices();

        for (Map<String, Object> service : knativeServices) {

            Map<String, Object> item = new LinkedHashMap<>(service);

            String name = service.get("name").toString();

            PodSummary pods = podService.getKnativePods(name);

            item.put("type", "SCALE_TO_ZERO");

            item.put(
                    "pods",
                    pods.getPodCount());

            item.put(
                    "runningPods",
                    pods.getRunningPods());

            result.add(item);
        }

        // -------------------------
        // HPA
        // -------------------------

        List<Map<String, Object>> hpaServices = hpaService.listHpaServices();

        for (Map<String, Object> service : hpaServices) {

            Map<String, Object> item = new LinkedHashMap<>(service);

            String name = service.get("name").toString();

            PodSummary pods = podService.getHpaPods(name);

            item.put("type", "HPA");

            item.put(
                    "pods",
                    pods.getPodCount());

            item.put(
                    "runningPods",
                    pods.getRunningPods());

            result.add(item);
        }

        return result;
    }
}