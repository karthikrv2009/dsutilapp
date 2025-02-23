package com.datapig.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MsalConfigController {

    @Value("${msal.clientId}")
    private String clientId;

    @Value("${msal.authority}")
    private String authority;

    @Value("${msal.redirectUri}")
    private String redirectUri;

    @GetMapping("/msal-config")
    public Map<String, String> getMsalConfig() {
        return Map.of(
                "clientId", clientId,
                "authority", authority,
                "redirectUri", redirectUri);
    }

}
