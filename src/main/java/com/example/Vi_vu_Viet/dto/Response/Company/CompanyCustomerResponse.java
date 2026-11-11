package com.example.Vi_vu_Viet.dto.Response.Company;

import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyCustomerResponse {
    UUID id;
    String fullName;
    String email;
    String phone;
    UserAccountStatus accountStatus;
    Long bookingCount;
    LocalDateTime lastBookingAt;
}
