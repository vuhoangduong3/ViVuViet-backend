package com.example.Vi_vu_Viet.dto.Response.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Cặp token xác thực")
public class TokenResponse {

    String tokenType;

    String accessToken;

    String refreshToken;

    Long expiresIn;
}
