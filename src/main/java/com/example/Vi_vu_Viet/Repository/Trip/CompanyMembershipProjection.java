package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberRoleType;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyMembershipProjection(
        UUID companyId,
        String companyName,
        String companySlug,
        String hotline,
        String timezone,
        BusCompanyStatus companyStatus,
        CompanyMemberStatus membershipStatus,
        LocalDateTime joinedAt,
        CompanyMemberRoleType role
) {
}
