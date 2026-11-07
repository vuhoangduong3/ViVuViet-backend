package com.example.Vi_vu_Viet.dto.Request.Trip;

import com.example.Vi_vu_Viet.Util.Enums.RouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Các trường cần cập nhật của tuyến đường; bỏ qua trường không gửi")
public class UpdateRouteRequest {

    @Size(max = 50, message = "Mã tuyến tối đa 50 ký tự")
    @Schema(description = "Mã tuyến duy nhất trong hãng")
    String code;

    @Size(max = 255, message = "Tên tuyến tối đa 255 ký tự")
    @Schema(description = "Tên tuyến")
    String name;

    @Schema(description = "ID khu vực xuất phát")
    UUID originAreaId;

    @Schema(description = "ID khu vực đích")
    UUID destinationAreaId;

    @Positive(message = "Khoảng cách phải lớn hơn 0")
    @Schema(description = "Khoảng cách theo km")
    BigDecimal distanceKm;

    @Positive(message = "Thời lượng phải lớn hơn 0")
    @Schema(description = "Thời lượng dự kiến theo phút")
    Integer durationMinutes;

    @PositiveOrZero(message = "Giá vé mặc định không được âm")
    @Schema(description = "Giá mặc định theo VND")
    Long baseFareAmount;

    @Schema(description = "Trạng thái tuyến")
    RouteStatus status;
}
