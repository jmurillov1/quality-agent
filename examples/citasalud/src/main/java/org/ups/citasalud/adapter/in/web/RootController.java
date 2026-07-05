package org.ups.citasalud.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "app", "CitaSalud API",
                "health", "/actuator/health",
                "doctors", "/v1/doctors",
                "appointments", "/v1/appointments"
        ));
    }
}
