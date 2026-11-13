package com.example.Vi_vu_Viet.dto.Request.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Lý do hủy chuyến chưa bắt đầu")
public class CancelTripRequest {

    @NotBlank(message = "Lý do hủy chuyến là bắt buộc")
    @Size(max = 500, message = "Lý do hủy chuyến tối đa 500 ký tự")
    @Schema(description = "Lý do hủy chuyến", example = "Xe gặp sự cố", requiredMode = Schema.RequiredMode.REQUIRED)
    String reason;

    @Size(max = 2000, message = "Mô tả hủy chuyến tối đa 2000 ký tự")
    @Schema(description = "Mô tả thêm về việc hủy chuyến")
    String description;

    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}
