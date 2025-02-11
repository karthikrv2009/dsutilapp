package com.datapig.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service")
public class ServiceController {

    @Autowired
    private ConfigurableApplicationContext context;

    @PostMapping("/stop")
    public void stopService() {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000); // Give some time for the response to be sent
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            context.close();
        });
        thread.setDaemon(false);
        thread.start();
    }

    @PostMapping("/start")
    public void startService() {
        // Logic to start the service
        // This typically involves starting the application context
    }
}
