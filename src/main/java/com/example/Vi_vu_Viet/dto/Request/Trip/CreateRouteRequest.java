package com.example.Vi_vu_Viet.dto.Request.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin tạo tuyến đường cho hãng xe")
public class CreateRouteRequest {

    @NotBlank(message = "Mã tuyến không được để trống")
    @Size(max = 50, message = "Mã tuyến tối đa 50 ký tự")
    @Schema(description = "Mã tuyến duy nhất trong hãng", example = "HN-DN")
    String code;

    @NotBlank(message = "Tên tuyến không được để trống")
    @Size(max = 255, message = "Tên tuyến tối đa 255 ký tự")
    @Schema(description = "Tên tuyến", example = "Hà Nội - Đà Nẵng")
    String name;

    @NotNull(message = "Khu vực đi không được để trống")
    @Schema(description = "ID khu vực xuất phát", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID originAreaId;

    @NotNull(message = "Khu vực đến không được để trống")
    @Schema(description = "ID khu vực đích", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID destinationAreaId;

    @NotNull(message = "Khoảng cách không được để trống")
    @Positive(message = "Khoảng cách phải lớn hơn 0")
    @Schema(description = "Khoảng cách theo km", example = "760.5")
    BigDecimal distanceKm;

    @NotNull(message = "Thời lượng không được để trống")
    @Positive(message = "Thời lượng phải lớn hơn 0")
    @Schema(description = "Thời lượng dự kiến theo phút", example = "900")
    Integer durationMinutes;

    @NotNull(message = "Giá vé mặc định không được để trống")
    @PositiveOrZero(message = "Giá vé mặc định không được âm")
    @Schema(description = "Giá mặc định theo VND", example = "450000")
    Long baseFareAmount;
}
