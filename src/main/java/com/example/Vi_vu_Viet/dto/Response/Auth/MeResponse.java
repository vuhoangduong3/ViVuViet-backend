package com.example.Vi_vu_Viet.dto.Response.Auth;

import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin tài khoản hiện tại")
public class MeResponse {

    UUID id;

    String fullName;

    String phone;

    String email;

    UserAccountStatus accountStatus;

    LocalDateTime createdAt;

    List<String> authorities;
}
