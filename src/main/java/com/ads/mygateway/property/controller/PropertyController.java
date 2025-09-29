package com.ads.mygateway.property.controller;

import com.ads.mygateway.property.dto.PropertyDTO;
import com.ads.mygateway.property.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    public ResponseEntity<PropertyDTO> saveProperty(@RequestBody PropertyDTO propertyDTO) {
        return ResponseEntity.ok(propertyService.saveProperty(propertyDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(propertyService.getById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<PropertyDTO>> getByName(@PathVariable String name) {
        return ResponseEntity.ok(propertyService.getByName(name));
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<PropertyDTO>> getByLocation(@PathVariable String location) {
        return ResponseEntity.ok(propertyService.getByLocation(location));
    }

    @GetMapping("/email/{emailId}")
    public ResponseEntity<List<PropertyDTO>> getByEmail(@PathVariable String emailId) {
        return ResponseEntity.ok(propertyService.getByEmailId(emailId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id) {
        propertyService.deleteById(id);
        return ResponseEntity.ok("Property deleted with id " + id);
    }

    @DeleteMapping("/name/{name}")
    public ResponseEntity<String> deleteByName(@PathVariable String name) {
        propertyService.deleteByName(name);
        return ResponseEntity.ok("Property deleted with name " + name);
    }
}
