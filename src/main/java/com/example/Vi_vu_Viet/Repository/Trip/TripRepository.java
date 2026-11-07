package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripSalesStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    @Query("select trip from Trip trip " +
            "join fetch trip.company_id company " +
            "join fetch trip.route_id route " +
            "join fetch route.origin_area_id originArea " +
            "join fetch route.destination_area_id destinationArea " +
            "where trip.company_id.id = :companyId " +
            "order by trip.departure_at desc, trip.id")
    List<Trip> findAllByCompanyId(@Param("companyId") UUID companyId);

    @Query("select trip from Trip trip " +
            "join fetch trip.company_id company " +
            "join fetch trip.route_id route " +
            "join fetch route.origin_area_id originArea " +
            "join fetch route.destination_area_id destinationArea " +
            "where trip.company_id.id = :companyId and trip.id = :tripId")
    Optional<Trip> findByIdAndCompanyId(
            @Param("companyId") UUID companyId,
            @Param("tripId") UUID tripId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select trip from Trip trip where trip.company_id.id = :companyId and trip.id = :tripId")
    Optional<Trip> findByIdAndCompanyIdForUpdate(
            @Param("companyId") UUID companyId,
            @Param("tripId") UUID tripId
    );

    @Query("select count(trip) from Trip trip " +
            "where trip.company_id.id = :companyId and lower(trip.trip_code) = lower(:tripCode)")
    long countByCompanyIdAndTripCode(
            @Param("companyId") UUID companyId,
            @Param("tripCode") String tripCode
    );

    @Query("select count(trip) from Trip trip " +
            "where trip.company_id.id = :companyId " +
            "and lower(trip.trip_code) = lower(:tripCode) and trip.id <> :tripId")
    long countByCompanyIdAndTripCodeExcludingTrip(
            @Param("companyId") UUID companyId,
            @Param("tripCode") String tripCode,
            @Param("tripId") UUID tripId
    );

    @Query("select trip from Trip trip " +
            "join fetch trip.company_id company " +
            "join fetch trip.route_id route " +
            "join fetch route.origin_area_id originArea " +
            "join fetch route.destination_area_id destinationArea " +
            "where trip.id = :tripId " +
            "and trip.status = :tripStatus " +
            "and trip.sales_status = :salesStatus " +
            "and company.status = :companyStatus")
    Optional<Trip> findPublishedTripById(
            @Param("tripId") UUID tripId,
            @Param("tripStatus") TripStatus tripStatus,
            @Param("salesStatus") TripSalesStatus salesStatus,
            @Param("companyStatus") BusCompanyStatus companyStatus
    );

    @Query("select trip, boardingStop, alightingStop from Trip trip " +
            "join TripStop boardingStop on boardingStop.trip_id.id = trip.id " +
            "join TripStop alightingStop on alightingStop.trip_id.id = trip.id " +
            "join fetch trip.company_id company " +
            "join fetch trip.route_id route " +
            "join fetch route.origin_area_id routeOriginArea " +
            "join fetch route.destination_area_id routeDestinationArea " +
            "join fetch boardingStop.area_id boardingArea " +
            "join fetch alightingStop.area_id alightingArea " +
            "where company.status = :companyStatus " +
            "and trip.status = :tripStatus " +
            "and trip.sales_status = :salesStatus " +
            "and boardingStop.area_id.id = :originAreaId " +
            "and alightingStop.area_id.id = :destinationAreaId " +
            "and boardingStop.sequence_no < alightingStop.sequence_no " +
            "and boardingStop.pickup_allowed = true " +
            "and alightingStop.dropoff_allowed = true " +
            "and boardingStop.planned_departure_at >= :departureDateStart " +
            "and boardingStop.planned_departure_at < :departureDateEnd " +
            "order by boardingStop.planned_departure_at, trip.id")
    List<Object[]> searchPublishedTrips(
            @Param("originAreaId") UUID originAreaId,
            @Param("destinationAreaId") UUID destinationAreaId,
            @Param("departureDateStart") LocalDateTime departureDateStart,
            @Param("departureDateEnd") LocalDateTime departureDateEnd,
            @Param("tripStatus") TripStatus tripStatus,
            @Param("salesStatus") TripSalesStatus salesStatus,
            @Param("companyStatus") BusCompanyStatus companyStatus
    );
}
