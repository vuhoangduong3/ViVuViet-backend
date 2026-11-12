package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.CompanyMember;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberRoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {

    @EntityGraph(attributePaths = {"company_id", "user_id"})
    @Query("select membership from CompanyMember membership " +
            "join membership.user_id account " +
            "left join DriverProfile profile on profile.membership_id.id = membership.id " +
            "and profile.company_id.id = :companyId " +
            "where membership.company_id.id = :companyId " +
            "and exists (select memberRole.id from CompanyMemberRole memberRole " +
            "where memberRole.membership_id.id = membership.id and memberRole.company_id.id = :companyId " +
            "and memberRole.role = com.example.Vi_vu_Viet.Util.Enums.CompanyMemberRoleType.DRIVER) " +
            "and (:pattern is null or lower(account.full_name) like :pattern escape '!' " +
            "or account.phone like :phonePattern escape '!' or lower(account.email) like :pattern escape '!') " +
            "and (:active is null or (:active = true " +
            "and membership.status = com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus.ACTIVE " +
            "and account.account_status = com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus.ACTIVE " +
            "and (profile.id is null or profile.status = com.example.Vi_vu_Viet.Util.Enums.DriverProfileStatus.ACTIVE)) " +
            "or (:active = false and (membership.status <> com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus.ACTIVE " +
            "or account.account_status <> com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus.ACTIVE " +
            "or profile.status <> com.example.Vi_vu_Viet.Util.Enums.DriverProfileStatus.ACTIVE))) " +
            "order by membership.created_at desc, membership.id")
    Page<CompanyMember> searchCompanyDrivers(@Param("companyId") UUID companyId,
                                            @Param("pattern") String pattern,
                                            @Param("phonePattern") String phonePattern,
                                            @Param("active") Boolean active,
                                            Pageable pageable);

    @EntityGraph(attributePaths = {"company_id", "user_id"})
    @Query("select membership from CompanyMember membership where membership.company_id.id = :companyId " +
            "and membership.user_id.id = :driverId " +
            "and exists (select memberRole.id from CompanyMemberRole memberRole " +
            "where memberRole.membership_id.id = membership.id and memberRole.company_id.id = :companyId " +
            "and memberRole.role = com.example.Vi_vu_Viet.Util.Enums.CompanyMemberRoleType.DRIVER)")
    Optional<CompanyMember> findCompanyDriver(@Param("companyId") UUID companyId,
                                             @Param("driverId") UUID driverId);

    // Identity and password belong to the shared account, not to a single company.
    @Query("select case when count(membership) > 0 then true else false end from CompanyMember membership " +
            "where membership.user_id.id = :userId and (membership.company_id.id <> :companyId " +
            "or exists (select memberRole.id from CompanyMemberRole memberRole " +
            "where memberRole.membership_id.id = membership.id " +
            "and memberRole.role = com.example.Vi_vu_Viet.Util.Enums.CompanyMemberRoleType.COMPANY_ADMIN))")
    boolean hasSharedOrAdminAccount(@Param("companyId") UUID companyId, @Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"company_id", "user_id"})
    @Query("select distinct membership from CompanyMember membership " +
            "join CompanyMemberRole memberRole on memberRole.membership_id.id = membership.id " +
            "where membership.user_id.id = :userId and membership.status = :membershipStatus " +
            "and memberRole.role = :role and memberRole.company_id.id = membership.company_id.id")
    List<CompanyMember> findActiveMembershipsByRole(@Param("userId") UUID userId,
                                                    @Param("membershipStatus") CompanyMemberStatus membershipStatus,
                                                    @Param("role") CompanyMemberRoleType role);

    @Query("select case when count(memberRole) > 0 then true else false end " +
            "from CompanyMemberRole memberRole " +
            "join memberRole.membership_id membership " +
            "where memberRole.company_id.id = :companyId and membership.user_id.id = :userId " +
            "and membership.company_id.id = :companyId " +
            "and membership.status = :membershipStatus and memberRole.role = :roleType")
    boolean hasCompanyRole(@Param("companyId") UUID companyId,
                           @Param("userId") UUID userId,
                           @Param("membershipStatus") CompanyMemberStatus membershipStatus,
                           @Param("roleType") CompanyMemberRoleType roleType);

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
