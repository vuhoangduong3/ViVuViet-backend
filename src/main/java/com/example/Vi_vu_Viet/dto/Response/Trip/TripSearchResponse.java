package com.example.Vi_vu_Viet.dto.Response.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Kết quả chuyến phù hợp với khu vực đi và đến")
public class TripSearchResponse {

    TripResponse trip;

    TripStopResponse boardingStop;

    TripStopResponse alightingStop;
}
