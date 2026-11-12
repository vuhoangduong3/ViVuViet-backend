package com.example.Vi_vu_Viet.dto.Response.Company;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardRouteResponse {
    UUID routeId;
    String routeCode;
    String routeName;
    Long tripCount;
    Long passengerCount;
    Long revenue;
    Long seatCount;
    BigDecimal occupancyRate;
}
