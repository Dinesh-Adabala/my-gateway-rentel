package com.ads.mygateway.inquiry.service;

import com.ads.mygateway.exception.ResourceNotFoundException;
import com.ads.mygateway.ical.entity.IcalEvent;
import com.ads.mygateway.ical.service.IcalEventService;
import com.ads.mygateway.inquiry.dto.InquiryRequestDTO;
import com.ads.mygateway.inquiry.dto.InquiryResponseDTO;
import com.ads.mygateway.inquiry.entity.Inquiry;
import com.ads.mygateway.inquiry.repository.InquiryRepository;
import com.ads.mygateway.loginservice.EmailService;
import com.ads.mygateway.property.entity.Property;
import com.ads.mygateway.property.repository.PropertyRepository;
import com.ads.mygateway.util.MailTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final PropertyRepository propertyRepository;
    private final IcalEventService icalEventService;
    private final EmailService emailService;
    // ✅ POST: Save Inquiry
    public InquiryResponseDTO saveInquiry(InquiryRequestDTO req) {
        LocalDateTime checkin = null, checkout = null;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            if (req.getCheckin() != null) {
                if (req.getCheckin().length() == 10) { // yyyy-MM-dd
                    checkin = LocalDate.parse(req.getCheckin(), dateFormatter).atStartOfDay();
                } else {
                    checkin = LocalDateTime.parse(req.getCheckin());
                }
            }
            if (req.getCheckout() != null) {
                if (req.getCheckout().length() == 10) {
                    checkout = LocalDate.parse(req.getCheckout(), dateFormatter).atStartOfDay();
                } else {
                    checkout = LocalDateTime.parse(req.getCheckout());
                }
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss");
        }
        String enquiryId = generateEnquiryId();
        Inquiry inquiry = Inquiry.builder()
                .enquiryId(enquiryId)
                .propertyId(req.getPropertyId())
                .propertyName(req.getPropertyName())
                .propertyImages(req.getPropertyImages())
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .gender(req.getGender())
                .guests(req.getGuests())
                .totalAmount(req.getTotalAmount())
                .message(req.getMessage())
                .checkin(checkin)
                .checkout(checkout)
                .nights(req.getNights())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status("PENDING")
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);

        // Send email to guest
        String guestHtml = MailTemplates.guestInquiryCreated(
                saved.getFullName(), saved.getPropertyName(),
                saved.getCheckin().toLocalDate().toString(),
                saved.getCheckout().toLocalDate().toString());
        emailService.sendHtmlMessage(saved.getEmail(),
                "Booking Request Received - " + saved.getPropertyName(), guestHtml);

        // Send email to property owner
        Property property = propertyRepository.findById(saved.getPropertyId()).orElse(null);
        if (property != null && property.getEmailId() != null) {
            String ownerHtml = MailTemplates.ownerInquiryNotification(
                    saved.getPropertyName(), saved.getFullName(), saved.getEmail(),
                    saved.getPhone(), saved.getCheckin().toLocalDate().toString(),
                    saved.getCheckout().toLocalDate().toString(),
                    saved.getGuests() + "");
            emailService.sendHtmlMessage(property.getEmailId(),
                    "New Booking Inquiry for " + saved.getPropertyName(), ownerHtml);
        }
        return mapToDTO(saved);
    }

    /// --- Accept inquiry ---
    public InquiryResponseDTO acceptInquiry(String enquiryId) {
        Inquiry inquiry = inquiryRepository.findByEnquiryId(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found for enquiryId: " + enquiryId));

        if (!"BOOKED".equalsIgnoreCase(inquiry.getStatus())) {
            inquiry.setStatus("BOOKED");
            inquiry.setUpdatedAt(LocalDateTime.now());
            inquiryRepository.save(inquiry);
            // Create and save iCal event (will not duplicate if one exists)
            IcalEvent event = icalEventService.createEventForInquiry(inquiry);

            // Optionally: attach event UID or link to your response DTO
            // e.g. mapToDTO(inquiry).setIcalUid(event.getUid());

            // Send booking confirmed email
            String confirmHtml = MailTemplates.bookingConfirmed(
                    inquiry.getFullName(), inquiry.getPropertyName(),
                    inquiry.getCheckin().toLocalDate().toString(),
                    inquiry.getCheckout().toLocalDate().toString());
            emailService.sendHtmlMessage(inquiry.getEmail(),
                    "Booking Confirmed - " + inquiry.getPropertyName(), confirmHtml);
        }

        return mapToDTO(inquiry);
    }


    // ✅ GET: Fetch Inquiries by Owner Email
    public List<InquiryResponseDTO> getInquiriesByEmail(String emailId) {
        List<Property> properties = propertyRepository.findByEmailId(emailId);

        if (properties.isEmpty()) {
            return List.of();
        }

        List<String> propertyIds = properties.stream()
                .map(Property::getPropertyId)
                .collect(Collectors.toList());

        List<Inquiry> inquiries = inquiryRepository.findByPropertyIdIn(propertyIds);

        return inquiries.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private InquiryResponseDTO mapToDTO(Inquiry inquiry) {
        return InquiryResponseDTO.builder()
                .id(inquiry.getId())
                .enquiryId(inquiry.getEnquiryId())
                .propertyId(inquiry.getPropertyId())
                .propertyName(inquiry.getPropertyName())
                .propertyImages(inquiry.getPropertyImages())
                .fullName(inquiry.getFullName())
                .email(inquiry.getEmail())
                .phone(inquiry.getPhone())
                .gender(inquiry.getGender())
                .guests(inquiry.getGuests())
                .totalAmount(inquiry.getTotalAmount())
                .message(inquiry.getMessage())
                .checkin(inquiry.getCheckin())
                .checkout(inquiry.getCheckout())
                .nights(inquiry.getNights())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .status(inquiry.getStatus())
                .build();
    }

    private String generateEnquiryId() {
        String prefix = "ENQ";
        String date = java.time.LocalDate.now().toString().replace("-", "");
        int random = new Random().nextInt(9000) + 1000;
        return prefix + date + random; // ENQ202510081234
    }

    // ✅ Get single inquiry by enquiryId
    public InquiryResponseDTO getInquiryByEnquiryId(String enquiryId) {
        Inquiry inquiry = inquiryRepository.findByEnquiryId(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found for enquiryId: " + enquiryId));
        return mapToDTO(inquiry);
    }

    // ✅ Get all inquiries
    public List<InquiryResponseDTO> getAllInquiries() {
        List<Inquiry> allInquiries = inquiryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return allInquiries.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

}