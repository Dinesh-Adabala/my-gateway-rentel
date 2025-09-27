package com.ads.mygateway.property.repository;

import com.ads.mygateway.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, String> {

    // ✅ LIKE query for propertyName
    @Query("SELECT p FROM Property p WHERE LOWER(p.propertyName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Property> findByPropertyName(@Param("name") String name);

    // ✅ LIKE query for location
    @Query("SELECT p FROM Property p WHERE LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Property> findByLocation(@Param("location") String location);

    // ✅ Exact delete
    void deleteByPropertyName(String propertyName);

    List<Property> findByEmailId(String emailId);
}