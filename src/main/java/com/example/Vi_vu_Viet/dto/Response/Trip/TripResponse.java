package com.example.Vi_vu_Viet.dto.Response.Trip;

import com.example.Vi_vu_Viet.Util.Enums.TripSalesStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin chuyến xe")
public class TripResponse {

    UUID id;

    UUID companyId;

    String companyName;

    UUID routeId;

    String routeCode;

    String routeName;

    UUID originAreaId;

    String originAreaName;

    UUID destinationAreaId;

    String destinationAreaName;

    String tripCode;

    LocalDateTime departureAt;

    LocalDateTime expectedArrivalAt;

    LocalDateTime actualDepartureAt;

    LocalDateTime actualArrivalAt;

    LocalDateTime bookingCutoffAt;

    Long baseFareAmount;

    TripStatus status;

    TripSalesStatus salesStatus;

    List<TripStopResponse> stops;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
