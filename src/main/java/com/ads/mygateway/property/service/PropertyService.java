package com.ads.mygateway.property.service;

import com.ads.mygateway.exception.PropertyNotFoundException;
import com.ads.mygateway.property.dto.PropertyDTO;
import com.ads.mygateway.property.entity.Property;
import com.ads.mygateway.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyDTO saveProperty(PropertyDTO dto) {
        Property property = mapToEntity(dto);

        // Generate propertyId -> MGWR + yyyyMMdd
        String todayDate = java.time.LocalDate.now().toString().replace("-", "");
        int randomNum = new Random().nextInt(900) + 100;
        String generatedId = "MGWR" + todayDate + randomNum;
        property.setPropertyId(generatedId);

        Property saved = propertyRepository.save(property);
        log.info("Property saved with ID: {}", saved.getPropertyId());
        return mapToDTO(saved);
    }

    public PropertyDTO getById(String id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id " + id));
        return mapToDTO(property);
    }

    // ✅ Now returns LIST because we’re using LIKE
    public List<PropertyDTO> getByName(String name) {
        if (name.length() < 3) {
            throw new IllegalArgumentException("Search query must be at least 3 characters long");
        }
        return propertyRepository.findByPropertyName(name).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<PropertyDTO> getByEmailId(String emailId) {
        List<Property> properties = propertyRepository.findByEmailId(emailId);
        if (properties.isEmpty()) {
            throw new PropertyNotFoundException("No properties found with email " + emailId);
        }
        return properties.stream()
                .map(this::mapToDTO)
                .toList();
    }


    // ✅ Now returns LIST because we’re using LIKE
    public List<PropertyDTO> getByLocation(String location) {
        if (location.length() < 3) {
            throw new IllegalArgumentException("Search query must be at least 3 characters long");
        }
        return propertyRepository.findByLocation(location).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public void deleteById(String id) {
        if (!propertyRepository.existsById(id)) {
            throw new PropertyNotFoundException("Property not found with id " + id);
        }
        propertyRepository.deleteById(id);
        log.info("Deleted property with id {}", id);
    }

    public void deleteByName(String name) {
        List<Property> properties = propertyRepository.findByPropertyName(name);
        if (properties.isEmpty()) {
            throw new PropertyNotFoundException("Property not found with name " + name);
        }
        propertyRepository.deleteByPropertyName(name);
        log.info("Deleted property(s) with name {}", name);
    }

    private PropertyDTO mapToDTO(Property property) {
        return PropertyDTO.builder()
                .propertyId(property.getPropertyId())
                .propertyName(property.getPropertyName())
                .location(property.getLocation())
                .guests(property.getGuests())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .kitchens(property.getKitchens())
                .ratePeriodStart(property.getRatePeriodStart())
                .ratePeriodEnd(property.getRatePeriodEnd())
                .minRate(property.getMinRate())
                .nightlyRate(property.getNightlyRate())
                .weekendRate(property.getWeekendRate())
                .weeklyRate(property.getWeeklyRate())
                .monthlyRate(property.getMonthlyRate())
                .additionalCharges(property.getAdditionalCharges())
                .amenities(property.getAmenities())
                .about(property.getAbout())
                .policyAndHouseRules(property.getPolicyAndHouseRules())
                .images(property.getImages())
                .emailId(property.getEmailId())
                .build();
    }

    private Property mapToEntity(PropertyDTO dto) {
        return Property.builder()
                .propertyId(dto.getPropertyId())
                .propertyName(dto.getPropertyName())
                .location(dto.getLocation())
                .guests(dto.getGuests())
                .bedrooms(dto.getBedrooms())
                .bathrooms(dto.getBathrooms())
                .kitchens(dto.getKitchens())
                .ratePeriodStart(dto.getRatePeriodStart())
                .ratePeriodEnd(dto.getRatePeriodEnd())
                .minRate(dto.getMinRate())
                .nightlyRate(dto.getNightlyRate())
                .weekendRate(dto.getWeekendRate())
                .weeklyRate(dto.getWeeklyRate())
                .monthlyRate(dto.getMonthlyRate())
                .additionalCharges(dto.getAdditionalCharges())
                .amenities(dto.getAmenities())
                .about(dto.getAbout())
                .policyAndHouseRules(dto.getPolicyAndHouseRules())
                .images(dto.getImages())
                .emailId(dto.getEmailId())
                .build();
    }
}
