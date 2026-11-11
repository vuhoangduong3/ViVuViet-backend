package com.example.Vi_vu_Viet.dto.Response.Booking;

import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.Util.Enums.PassengerTripStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketResponse {

    UUID id;
    String ticketCode;
    String passengerName;
    String passengerPhone;
    String pickupAddress;
    String dropoffAddress;
    UUID seatId;
    String seatCode;
    String seatClass;
    Long fareAmount;
    TicketStatus status;
    PassengerTripStatus passengerTripStatus;
    LocalDateTime createdAt;
}
