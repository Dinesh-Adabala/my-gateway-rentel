package com.ads.mygateway.inquiry.controller;

import com.ads.mygateway.inquiry.dto.InquiryRequestDTO;
import com.ads.mygateway.inquiry.dto.InquiryResponseDTO;
import com.ads.mygateway.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/inquiries")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InquiryController {

    private final InquiryService inquiryService;

    // ✅ POST - Save Inquiry
    @PostMapping("/create-inquiry")
    public ResponseEntity<InquiryResponseDTO> createInquiry(@RequestBody InquiryRequestDTO request) {
        return ResponseEntity.ok(inquiryService.saveInquiry(request));
    }

    // ✅ GET - Get all inquiries for properties owned by email
    @GetMapping("/fetch-inquiry-emailId/{emailId}")
    public ResponseEntity<List<InquiryResponseDTO>> getInquiriesByOwnerEmail(@PathVariable String emailId) {
        return ResponseEntity.ok(inquiryService.getInquiriesByEmail(emailId));
    }

    @PostMapping("/{enquiryId}/accept")
    public ResponseEntity<InquiryResponseDTO> acceptInquiry(@PathVariable String enquiryId) {
        return ResponseEntity.ok(inquiryService.acceptInquiry(enquiryId));
    }

    @GetMapping("/fetch-by-id/{enquiryId}")
    public ResponseEntity<InquiryResponseDTO> getInquiryByEnquiryId(@PathVariable String enquiryId) {
        return ResponseEntity.ok(inquiryService.getInquiryByEnquiryId(enquiryId));
    }

    // ✅ NEW 2: Fetch all inquiries (for admin or overall list)
    @GetMapping("/fetch-all")
    public ResponseEntity<List<InquiryResponseDTO>> getAllInquiries() {
        return ResponseEntity.ok(inquiryService.getAllInquiries());
    }
}
