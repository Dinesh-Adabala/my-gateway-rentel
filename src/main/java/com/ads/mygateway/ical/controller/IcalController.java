package com.ads.mygateway.ical.controller;

import com.ads.mygateway.ical.dto.BookedDateDTO;
import com.ads.mygateway.ical.service.IcalImportExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ical")
public class IcalController {

    private final IcalImportExportService service;
    private final Logger log = LoggerFactory.getLogger(IcalController.class);

    public IcalController(IcalImportExportService service) {
        this.service = service;
    }

    // Manual import endpoint if you want to call once
    @PostMapping("/import")
    public ResponseEntity<?> importIcs(@RequestBody Map<String, String> body) {
        String propertyId = body.get("propertyId");
        String sourceUrl = body.get("sourceUrl");
        if (propertyId == null || sourceUrl == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "propertyId and sourceUrl required"));
        }
        try {
            int imported = service.importFromUrl(propertyId, sourceUrl);
            return ResponseEntity.ok(Map.of("status", "ok", "imported", imported));
        } catch (Exception ex) {
            log.error("Manual import error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", ex.getMessage()));
        }
    }

    // Export combined .ics for property (URL style matches your screenshot pattern)
    @GetMapping(value = "/export/{propertyId}.ics", produces = "text/calendar; charset=utf-8")
    public ResponseEntity<byte[]> exportIcs(@PathVariable String propertyId) {
        String ics = service.exportForProperty(propertyId);
        byte[] body = ics.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"property-" + propertyId + ".ics\"");
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/booked-dates/{propertyId}")
    public ResponseEntity<List<BookedDateDTO>> getBookedDates(@PathVariable String propertyId) {
        return ResponseEntity.ok(service.getBookedDatesForProperty(propertyId));
    }

}
