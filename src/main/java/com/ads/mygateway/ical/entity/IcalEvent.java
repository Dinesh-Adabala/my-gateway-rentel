package com.ads.mygateway.ical.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "ical_event")
public class IcalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uid;

    @Column(nullable = false)
    private String inquiryId;

    @Column(nullable = false)
    private String propertyId;

    @Column(nullable = false)
    private LocalDateTime dtStart;

    @Column(nullable = false)
    private LocalDateTime dtEnd;

    @Column(columnDefinition = "text")
    private String icsContent; // raw .ics content for downloading/sharing
    @Column(nullable = false)
    private String source;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // constructors, getters, setters

    public IcalEvent() {}

    public IcalEvent(String uid, String inquiryId,String propertyId, LocalDateTime dtStart, LocalDateTime dtEnd, String icsContent , String source) {
        this.uid = uid;
        this.inquiryId = inquiryId;
        this.propertyId = propertyId;
        this.dtStart = dtStart;
        this.dtEnd = dtEnd;
        this.icsContent = icsContent;
        this.source= source;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // getters & setters omitted for brevity — include them or use Lombok
    // ...
}
