package com.mertkacar.controller;

import com.mertkacar.businiess.abstracts.UnitService;
import com.mertkacar.dto.requests.UnitRequest;
import com.mertkacar.model.Unit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController("/api/unit")
@RequiredArgsConstructor
public class UnitController
{
    private final UnitService unitService;

    @GetMapping("/getAll")
    public ResponseEntity<List<Unit>> getAll() {
        return ResponseEntity.ok(unitService.list());
    }

    @GetMapping("/getById")
    public ResponseEntity<Unit> getUnit(@RequestParam UUID id) {
        return ResponseEntity.ok(unitService.get(id));
    }


    @PostMapping("/create")
    public ResponseEntity<Unit> create(@RequestBody UnitRequest req) {
        return ResponseEntity.ok(unitService.create(req));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestParam UUID id) {
     return  ResponseEntity.ok(unitService.delete(id));
    }
}
