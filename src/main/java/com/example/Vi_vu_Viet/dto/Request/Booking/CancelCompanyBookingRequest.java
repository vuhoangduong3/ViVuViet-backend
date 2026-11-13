package com.example.Vi_vu_Viet.dto.Request.Booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Lý do admin hãng hủy đơn đặt vé")
public class CancelCompanyBookingRequest {

    @NotBlank(message = "Lý do hủy đơn là bắt buộc")
    @Size(max = 500, message = "Lý do hủy đơn tối đa 500 ký tự")
    @Schema(example = "Khách yêu cầu hủy vé", requiredMode = Schema.RequiredMode.REQUIRED)
    String reason;

    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }
}
