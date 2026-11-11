package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.Vehicle;
import com.example.Vi_vu_Viet.Util.Enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    @Query("select vehicle from Vehicle vehicle where vehicle.company_id.id = :companyId " +
            "and (:status is null or vehicle.status = :status) " +
            "and (:pattern is null or lower(vehicle.plate_normalized) like :pattern escape '!' " +
            "or lower(vehicle.vehicle_model) like :pattern escape '!') " +
            "order by vehicle.plate_normalized, vehicle.id")
    List<Vehicle> searchByCompany(@Param("companyId") UUID companyId,
                                  @Param("status") VehicleStatus status,
                                  @Param("pattern") String pattern);

    @Query("select vehicle from Vehicle vehicle where vehicle.id = :vehicleId " +
            "and vehicle.company_id.id = :companyId and vehicle.status = :status")
    Optional<Vehicle> findActiveForCompany(@Param("vehicleId") UUID vehicleId,
                                           @Param("companyId") UUID companyId,
                                           @Param("status") VehicleStatus status);

    @Query("select vehicle from Vehicle vehicle where vehicle.id = :vehicleId " +
            "and vehicle.company_id.id = :companyId")
    Optional<Vehicle> findByCompany(@Param("vehicleId") UUID vehicleId,
                                    @Param("companyId") UUID companyId);

    @Query("select case when count(vehicle) > 0 then true else false end from Vehicle vehicle " +
            "where vehicle.company_id.id = :companyId and vehicle.plate_normalized = :plate")
    boolean existsByCompanyAndPlate(@Param("companyId") UUID companyId, @Param("plate") String plate);

    @Query("select case when count(vehicle) > 0 then true else false end from Vehicle vehicle " +
            "where vehicle.company_id.id = :companyId and vehicle.plate_normalized = :plate " +
            "and vehicle.id <> :vehicleId")
    boolean existsByCompanyAndPlateExcluding(@Param("companyId") UUID companyId,
                                             @Param("plate") String plate,
                                             @Param("vehicleId") UUID vehicleId);
}
