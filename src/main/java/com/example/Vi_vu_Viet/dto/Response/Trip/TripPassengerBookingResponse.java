package com.example.Vi_vu_Viet.dto.Response.Trip;

import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.dto.Response.Booking.TicketResponse;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripPassengerBookingResponse {
    UUID id;
    String bookingCode;
    BookingStatus status;
    String contactName;
    String contactPhone;
    String contactEmail;
    LocalDateTime createdAt;
    List<TicketResponse> passengers;
}
