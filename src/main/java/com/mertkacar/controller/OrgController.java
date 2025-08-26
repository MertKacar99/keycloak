package com.mertkacar.controller;

import com.mertkacar.businiess.abstracts.InstitutionService;
import com.mertkacar.model.Institution;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
public class OrgController {

    private final InstitutionService institutionService;
//    private final UnitService unitService;
//    private final UserOrgService userOrgService;

    // --- Institution ---
    @PostMapping("/institutions/create")
    public ResponseEntity<?> createInstitution(@RequestBody Institution req) {
        var saved = institutionService.create(req);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/institutions")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<?> listInstitutions() {
        return ResponseEntity.ok(institutionService.list());
    }

    // --- Unit ---
//    @PostMapping("/units")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> createUnit(@RequestBody UnitRequest req) {
//        var saved = unitService.create(req);
//        return ResponseEntity.ok(saved);
//    }

//    @GetMapping("/units")
//    @PreAuthorize("hasAnyRole('ADMIN','USER')")
//    public ResponseEntity<?> listUnits() {
//        return ResponseEntity.ok(unitService.list());
//    }

    // --- User ↔ Institution ---
//    @PostMapping("/users/change-institution")
//    @PreAuthorize("hasAuthority('PERM_USERS_UPDATE')")
//    public ResponseEntity<?> changeInstitution(@RequestBody ChangeInstitutionRequest req) {
//        userOrgService.changeUserInstitution(req);
//        return ResponseEntity.ok(Map.of("message", "User institution updated"));
//    }

    // --- User ↔ Units ---
//    @PostMapping("/users/assign-units")
//    @PreAuthorize("hasAuthority('PERM_USERS_UPDATE')")
//    public ResponseEntity<?> assignUnits(@RequestBody AssignUnitsRequest req) {
//        userOrgService.assignUnits(req);
//        return ResponseEntity.ok(Map.of("message", "Units assigned"));
//    }
//
//    @DeleteMapping("/users/{userId}/units/{unitId}")
//    @PreAuthorize("hasAuthority('PERM_USERS_UPDATE')")
//    public ResponseEntity<?> removeUnit(@PathVariable UUID userId, @PathVariable UUID unitId) {
//        userOrgService.removeUnit(userId, unitId);
//        return ResponseEntity.ok(Map.of("message", "Unit removed from user"));
//    }
}
