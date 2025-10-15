package com.ads.mygateway.ical.repository;

import com.ads.mygateway.ical.entity.IcalEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IcalEventRepository extends JpaRepository<IcalEvent, Long> {
    Optional<IcalEvent> findByInquiryId(String inquiryId);
    Optional<IcalEvent> findByUid(String uid);
    Optional<IcalEvent> findByUidAndPropertyId(String uid, String propertyId);
    List<IcalEvent> findAllByPropertyId(String propertyId);
    @Query("""
        SELECT DISTINCT e.propertyId 
        FROM IcalEvent e
        WHERE (e.dtStart <= :checkout AND e.dtEnd >= :checkin)
    """)
    List<String> findBookedPropertyIdsBetween(
            @Param("checkin") LocalDateTime checkin,
            @Param("checkout") LocalDateTime checkout
    );
}
