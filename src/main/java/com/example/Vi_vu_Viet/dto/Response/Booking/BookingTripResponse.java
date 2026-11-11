package com.example.Vi_vu_Viet.dto.Response.Booking;

import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin chuyến và hãng để hiển thị vé điện tử")
public class BookingTripResponse {
    UUID id;
    String tripCode;
    TripStatus status;
    LocalDateTime departureAt;
    LocalDateTime expectedArrivalAt;
    UUID companyId;
    String companyName;
    String companyHotline;
    String timezone;
    UUID routeId;
    String routeCode;
    String routeName;
    UUID originAreaId;
    String originAreaName;
    UUID destinationAreaId;
    String destinationAreaName;
}
