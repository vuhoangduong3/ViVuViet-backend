package com.example.Vi_vu_Viet.dto.Response.Auth;

import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Hồ sơ của người dùng đang đăng nhập")
public class ProfileResponse {
    UUID id;
    String fullName;
    String email;
    String phone;
    UserAccountStatus accountStatus;
    LocalDateTime phoneVerifiedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
