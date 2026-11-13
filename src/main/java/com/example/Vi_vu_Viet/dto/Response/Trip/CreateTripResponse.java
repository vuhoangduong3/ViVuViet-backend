package com.example.Vi_vu_Viet.dto.Response.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTripResponse extends TripResponse {
    @Schema(description = "Tiền công tài xế theo chuyến, đơn vị VND; null nếu chưa nhập")
    Long driverWage;
}
