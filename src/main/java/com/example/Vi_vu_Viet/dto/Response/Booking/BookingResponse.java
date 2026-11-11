package com.example.Vi_vu_Viet.dto.Response.Booking;

import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {

    UUID id;
    UUID tripId;
    BookingTripResponse trip;
    String bookingCode;
    String contactName;
    String contactPhone;
    String contactEmail;
    BookingStatus status;
    LocalDateTime holdExpiresAt;
    Long quotedTotalAmount;
    String currency;
    LocalDateTime confirmedAt;
    LocalDateTime cancelledAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<TicketResponse> tickets;
}
