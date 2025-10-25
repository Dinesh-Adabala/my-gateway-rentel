package com.ads.mygateway.ical.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookedDateDTO {
    private String startDate;
    private String endDate;
}
