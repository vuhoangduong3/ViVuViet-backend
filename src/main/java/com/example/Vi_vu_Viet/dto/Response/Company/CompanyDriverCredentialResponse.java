package com.example.Vi_vu_Viet.dto.Response.Company;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin đăng nhập chỉ trả về khi tạo hoặc đặt lại mật khẩu")
public class CompanyDriverCredentialResponse {
    UUID driverId;
    String fullName;
    String login;

    @ToString.Exclude
    String temporaryPassword;
}
