package com.example.Vi_vu_Viet.dto.Response.Company;

import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberRoleType;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Hãng xe mà tài khoản hiện tại đang tham gia")
public class MyCompanyResponse {

    UUID companyId;

    String companyName;

    String companySlug;

    String hotline;

    String timezone;

    BusCompanyStatus companyStatus;

    CompanyMemberStatus membershipStatus;

    LocalDateTime joinedAt;

    List<CompanyMemberRoleType> roles;
}
