package com.example.starter.controller;

import com.example.starter.dto.ComponentRequest;
import com.example.starter.dto.ComponentResponse;
import com.example.starter.service.ComponentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {

    private final ComponentService componentService;

    @GetMapping
    public List<ComponentResponse> list(@RequestParam(required = false) String category) {
        return componentService.findAll(category);
    }

    @GetMapping("/{id}")
    public ComponentResponse get(@PathVariable Long id) {
        return componentService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ComponentResponse> create(@Valid @RequestBody ComponentRequest req) {
        ComponentResponse created = componentService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ComponentResponse update(@PathVariable Long id, @Valid @RequestBody ComponentRequest req) {
        return componentService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        componentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}