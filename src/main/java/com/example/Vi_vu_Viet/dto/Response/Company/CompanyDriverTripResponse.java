package com.example.Vi_vu_Viet.dto.Response.Company;

import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyDriverTripResponse {
    UUID tripId;
    String tripCode;
    String routeName;
    LocalDateTime departureAt;
    LocalDateTime expectedArrivalAt;
    TripStatus status;
    DriverAssignmentStatus assignmentStatus;
    LocalDateTime assignedAt;
}
