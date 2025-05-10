package com.example.auth_server.config;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@RestController
public class ErrorConfig implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        return "Auth server is running";
    }
} 