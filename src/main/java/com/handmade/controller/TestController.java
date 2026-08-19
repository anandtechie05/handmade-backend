package com.handmade.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/protected")
    public String protectedEndpoint(Authentication authentication) {

        return "JWT authentication successful! Logged in as: "
                + authentication.getName();
    }
}