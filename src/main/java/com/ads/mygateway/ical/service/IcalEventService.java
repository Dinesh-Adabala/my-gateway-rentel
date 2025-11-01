package com.ads.mygateway.ical.service;

import com.ads.mygateway.ical.entity.IcalEvent;
import com.ads.mygateway.ical.repository.IcalEventRepository;
import com.ads.mygateway.inquiry.entity.Inquiry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
public class IcalEventService {

    private final IcalEventRepository icalEventRepository;

    public IcalEventService(IcalEventRepository icalEventRepository) {
        this.icalEventRepository = icalEventRepository;
    }

    @Transactional
    public IcalEvent createEventForInquiry(Inquiry inquiry) {
        if (inquiry == null) throw new IllegalArgumentException("inquiry is required");
        // check for existing event for the inquiry (skip duplicates)
        Optional<IcalEvent> existing = icalEventRepository.findByInquiryId(inquiry.getEnquiryId());
        if (existing.isPresent()) {
            return existing.get();
        }

        // Validate required fields
        if (inquiry.getCheckin() == null || inquiry.getCheckout() == null) {
            throw new IllegalArgumentException("Inquiry must contain checkin and checkout datetimes.");
        }
        if (!StringUtils.hasText(inquiry.getPropertyId())) {
            throw new IllegalArgumentException("Inquiry must contain propertyId.");
        }

        String uid = generateUID(inquiry); // inquiryId + random UUID
        String ics = buildIcsString(uid, inquiry);

        IcalEvent event = new IcalEvent();
        event.setUid(uid);
        event.setInquiryId(inquiry.getEnquiryId());
        event.setPropertyId(inquiry.getPropertyId());
        event.setDtStart(inquiry.getCheckin());
        event.setDtEnd(inquiry.getCheckout());
        event.setIcsContent(ics);
        event.setSource("mygatewayrental"); // IMPORTANT: mark as internal

        return icalEventRepository.save(event);
    }


    private String generateUID(Inquiry inquiry) {
        return inquiry.getEnquiryId() + "-" + UUID.randomUUID();
    }

    private String buildIcsString(String uid, Inquiry inquiry) {
        DateTimeFormatter icsFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(ZoneOffset.UTC);
        ZoneId systemZone = ZoneId.systemDefault();

        Instant startInstant = inquiry.getCheckin().atZone(systemZone).toInstant();
        Instant endInstant = inquiry.getCheckout().atZone(systemZone).toInstant();
        Instant stampInstant = Instant.now();

        String dtStartStr = icsFormatter.format(startInstant);
        String dtEndStr = icsFormatter.format(endInstant);
        String dtStampStr = icsFormatter.format(stampInstant);

        String summary = "Booking: " + (StringUtils.hasText(inquiry.getPropertyName()) ? inquiry.getPropertyName() : "Property");
        String description = buildDescription(inquiry);
        String location = StringUtils.hasText(inquiry.getPropertyName()) ? inquiry.getPropertyName() : "";

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR").append("\r\n");
        sb.append("PRODID:-//YourCompany//BookingCalendar//EN").append("\r\n");
        sb.append("VERSION:2.0").append("\r\n");
        sb.append("CALSCALE:GREGORIAN").append("\r\n");
        sb.append("METHOD:PUBLISH").append("\r\n");
        sb.append("BEGIN:VEVENT").append("\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(dtStampStr).append("\r\n");
        sb.append("DTSTART:").append(dtStartStr).append("\r\n");
        sb.append("DTEND:").append(dtEndStr).append("\r\n");
        sb.append("SUMMARY:").append(escapeText(summary)).append("\r\n");
        sb.append("DESCRIPTION:").append(escapeText(description)).append("\r\n");
        sb.append("LOCATION:").append(escapeText(location)).append("\r\n");
        sb.append("END:VEVENT").append("\r\n");
        sb.append("END:VCALENDAR").append("\r\n");

        return sb.toString();
    }


    private String buildDescription(Inquiry inquiry) {
        StringBuilder d = new StringBuilder();
        d.append("Property ID: ").append(inquiry.getPropertyId() != null ? inquiry.getPropertyId() : "").append("\\n");
        d.append("Guest: ").append(inquiry.getFullName() != null ? inquiry.getFullName() : "").append("\\n");
        d.append("Email: ").append(inquiry.getEmail() != null ? inquiry.getEmail() : "").append("\\n");
        d.append("Phone: ").append(inquiry.getPhone() != null ? inquiry.getPhone() : "").append("\\n");
        d.append("Guests: ").append(inquiry.getGuests() != null ? inquiry.getGuests() : "").append("\\n");
        d.append("Total: ").append(inquiry.getTotalAmount() != null ? inquiry.getTotalAmount() : "").append("\\n");
        if (inquiry.getMessage() != null) {
            d.append("Message: ").append(inquiry.getMessage()).append("\\n");
        }
        return d.toString();
    }

    private String escapeText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }
}