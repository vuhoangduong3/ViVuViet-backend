package com.example.Vi_vu_Viet.dto.Response.Company;

import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardTodayTripResponse {
    UUID tripId;
    String tripCode;
    String routeName;
    String originAreaName;
    String destinationAreaName;
    OffsetDateTime departureAt;
    OffsetDateTime actualDepartureAt;
    TripStatus tripStatus;
    UUID vehicleId;
    String vehiclePlate;
    UUID driverId;
    String driverName;
    DriverAssignmentStatus assignmentStatus;
    Long lateMinutes;
}
