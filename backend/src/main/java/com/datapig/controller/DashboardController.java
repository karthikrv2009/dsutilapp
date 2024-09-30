package com.datapig.controller; 

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("metric1", "50%");
        metrics.put("metric2", "75%");
        // Add more metrics as needed

        return ResponseEntity.ok(metrics);
    }
}
