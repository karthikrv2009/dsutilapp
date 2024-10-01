package com.datapig.controller; 


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("metric1", "50%");
        metrics.put("metric2", "75%");
        
        // Log the metrics being sent to the frontend
        logger.info("Sending dashboard metrics: {}", metrics);

        return ResponseEntity.ok(metrics);
    }
}
