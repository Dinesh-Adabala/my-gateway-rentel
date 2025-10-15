package com.ads.mygateway.property.controller;

import com.ads.mygateway.property.dto.PropertyDTO;
import com.ads.mygateway.property.dto.PropertySearchRequest;
import com.ads.mygateway.property.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping("/create-property")
    public ResponseEntity<PropertyDTO> saveProperty(@RequestBody PropertyDTO propertyDTO) {
        return ResponseEntity.ok(propertyService.saveProperty(propertyDTO));
    }

    @GetMapping("/fetch-property-by-id/{id}")
    public ResponseEntity<PropertyDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(propertyService.getById(id));
    }

    @GetMapping("/fetch-property-by-name")
    public ResponseEntity<List<PropertyDTO>> getByNameAndAvailability(
            @RequestBody PropertySearchRequest request
    ) {
        List<PropertyDTO> result = propertyService.getByNameAndAvailability(request.getName(),request.getCheckin(),request.getCheckout());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/fetch-property-by-location/{location}")
    public ResponseEntity<List<PropertyDTO>> getByLocation(@PathVariable String location) {
        return ResponseEntity.ok(propertyService.getByLocation(location));
    }

    @GetMapping("/fetch-property-by-email/{emailId}")
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
