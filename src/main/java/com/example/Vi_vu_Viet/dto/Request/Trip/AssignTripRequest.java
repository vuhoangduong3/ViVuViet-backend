package com.example.Vi_vu_Viet.dto.Request.Trip;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignTripRequest {
    @NotNull UUID vehicleId;
    @NotNull UUID driverId;
}
