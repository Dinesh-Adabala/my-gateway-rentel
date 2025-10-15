package com.ads.mygateway.ical.service;

import com.ads.mygateway.ical.entity.IcalEvent;
import com.ads.mygateway.ical.repository.IcalEventRepository;
import com.ads.mygateway.inquiry.entity.Inquiry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Optional<IcalEvent> existing = icalEventRepository.findByInquiryId(inquiry.getEnquiryId());
        if (existing.isPresent()) {
            return existing.get(); // skip duplicate
        }

        if (inquiry.getCheckin() == null || inquiry.getCheckout() == null) {
            throw new IllegalArgumentException("Inquiry must contain checkin and checkout datetimes.");
        }

        String uid = generateUID(inquiry);
        String ics = buildIcsString(uid, inquiry);

        // ✅ Now includes propertyId
        IcalEvent event = new IcalEvent(
                uid,
                inquiry.getEnquiryId(),
                inquiry.getPropertyId(),
                inquiry.getCheckin(),
                inquiry.getCheckout(),
                ics
        );

        return icalEventRepository.save(event);
    }

    private String generateUID(Inquiry inquiry) {
        return inquiry.getEnquiryId() + "-" + UUID.randomUUID();
    }

    private String buildIcsString(String uid, Inquiry inquiry) {
        DateTimeFormatter icsFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        ZoneId systemZone = ZoneId.systemDefault();

        ZonedDateTime dtStartZ = inquiry.getCheckin().atZone(systemZone).withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime dtEndZ = inquiry.getCheckout().atZone(systemZone).withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime dtStamp = ZonedDateTime.now(ZoneOffset.UTC);

        String dtStartStr = dtStartZ.format(icsFormatter);
        String dtEndStr = dtEndZ.format(icsFormatter);
        String dtStampStr = dtStamp.format(icsFormatter);

        String summary = "Booking: " + (inquiry.getPropertyName() != null ? inquiry.getPropertyName() : "Property");
        String description = buildDescription(inquiry);

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
        sb.append("LOCATION:").append(escapeText(inquiry.getPropertyName())).append("\r\n");
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