package com.example.Vi_vu_Viet.dto.Response.Booking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Ghế và tình trạng còn trống của chuyến")
public class TripSeatResponse {

    UUID id;
    String seatCode;
    String seatClass;
    Integer deck;
    Integer rowNo;
    Integer columnNo;
    boolean available;
}
