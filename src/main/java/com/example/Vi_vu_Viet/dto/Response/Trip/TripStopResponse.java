package com.example.Vi_vu_Viet.dto.Response.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Điểm dừng đã được chụp vào lịch trình chuyến")
public class TripStopResponse {

    UUID id;

    UUID stopId;

    UUID areaId;

    String areaCode;

    String areaName;

    Integer sequenceNo;

    String name;

    String address;

    LocalDateTime plannedArrivalAt;

    LocalDateTime plannedDepartureAt;

    Boolean pickupAllowed;

    Boolean dropoffAllowed;
}
