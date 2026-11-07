package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.Stop;
import com.example.Vi_vu_Viet.Util.Enums.StopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface StopRepository extends JpaRepository<Stop, UUID> {

    @Query("select stop from Stop stop " +
            "join fetch stop.area_id area " +
            "where stop.company_id.id = :companyId " +
            "order by stop.name, stop.id")
    List<Stop> findAllByCompanyId(@Param("companyId") UUID companyId);

    @Query("select stop from Stop stop " +
            "join fetch stop.area_id area " +
            "where stop.company_id.id = :companyId " +
            "and stop.id in :stopIds " +
            "and stop.status = :status")
    List<Stop> findAllByCompanyIdAndIdsAndStatus(
            @Param("companyId") UUID companyId,
            @Param("stopIds") Collection<UUID> stopIds,
            @Param("status") StopStatus status
    );
}
