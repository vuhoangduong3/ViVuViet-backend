package com.example.Vi_vu_Viet.dto.Response.Company;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardDriverResponse {
    UUID driverId;
    String driverName;
    Long assignedTrips;
    Long completedTrips;
    Long inProgressTrips;
    Long scheduledTrips;
    Long cancelledTrips;
    Long terminatedTrips;
    Long revenue;
    @Schema(description = "Chưa có dữ liệu đánh giá tài xế; trả null", nullable = true)
    BigDecimal rating;
}
