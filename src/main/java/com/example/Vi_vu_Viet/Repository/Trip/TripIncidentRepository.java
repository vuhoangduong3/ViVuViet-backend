package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.TripIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.Vi_vu_Viet.Util.Enums.IncidentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripIncidentRepository extends JpaRepository<TripIncident, UUID> {
    @EntityGraph(attributePaths = {"trip_id", "reported_by"})
    @Query("select incident from TripIncident incident " +
            "where incident.company_id.id = :companyId and incident.trip_id.company_id.id = :companyId " +
            "and incident.status in :statuses order by incident.opened_at desc, incident.id")
    List<TripIncident> findDashboardIncidents(@Param("companyId") UUID companyId,
                                             @Param("statuses") List<IncidentStatus> statuses,
                                             Pageable pageable);
}
