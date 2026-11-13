package com.example.Vi_vu_Viet.dto.Request.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin tạo chuyến xe theo tuyến mẫu")
public class CreateTripRequest {

    @NotNull(message = "Tuyến không được để trống")
    @Schema(description = "ID tuyến đang hoạt động của hãng", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID routeId;

    @NotBlank(message = "Mã chuyến không được để trống")
    @Size(max = 50, message = "Mã chuyến tối đa 50 ký tự")
    @Schema(description = "Mã chuyến duy nhất trong hãng", example = "HN-DN-20261201-0700")
    String tripCode;

    @NotNull(message = "Thời gian khởi hành không được để trống")
    @Schema(description = "Thời gian khởi hành", example = "2026-12-01T07:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime departureAt;

    @NotNull(message = "Thời gian đến dự kiến không được để trống")
    @Schema(description = "Thời gian đến dự kiến", example = "2026-12-01T19:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime expectedArrivalAt;

    @Schema(description = "Thời điểm ngừng nhận đặt chỗ; mặc định trước giờ khởi hành 30 phút")
    LocalDateTime bookingCutoffAt;

    @PositiveOrZero(message = "Tiền công tài xế không được âm")
    @Schema(description = "Tiền công tài xế theo chuyến, đơn vị VND; null nếu chưa nhập", example = "400000")
    Long driverWage;
}
