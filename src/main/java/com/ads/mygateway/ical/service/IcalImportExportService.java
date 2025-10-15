package com.ads.mygateway.ical.service;

import com.ads.mygateway.ical.entity.IcalEvent;
import com.ads.mygateway.ical.repository.IcalEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IcalImportExportService {

    private final IcalEventRepository repo;
    private final HttpClient httpClient;
    private final Logger log = LoggerFactory.getLogger(IcalImportExportService.class);

    public IcalImportExportService(IcalEventRepository repo) {
        this.repo = repo;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Import a remote .ics URL for a specific propertyId.
     * Returns number of newly imported events.
     */
    @Transactional
    public int importFromUrl(String propertyId, String url) throws IOException, InterruptedException {
        String ics = fetchRemoteIcs(url);
        if (ics == null || ics.trim().isEmpty()) return 0;

        ics = unfoldIcs(ics);
        List<String> vevents = extractVevenBlocks(ics);
        int imported = 0;

        for (String veventBody : vevents) {
            String rawUid = extractSingleField(veventBody, "UID").orElse(null);
            String uid = (rawUid == null || rawUid.isBlank()) ? UUID.randomUUID().toString() : rawUid;

            // Skip if already exists for this property
            if (repo.findByUidAndPropertyId(uid, propertyId).isPresent()) {
                continue;
            }

            Optional<String> dtStartRaw = extractSingleField(veventBody, "DTSTART");
            Optional<String> dtEndRaw   = extractSingleField(veventBody, "DTEND");
            Optional<String> descOpt    = extractSingleField(veventBody, "DESCRIPTION");
            Optional<String> summaryOpt = extractSingleField(veventBody, "SUMMARY");
            Optional<String> locOpt     = extractSingleField(veventBody, "LOCATION");

            LocalDateTime dtStart = parseIcsDateTime(dtStartRaw.orElse(null));
            LocalDateTime dtEnd = parseIcsDateTime(dtEndRaw.orElse(null));
            if (dtStart == null) {
                // skip if no valid start time (you can change to fallback behavior)
                log.warn("Skipping VEVENT without DTSTART for propertyId={}, uid={}", propertyId, uid);
                continue;
            }
            if (dtEnd == null) {
                // If DTEND missing, assume one night/day or keep as start + 1 day
                dtEnd = dtStart.plusDays(1);
            }

            String singleIcs = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nBEGIN:VEVENT\r\n" + veventBody.trim() + "\r\nEND:VEVENT\r\nEND:VCALENDAR\r\n";

            IcalEvent event = new IcalEvent();
            event.setUid(uid);
            event.setInquiryId(null); // imported external events have no inquiry
            event.setPropertyId(propertyId);
            event.setDtStart(dtStart);
            event.setDtEnd(dtEnd);
            event.setIcsContent(singleIcs);

            repo.save(event);
            imported++;
        }

        return imported;
    }

    /**
     * Build a combined VCALENDAR for a property.
     */
    public String exportForProperty(String propertyId) {
        List<IcalEvent> events = repo.findAllByPropertyId(propertyId);
        DateTimeFormatter icsFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        ZoneId systemZone = ZoneId.systemDefault();

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR").append("\r\n");
        sb.append("PRODID:-//MAYGATEWAYRENTEL//BookingCalendar//EN").append("\r\n");
        sb.append("VERSION:2.0").append("\r\n");
        sb.append("CALSCALE:GREGORIAN").append("\r\n");
        sb.append("METHOD:PUBLISH").append("\r\n");

        for (IcalEvent e : events) {
            // If we have raw VEVENT inside icsContent, reuse it
            if (e.getIcsContent() != null && e.getIcsContent().toUpperCase().contains("BEGIN:VEVENT")) {
                Optional<String> veventOpt = extractFirstVevenFromIcs(e.getIcsContent());
                if (veventOpt.isPresent()) {
                    sb.append(veventOpt.get()).append("\r\n");
                    continue;
                }
            }

            sb.append("BEGIN:VEVENT").append("\r\n");
            sb.append("UID:").append(e.getUid()).append("\r\n");
            sb.append("DTSTAMP:").append(ZonedDateTime.now(ZoneOffset.UTC).format(icsFormatter)).append("\r\n");

            ZonedDateTime zStart = e.getDtStart().atZone(systemZone).withZoneSameInstant(ZoneOffset.UTC);
            ZonedDateTime zEnd = e.getDtEnd().atZone(systemZone).withZoneSameInstant(ZoneOffset.UTC);
            sb.append("DTSTART:").append(zStart.format(icsFormatter)).append("\r\n");
            sb.append("DTEND:").append(zEnd.format(icsFormatter)).append("\r\n");

            // Add summary/description/location if present inside stored raw content
            extractSingleField(e.getIcsContent(), "SUMMARY").ifPresent(s -> sb.append("SUMMARY:").append(escape(s)).append("\r\n"));
            extractSingleField(e.getIcsContent(), "DESCRIPTION").ifPresent(s -> sb.append("DESCRIPTION:").append(escape(s)).append("\r\n"));
            extractSingleField(e.getIcsContent(), "LOCATION").ifPresent(s -> sb.append("LOCATION:").append(escape(s)).append("\r\n"));

            sb.append("END:VEVENT").append("\r\n");
        }

        sb.append("END:VCALENDAR").append("\r\n");
        return sb.toString();
    }

    // ---------------- Helper methods ----------------

    private String fetchRemoteIcs(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .header("Accept", "text/calendar,application/octet-stream,application/calendar")
                .build();

        HttpResponse<byte[]> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return new String(resp.body(), StandardCharsets.UTF_8);
        } else {
            throw new IOException("Failed to fetch ICS. HTTP status: " + resp.statusCode());
        }
    }

    private static String unfoldIcs(String ics) {
        if (ics == null) return "";
        // unfold folded lines per RFC: lines that start with space or tab are continuations
        // replace CRLF + single space/tab with empty string
        return ics.replaceAll("\r\n[ \t]", "");
    }

    private static List<String> extractVevenBlocks(String icsContent) {
        List<String> result = new ArrayList<>();
        if (icsContent == null) return result;
        Pattern p = Pattern.compile("BEGIN:VEVENT(.*?)END:VEVENT", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(icsContent);
        while (m.find()) {
            String body = m.group(1).trim();
            result.add(body);
        }
        return result;
    }

    private static Optional<String> extractFirstVevenFromIcs(String icsContent) {
        if (icsContent == null) return Optional.empty();
        Pattern p = Pattern.compile("BEGIN:VEVENT(.*?)END:VEVENT", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(icsContent);
        if (m.find()) {
            return Optional.of("BEGIN:VEVENT" + m.group(1) + "END:VEVENT");
        }
        return Optional.empty();
    }

    private static Optional<String> extractSingleField(String text, String fieldName) {
        if (text == null) return Optional.empty();
        Pattern p = Pattern.compile("(?m)^" + Pattern.quote(fieldName) + "(?:;[^:]*?)?:(.*)$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return Optional.of(m.group(1).trim());
        }
        return Optional.empty();
    }

    /**
     * Basic parser that handles common ICS date formats:
     * - 20251020T120000Z
     * - 20251020T120000
     * - 20251020 (all-day)
     */
    private static LocalDateTime parseIcsDateTime(String raw) {
        if (raw == null) return null;
        raw = raw.trim();
        // Try Z-terminated UTC
        try {
            if (raw.endsWith("Z")) {
                Instant inst = Instant.parse(raw.replaceAll("(?<=\\d{8}T\\d{6})Z$", "Z")); // "yyyyMMdd'T'HHmmss'Z'" -> parse via DateTimeFormatter below
                // Instant.parse expects ISO-8601, so first convert to yyyy-MM-ddTHH:mm:ssZ if possible
            }
        } catch (Exception ignored) {}

        // Try patterns manually
        List<DateTimeFormatter> tryFormats = List.of(
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC),
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"),
                DateTimeFormatter.ofPattern("yyyyMMdd")
        );

        // Ends with Z
        if (raw.endsWith("Z")) {
            try {
                Instant inst = tryFormats.get(0).parse(raw, Instant::from);
                return LocalDateTime.ofInstant(inst, ZoneId.systemDefault());
            } catch (DateTimeParseException ignored) {}
        }

        // Try yyyyMMdd'T'HHmmss without Z
        try {
            return LocalDateTime.parse(raw, tryFormats.get(1));
        } catch (DateTimeParseException ignored) {}

        // Try date only
        try {
            java.time.LocalDate ld = java.time.LocalDate.parse(raw, tryFormats.get(2));
            return ld.atStartOfDay();
        } catch (DateTimeParseException ignored) {}

        // Last attempt: try ISO-like conversions (insert separators)
        try {
            String iso = raw;
            if (raw.matches("\\d{8}T\\d{6}Z")) {
                iso = raw.substring(0, 8) + "T" + raw.substring(9, 15) + "Z";
            }
        } catch (Exception ignored) {}

        return null;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace(",", "\\,").replace(";", "\\;");
    }
}
