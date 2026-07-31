package com.efgenbosh.backend.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApplicationStatus health() {
        return new ApplicationStatus("UP", "efgen-bosh-app");
    }

    public record ApplicationStatus(String status, String service) {
    }
}
