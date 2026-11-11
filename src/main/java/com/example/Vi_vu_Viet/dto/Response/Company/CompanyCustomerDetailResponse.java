package com.example.Vi_vu_Viet.dto.Response.Company;

import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyCustomerDetailResponse {
    UUID id;
    String fullName;
    String email;
    String phone;
    UserAccountStatus accountStatus;
    LocalDateTime phoneVerifiedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<CompanyCustomerBookingResponse> bookings;
}
