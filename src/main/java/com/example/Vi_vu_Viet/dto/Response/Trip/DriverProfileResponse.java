package com.example.Vi_vu_Viet.dto.Response.Trip;

import com.example.Vi_vu_Viet.Util.Enums.DriverProfileStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DriverProfileResponse {
    UUID id;
    UUID companyId;
    String companyName;
    UUID userId;
    String fullName;
    String phone;
    String email;
    LocalDateTime joinedAt;
    String employeeCode;
    String licenseNumber;
    String licenseClass;
    LocalDate licenseExpiresOn;
    String licenseStatus;
    DriverProfileStatus status;
}
