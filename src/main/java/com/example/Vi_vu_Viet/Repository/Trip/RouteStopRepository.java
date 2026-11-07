package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {

    @Query("select routeStop from RouteStop routeStop " +
            "join fetch routeStop.stop_id stop " +
            "join fetch stop.area_id area " +
            "where routeStop.company_id.id = :companyId and routeStop.route_id.id in :routeIds " +
            "order by routeStop.route_id.id, routeStop.sequence_no")
    List<RouteStop> findAllByCompanyIdAndRouteIds(
            @Param("companyId") UUID companyId,
            @Param("routeIds") Collection<UUID> routeIds
    );

    @Query("select routeStop from RouteStop routeStop " +
            "join fetch routeStop.stop_id stop " +
            "join fetch stop.area_id area " +
            "where routeStop.company_id.id = :companyId and routeStop.route_id.id = :routeId " +
            "order by routeStop.sequence_no")
    List<RouteStop> findAllByCompanyIdAndRouteId(
            @Param("companyId") UUID companyId,
            @Param("routeId") UUID routeId
    );
}
