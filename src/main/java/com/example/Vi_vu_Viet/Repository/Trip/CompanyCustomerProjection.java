package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CompanyCustomerProjection {
    UUID getId();
    String getFullName();
    String getEmail();
    String getPhone();
    UserAccountStatus getAccountStatus();
    Long getBookingCount();
    LocalDateTime getLastBookingAt();
}
