package com.mertkacar.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {


    @GetMapping("/public/hello")
    @PreAuthorize("permitAll()")
    public String publicHello() {
        return "Hello World (public)";
    }

    @GetMapping("/user/hello")
    public String userHello() {
        return "Hello User (authenticated)";
    }

    @GetMapping("/admin/hello")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminHello() {
        return "Hello Admin (only for ADMIN role)";
    }
}
