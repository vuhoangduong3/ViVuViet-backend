package com.example.Vi_vu_Viet.dto.Request.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Danh sách điểm dừng mới thay thế toàn bộ điểm dừng hiện tại của tuyến")
public class ReplaceRouteStopsRequest {

    @NotEmpty(message = "Tuyến phải có ít nhất hai điểm dừng")
    @Size(min = 2, message = "Tuyến phải có ít nhất hai điểm dừng")
    @Schema(description = "Danh sách điểm dừng theo thứ tự tuyến", requiredMode = Schema.RequiredMode.REQUIRED)
    List<@NotNull @Valid RouteStopInput> stops;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Schema(description = "Thông tin một điểm dừng trên tuyến")
    public static class RouteStopInput {

        @NotNull(message = "Điểm đón/trả không được để trống")
        @Schema(description = "ID điểm đón/trả thuộc hãng", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID stopId;

        @NotNull(message = "Thứ tự điểm dừng không được để trống")
        @Positive(message = "Thứ tự điểm dừng phải bắt đầu từ 1")
        @Schema(description = "Thứ tự liên tục từ 1", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer sequenceNo;

        @PositiveOrZero(message = "Thời điểm đến không được âm")
        @Schema(description = "Số phút từ lúc khởi hành đến khi đến điểm này")
        Integer arrivalOffsetMinutes;

        @PositiveOrZero(message = "Thời điểm rời đi không được âm")
        @Schema(description = "Số phút từ lúc khởi hành đến khi rời điểm này")
        Integer departureOffsetMinutes;

        @NotNull(message = "Cần xác định điểm này có cho đón khách không")
        @Schema(description = "Cho phép đón khách", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean pickupAllowed;

        @NotNull(message = "Cần xác định điểm này có cho trả khách không")
        @Schema(description = "Cho phép trả khách", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean dropoffAllowed;
    }
}
