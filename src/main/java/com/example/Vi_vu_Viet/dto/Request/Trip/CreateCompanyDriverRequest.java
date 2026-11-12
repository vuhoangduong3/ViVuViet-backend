package com.example.Vi_vu_Viet.dto.Request.Trip;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCompanyDriverRequest {
    @NotBlank
    @Size(min = 3, max = 160)
    String fullName;

    @NotBlank
    @Pattern(regexp = "^0\\d{9,10}$", message = "Phone must start with 0 and contain 10 to 11 digits")
    String phone;

    @Email
    @Size(max = 254)
    String email;

    @NotBlank
    @Pattern(regexp = "^\\d{12}$", message = "License number must contain exactly 12 digits")
    String licenseNumber;

    @NotBlank
    @Pattern(regexp = "^(B1|B2|C1|C|D1|D2|E)$")
    String licenseClass;

    @NotNull
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
