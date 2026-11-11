package com.example.Vi_vu_Viet.dto.Response.Company;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardDriverResponse {
    UUID driverId;
    String driverName;
    Long assignedTrips;
    Long completedTrips;
    Long inProgressTrips;
    Long scheduledTrips;
    Long cancelledTrips;
}
