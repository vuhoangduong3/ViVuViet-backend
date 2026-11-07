package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.CompanyMember;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {

    @Query("select new com.example.Vi_vu_Viet.Repository.Trip.CompanyMembershipProjection(" +
            "company.id, company.name, company.slug, company.hotline, company.timezone, " +
            "company.status, membership.status, membership.joined_at, memberRole.role) " +
            "from CompanyMember membership " +
            "join membership.company_id company " +
            "left join CompanyMemberRole memberRole " +
            "on memberRole.membership_id.id = membership.id " +
            "and memberRole.company_id.id = company.id " +
            "where membership.user_id.id = :userId " +
            "and membership.status = :membershipStatus " +
            "and company.status = :companyStatus " +
            "order by company.name, memberRole.role")
    List<CompanyMembershipProjection> findActiveCompaniesForUser(
            @Param("userId") UUID userId,
            @Param("membershipStatus") CompanyMemberStatus membershipStatus,
            @Param("companyStatus") BusCompanyStatus companyStatus
    );
}
