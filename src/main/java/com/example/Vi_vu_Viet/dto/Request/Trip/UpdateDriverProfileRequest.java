package com.example.Vi_vu_Viet.dto.Request.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Cập nhật hồ sơ tài xế của một hãng; trường null được giữ nguyên")
public class UpdateDriverProfileRequest {

    @NotNull
    UUID companyId;

    @Size(max = 50)
    @Pattern(regexp = "(?s).*\\S.*")
    String employeeCode;

    @Size(max = 50)
    @Pattern(regexp = "(?s).*\\S.*")
    String licenseNumber;

    @Size(max = 30)
    @Pattern(regexp = "(?s).*\\S.*")
    String licenseClass;

    LocalDate licenseExpiresOn;

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode == null ? null : employeeCode.trim();
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber == null ? null : licenseNumber.trim();
    }

    public void setLicenseClass(String licenseClass) {
        this.licenseClass = licenseClass == null ? null : licenseClass.trim();
    }
}
