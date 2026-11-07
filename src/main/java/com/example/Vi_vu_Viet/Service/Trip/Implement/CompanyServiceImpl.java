package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Repository.Trip.CompanyMemberRepository;
import com.example.Vi_vu_Viet.Repository.Trip.CompanyMembershipProjection;
import com.example.Vi_vu_Viet.Service.Trip.CompanyService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberRoleType;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.MyCompanyResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    CompanyMemberRepository companyMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<MyCompanyResponse>> getMyCompanies(UUID userId) {
        List<CompanyMembershipProjection> rows = companyMemberRepository.findActiveCompaniesForUser(
                userId,
                CompanyMemberStatus.ACTIVE,
                BusCompanyStatus.ACTIVE
        );

        Map<UUID, MyCompanyResponse> companies = new LinkedHashMap<>();
        for (CompanyMembershipProjection row : rows) {
            MyCompanyResponse response = companies.computeIfAbsent(row.companyId(), id -> {
                MyCompanyResponse company = new MyCompanyResponse();
                company.setCompanyId(row.companyId());
                company.setCompanyName(row.companyName());
                company.setCompanySlug(row.companySlug());
                company.setHotline(row.hotline());
                company.setTimezone(row.timezone());
                company.setCompanyStatus(row.companyStatus());
                company.setMembershipStatus(row.membershipStatus());
                company.setJoinedAt(row.joinedAt());
                company.setRoles(new ArrayList<>());
                return company;
            });

            CompanyMemberRoleType role = row.role();
            if (role != null && !response.getRoles().contains(role)) {
                response.getRoles().add(role);
            }
        }

        BaseResponse<List<MyCompanyResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(new ArrayList<>(companies.values()));
        return response;
    }
}
