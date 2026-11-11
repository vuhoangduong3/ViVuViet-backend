package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusCompanyRepository extends JpaRepository<BusCompany, UUID> {

    Optional<BusCompany> findBySlug(String slug);

    Optional<BusCompany> findByIdAndStatus(UUID id, BusCompanyStatus status);
}
