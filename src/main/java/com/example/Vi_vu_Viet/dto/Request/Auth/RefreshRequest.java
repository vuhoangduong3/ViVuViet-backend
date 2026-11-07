package com.example.Vi_vu_Viet.dto.Request.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Yêu cầu làm mới phiên đăng nhập")
public class RefreshRequest {

    @NotBlank(message = "Refresh token không được để trống")
    @Schema(description = "Refresh token hiện tại")
    String refreshToken;
}
