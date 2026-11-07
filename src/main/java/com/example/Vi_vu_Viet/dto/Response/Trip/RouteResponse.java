package com.example.Vi_vu_Viet.dto.Response.Trip;

import com.example.Vi_vu_Viet.Util.Enums.RouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin tuyến đường của hãng")
public class RouteResponse {

    UUID id;

    UUID companyId;

    String code;

    String name;

    UUID originAreaId;

    String originAreaCode;

    String originAreaName;

    UUID destinationAreaId;

    String destinationAreaCode;

    String destinationAreaName;

    BigDecimal distanceKm;

    Integer durationMinutes;

    Long baseFareAmount;

    RouteStatus status;

    List<RouteStopResponse> stops;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
