package com.example.Vi_vu_Viet.dto.Request.Trip;

import com.example.Vi_vu_Viet.Util.Enums.VehicleStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateVehicleRequest {
    @Size(max = 20)
    String plateNumber;

    @Size(max = 120)
    String vehicleModel;

    @Positive
    Integer passengerCapacity;

    VehicleStatus status;
}
