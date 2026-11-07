package com.example.Vi_vu_Viet.dto.Request.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin tạo điểm đón/trả cho hãng xe")
public class CreateStopRequest {

    @NotNull(message = "Địa điểm không được để trống")
    @Schema(description = "ID địa điểm trong danh mục areas", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID areaId;

    @NotBlank(message = "Tên điểm đón/trả không được để trống")
    @Size(max = 255, message = "Tên điểm đón/trả tối đa 255 ký tự")
    String name;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    String address;

    @Size(max = 255, message = "Hướng dẫn đón khách tối đa 255 ký tự")
    String pickupInstructions;
}
