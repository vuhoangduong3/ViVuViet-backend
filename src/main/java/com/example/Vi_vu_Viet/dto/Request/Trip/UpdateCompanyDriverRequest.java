package com.example.Vi_vu_Viet.dto.Request.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Cập nhật tài xế; trường null được giữ nguyên, email rỗng để xóa email")
public class UpdateCompanyDriverRequest {
    @Size(min = 3, max = 160)
    @Pattern(regexp = "(?s).*\\S.*")
    String fullName;

    @Pattern(regexp = "^0\\d{9,10}$")
    String phone;

    @Email
    @Size(max = 254)
    String email;

    @Pattern(regexp = "^\\d{12}$")
    String licenseNumber;

    @Pattern(regexp = "^(B1|B2|C1|C|D1|D2|E)$")
    String licenseClass;

    @Future
    LocalDate licenseExpiresOn;

    public void setFullName(String fullName) {
        this.fullName = fullName == null ? null : fullName.trim();
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.replaceAll("[\\s()\\-]", "");
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber == null ? null : licenseNumber.replaceAll("\\s", "");
    }

    public void setLicenseClass(String licenseClass) {
        this.licenseClass = licenseClass == null ? null : licenseClass.trim();
    }
}
