package com.mertkacar.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {


    @GetMapping("/public/hello")
    @PreAuthorize("permitAll()")
    public String publicHello() {
        return "Hello World (public)";
    }

    @GetMapping("/user/hello")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasAuthority('PERM_USERS_UPDATE') and hasAuthority('PERM_USERS_DELETE')")

    public String userHello() {
        return "Hello User (authenticated)";
    }

    @GetMapping("/admin/hello")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_USERS_UPDATE')")
    public String adminHello() {
        return "Hello Admin (only for ADMIN role)";
    }

    // Görüntüleme (READ)
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_USERS_UPDATE')")
    public String listUsers() {
        return "Users listed";
    }

    // Güncelleme (UPDATE)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') and hasAuthority('PERM_USERS_UPDATE')")
    public String updateUser(@PathVariable Long id) {
        return "User " + id + " updated";
    }

    // Silme (DELETE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') and hasAuthority('PERM_USERS_DELETE')")
    public String deleteUser(@PathVariable Long id) {
        return "User " + id + " deleted";
    }

}
