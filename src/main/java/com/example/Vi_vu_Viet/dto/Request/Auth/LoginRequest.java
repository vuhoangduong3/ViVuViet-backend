package com.example.Vi_vu_Viet.dto.Request.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin đăng nhập")
public class LoginRequest {

    @NotBlank(message = "Email hoặc số điện thoại không được để trống")
    @Schema(description = "Email hoặc số điện thoại đã đăng ký", example = "user@example.com")
    String identifier;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Schema(description = "Mật khẩu tài khoản", example = "YourPassword123!")
    String password;
}
