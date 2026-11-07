package com.example.Vi_vu_Viet.Security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("companyAuthorization")
public class CompanyAuthorization {

    public boolean hasCompanyRole(Authentication authentication, UUID companyId, String role) {
        if (authentication == null || !authentication.isAuthenticated() || companyId == null || role == null) {
            return false;
        }

        String requiredAuthority = role + ":" + companyId;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(requiredAuthority));
    }
}
