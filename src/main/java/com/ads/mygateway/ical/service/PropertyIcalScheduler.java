package com.ads.mygateway.ical.service;

import com.ads.mygateway.property.entity.Property;
import com.ads.mygateway.property.repository.PropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PropertyIcalScheduler {

    private final PropertyRepository propertyRepository;
    private final IcalImportExportService importExportService;
    private final Logger log = LoggerFactory.getLogger(PropertyIcalScheduler.class);

    public PropertyIcalScheduler(PropertyRepository propertyRepository,
                                 IcalImportExportService importExportService) {
        this.propertyRepository = propertyRepository;
        this.importExportService = importExportService;
    }

    // Run every 5 minutes (fixedRate measured from method start)
    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void importAllPropertyIcalUrls() {
        log.info("Scheduled ICS import started.");
        List<Property> properties = propertyRepository.findAll();
        for (Property p : properties) {
            List<String> urls = p.getIcalUrls();
            if (urls == null || urls.isEmpty()) continue;
            for (String url : urls) {
                if (url == null || url.isBlank()) continue;
                try {
                    int imported = importExportService.importFromUrl(p.getPropertyId(), url);
                    log.info("Imported {} events for propertyId={} from URL={}", imported, p.getPropertyId(), url);
                } catch (Exception ex) {
                    log.error("Failed to import ICS for propertyId={} url={} : {}", p.getPropertyId(), url, ex.getMessage());
                }
            }
        }
        log.info("Scheduled ICS import finished.");
    }
}

