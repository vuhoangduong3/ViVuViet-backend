package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {

    @Query("select route from Route route " +
            "join fetch route.origin_area_id originArea " +
            "join fetch route.destination_area_id destinationArea " +
            "where route.company_id.id = :companyId " +
            "order by route.name, route.id")
    List<Route> findAllByCompanyId(@Param("companyId") UUID companyId);

    @Query("select route from Route route " +
            "join fetch route.origin_area_id originArea " +
            "join fetch route.destination_area_id destinationArea " +
            "where route.company_id.id = :companyId and route.id = :routeId")
    Optional<Route> findByIdAndCompanyId(
            @Param("companyId") UUID companyId,
            @Param("routeId") UUID routeId
    );

    @Query("select count(route) from Route route " +
            "where route.company_id.id = :companyId and lower(route.code) = lower(:code)")
    long countByCompanyIdAndCode(
            @Param("companyId") UUID companyId,
            @Param("code") String code
    );

    @Query("select count(route) from Route route " +
            "where route.company_id.id = :companyId and lower(route.code) = lower(:code) " +
            "and route.id <> :routeId")
    long countByCompanyIdAndCodeExcludingRoute(
            @Param("companyId") UUID companyId,
            @Param("code") String code,
            @Param("routeId") UUID routeId
    );
}
