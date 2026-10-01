package com.omena.immo.adapter.in.web;

import com.omena.immo.adapter.out.health.InfrastructureHealthService;
import com.omena.immo.adapter.out.health.InfrastructureHealthService.SystemHealth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemHealthController {

    private final InfrastructureHealthService healthService;

    public SystemHealthController(InfrastructureHealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/health")
    public SystemHealth health() {
        return healthService.inspect();
    }
}
