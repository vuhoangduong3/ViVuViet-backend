package com.example.Vi_vu_Viet.dto.Request.Trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RejectTripRequest {
    @NotBlank(message = "Lý do từ chối là bắt buộc")
    @Size(max = 500, message = "Lý do từ chối tối đa 500 ký tự")
    String reason;

    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }
}
