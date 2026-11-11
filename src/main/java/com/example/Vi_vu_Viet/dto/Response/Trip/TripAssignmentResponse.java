package com.example.Vi_vu_Viet.dto.Response.Trip;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripAssignmentResponse {
    UUID tripId;
    UUID vehicleId;
    UUID driverId;
    String driverName;
    String driverPhone;
    LocalDateTime assignedAt;
}
