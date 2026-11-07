package com.example.Vi_vu_Viet.Repository.Auth;

import com.example.Vi_vu_Viet.Entity.Trip.CompanyMemberRole;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyMemberRoleRepository extends JpaRepository<CompanyMemberRole, UUID> {

    @Query("select memberRole.role, company.id " +
            "from CompanyMemberRole memberRole " +
            "join memberRole.company_id company " +
            "join memberRole.membership_id membership " +
            "where membership.user_id.id = :userId " +
            "and company.id = membership.company_id.id " +
            "and membership.status = :membershipStatus")
    List<Object[]> findActiveCompanyRoles(
            @Param("userId") UUID userId,
            @Param("membershipStatus") CompanyMemberStatus membershipStatus
    );
}
