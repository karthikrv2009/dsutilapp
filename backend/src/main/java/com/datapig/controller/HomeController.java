package com.datapig.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping(value = { "/", "/login", "/landing", "/dashboard", "/database-config", "/license", "/changelog" })
    public String serveReactApp() {
        return "forward:/index.html"; // Forward request to React app
    }
}