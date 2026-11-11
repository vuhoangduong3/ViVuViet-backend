package com.example.Vi_vu_Viet.dto.Request.Trip;

import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTripProgressRequest {
    @NotNull
    TripStatus status;

    LocalDateTime actualAt;
}
