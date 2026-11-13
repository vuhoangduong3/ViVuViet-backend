package com.example.Vi_vu_Viet.dto.Response.Trip;

import com.example.Vi_vu_Viet.Util.Enums.VehicleStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleResponse {
    UUID id;
    UUID companyId;
    String plateNumber;
    String vehicleModel;
    Integer passengerCapacity;
    LocalDate inspectionExpiresOn;
    String vehicleType;
    Integer manufacturingYear;
    String fuelType;
    String color;
    UUID seatLayoutId;
    VehicleStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
