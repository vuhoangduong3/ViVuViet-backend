package com.example.Vi_vu_Viet.dto.Response.Company;

import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardTripPointResponse {
    LocalDate date;
    Long totalTrips;
    Map<TripStatus, Long> byStatus;
}
