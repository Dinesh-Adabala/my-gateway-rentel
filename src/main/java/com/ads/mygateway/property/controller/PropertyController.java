package com.ads.mygateway.property.controller;

import com.ads.mygateway.property.dto.LocationSuggestionDTO;
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
    @PutMapping("/edit-property")
    public ResponseEntity<PropertyDTO> editProperty(@RequestBody PropertyDTO dto) {
        PropertyDTO updated = propertyService.updateProperty(dto);
        return ResponseEntity.ok(updated);
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

    // ✅ Fetch properties by location + checkin/checkout
    @GetMapping("/fetch-property-by-location")
    public ResponseEntity<List<PropertyDTO>> getByLocationAndAvailability(
            @RequestBody PropertySearchRequest request
    ) {
        List<PropertyDTO> result = propertyService.getByLocationAndAvailability(
                request.getName(),
                request.getCheckin(),
                request.getCheckout()
        );
        return ResponseEntity.ok(result);
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

    @GetMapping("/fetch-all-property")
    public ResponseEntity<List<PropertyDTO>> getAllProperty() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }
    @GetMapping("/location-suggestions")
    public ResponseEntity<List<LocationSuggestionDTO>> suggestLocations(@RequestParam("q") String q) {
        List<LocationSuggestionDTO> list = propertyService.suggestLocations(q);
        return ResponseEntity.ok(list);
    }
}
