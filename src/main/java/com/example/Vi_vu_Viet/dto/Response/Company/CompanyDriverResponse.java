package com.example.Vi_vu_Viet.dto.Response.Company;

import com.example.Vi_vu_Viet.Util.Enums.CompanyDriverStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyDriverResponse {
    @Schema(description = "ID tài khoản, cũng là driverId dùng khi phân công chuyến")
    UUID driverId;
    UUID companyId;
    UUID profileId;
    String fullName;
    String phone;
    String email;
    String licenseNumber;
    String licenseClass;
    LocalDate licenseExpiresOn;
    CompanyDriverStatus status;
    String statusNote;
    long tripCount30Days;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
