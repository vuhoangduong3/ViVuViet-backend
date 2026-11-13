package com.example.Vi_vu_Viet.dto.Request.Trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;

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
    LocalDate inspectionExpiresOn;
    @Size(max = 120) String vehicleType;
    @jakarta.validation.constraints.Min(1886) @jakarta.validation.constraints.Max(9999) Integer manufacturingYear;
    @Size(max = 80) String fuelType;
    @Size(max = 80) String color;
}
