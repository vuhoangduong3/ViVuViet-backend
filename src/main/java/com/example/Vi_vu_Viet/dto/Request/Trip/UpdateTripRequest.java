package com.example.Vi_vu_Viet.dto.Request.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Các trường cần cập nhật của chuyến; chỉ chuyến ở trạng thái DRAFT được sửa")
public class UpdateTripRequest {

    @Size(max = 50, message = "Mã chuyến tối đa 50 ký tự")
    @Schema(description = "Mã chuyến duy nhất trong hãng")
    String tripCode;

    @Schema(description = "Thời gian khởi hành mới")
    LocalDateTime departureAt;

    @Schema(description = "Thời gian đến dự kiến mới")
    LocalDateTime expectedArrivalAt;

    @Schema(description = "Thời điểm ngừng nhận đặt chỗ mới")
    LocalDateTime bookingCutoffAt;
}
