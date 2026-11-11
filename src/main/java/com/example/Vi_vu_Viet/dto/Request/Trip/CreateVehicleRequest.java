package com.example.Vi_vu_Viet.dto.Request.Trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateVehicleRequest {
    @NotBlank
    @Size(max = 20)
    String plateNumber;

    @NotBlank
    @Size(max = 120)
    String vehicleModel;

    @NotNull
    @Positive
    Integer passengerCapacity;
}
