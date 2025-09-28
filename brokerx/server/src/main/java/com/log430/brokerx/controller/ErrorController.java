package com.log430.brokerx.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorController {
    @GetMapping("/api/test-error")
    public String error() {
        return "Page not found! 404";
    }
}