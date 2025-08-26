package com.mertkacar.controller;

import com.mertkacar.businiess.abstracts.AdminUserRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mertkacar.dto.requests.AssignRolesRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserRoleController {

    private final AdminUserRolService adminUserRoleService;

    // Yalnızca ADMIN (veya PERM_EDIT_USERS) diğer kullanıcılara rol
    @PostMapping("/assign-roles")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_USERS_UPDATE')")
    public ResponseEntity<?> assignRoles(@RequestBody AssignRolesRequest req) {
        var result = adminUserRoleService.assignRoles(req);
        return ResponseEntity.ok(result); // 200 + JSON body
    }



}
