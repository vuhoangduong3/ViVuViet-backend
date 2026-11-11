package com.example.Vi_vu_Viet.dto.Response.Trip;

import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.dto.Response.Booking.TicketResponse;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DriverTripResponse {
    TripResponse trip;
    VehicleResponse vehicle;
    DriverAssignmentStatus assignmentStatus;
    LocalDateTime acceptedAt;
    String rejectionReason;
    List<TicketResponse> passengers;
}
