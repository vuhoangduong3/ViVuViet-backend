package com.example.Vi_vu_Viet.dto.Response.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Điểm dừng mẫu thuộc tuyến")
public class RouteStopResponse {

    UUID id;

    UUID stopId;

    String stopName;

    String address;

    UUID areaId;

    String areaName;

    Integer sequenceNo;

    Integer arrivalOffsetMinutes;

    Integer departureOffsetMinutes;

    Boolean pickupAllowed;

    Boolean dropoffAllowed;
}
