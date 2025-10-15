package com.ads.mygateway.inquiry.repository;

import com.ads.mygateway.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Optional<Inquiry> findByEnquiryId(String enquiryId);
    List<Inquiry> findByPropertyIdIn(List<String> propertyIds);
}
