package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.DriverProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverProfileRepository extends JpaRepository<DriverProfile, UUID> {

    @Query("select profile from DriverProfile profile where profile.company_id.id = :companyId " +
            "and profile.membership_id.id in :membershipIds")
    List<DriverProfile> findAllByCompanyAndMemberships(@Param("companyId") UUID companyId,
                                                     @Param("membershipIds") List<UUID> membershipIds);

    @Query("select profile from DriverProfile profile where profile.company_id.id = :companyId " +
            "and profile.membership_id.company_id.id = :companyId " +
            "and profile.membership_id.user_id.id = :userId")
    Optional<DriverProfile> findByCompanyAndUser(@Param("companyId") UUID companyId,
                                                @Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"company_id", "membership_id", "membership_id.user_id"})
    @Query("select profile from DriverProfile profile where profile.company_id.id = :companyId " +
            "and profile.membership_id.id = :membershipId")
    Optional<DriverProfile> findByCompanyAndMembership(@Param("companyId") UUID companyId,
                                                        @Param("membershipId") UUID membershipId);

    @Query("select case when count(profile) > 0 then true else false end from DriverProfile profile " +
            "where profile.company_id.id = :companyId and lower(profile.license_number) = lower(:licenseNumber) " +
            "and (:profileId is null or profile.id <> :profileId)")
    boolean existsLicenseForAnotherProfile(@Param("companyId") UUID companyId,
                                           @Param("licenseNumber") String licenseNumber,
                                           @Param("profileId") UUID profileId);
}
