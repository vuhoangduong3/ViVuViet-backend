package com.example.Vi_vu_Viet.dto.Request.Trip;

import com.example.Vi_vu_Viet.Util.Enums.PassengerTripStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePassengerTripStatusRequest {
    @NotNull
    PassengerTripStatus status;
}
