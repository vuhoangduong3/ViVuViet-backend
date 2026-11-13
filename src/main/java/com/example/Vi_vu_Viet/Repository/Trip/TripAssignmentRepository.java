package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.TripAssignment;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripAssignmentRepository extends JpaRepository<TripAssignment, UUID> {

    @Modifying
    @Query("delete from TripAssignment assignment where assignment.company_id.id = :companyId " +
            "and assignment.trip_id.id = :tripId")
    int deleteByCompanyAndTrip(@Param("companyId") UUID companyId, @Param("tripId") UUID tripId);

    @EntityGraph(attributePaths = {"trip_id", "trip_id.route_id"})
    @Query("select assignment from TripAssignment assignment " +
            "where assignment.company_id.id = :companyId and assignment.trip_id.company_id.id = :companyId " +
            "and assignment.driver_user_id.id = :driverId " +
            "and assignment.assignment_status not in :excludedStatuses " +
            "order by assignment.assigned_at desc, assignment.id desc")
    Page<TripAssignment> findCompanyDriverTrips(@Param("companyId") UUID companyId,
                                               @Param("driverId") UUID driverId,
                                               @Param("excludedStatuses") List<DriverAssignmentStatus> excludedStatuses,
                                               Pageable pageable);

    @Query("select assignment.driver_user_id.id as driverId, count(assignment) as tripCount " +
            "from TripAssignment assignment " +
            "where assignment.company_id.id = :companyId and assignment.trip_id.company_id.id = :companyId " +
            "and assignment.driver_user_id.id in :driverIds " +
            "and assignment.trip_id.departure_at >= :fromAt and assignment.trip_id.departure_at <= :toAt " +
            "and assignment.assignment_status not in :excludedStatuses " +
            "and assignment.trip_id.status in :tripStatuses " +
            "group by assignment.driver_user_id.id")
    List<CompanyDriverTripCountProjection> countCompanyDriverTrips(@Param("companyId") UUID companyId,
            @Param("driverIds") List<UUID> driverIds, @Param("fromAt") LocalDateTime fromAt,
            @Param("toAt") LocalDateTime toAt,
            @Param("excludedStatuses") List<DriverAssignmentStatus> excludedStatuses,
            @Param("tripStatuses") List<TripStatus> tripStatuses);

    @EntityGraph(attributePaths = {"driver_user_id", "vehicle_id", "assigned_by"})
    @Query("select assignment from TripAssignment assignment " +
            "where assignment.company_id.id = :companyId and assignment.trip_id.id = :tripId")
    Optional<TripAssignment> findByCompanyAndTrip(@Param("companyId") UUID companyId,
                                                  @Param("tripId") UUID tripId);

    @EntityGraph(attributePaths = {"company_id", "trip_id", "trip_id.route_id",
            "trip_id.route_id.origin_area_id", "trip_id.route_id.destination_area_id", "vehicle_id"})
    @Query("select assignment from TripAssignment assignment " +
            "where assignment.driver_user_id.id = :driverId " +
            "and assignment.assignment_status not in :excludedStatuses " +
            "and (:fromAt is null or assignment.trip_id.departure_at >= :fromAt) " +
            "and (:toAt is null or assignment.trip_id.departure_at < :toAt) " +
            "and (:tripStatus is null or assignment.trip_id.status = :tripStatus) " +
            "order by assignment.trip_id.departure_at, assignment.id")
    List<TripAssignment> findDriverTrips(@Param("driverId") UUID driverId,
                                         @Param("fromAt") LocalDateTime fromAt,
                                         @Param("toAt") LocalDateTime toAt,
                                         @Param("tripStatus") TripStatus tripStatus,
                                         @Param("excludedStatuses") List<DriverAssignmentStatus> excludedStatuses);

    @EntityGraph(attributePaths = {"company_id", "trip_id", "trip_id.route_id",
            "trip_id.route_id.origin_area_id", "trip_id.route_id.destination_area_id", "vehicle_id"})
    @Query("select assignment from TripAssignment assignment " +
            "where assignment.driver_user_id.id = :driverId and assignment.trip_id.id = :tripId")
    Optional<TripAssignment> findDriverTrip(@Param("driverId") UUID driverId, @Param("tripId") UUID tripId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"company_id", "trip_id", "trip_id.route_id",
            "trip_id.route_id.origin_area_id", "trip_id.route_id.destination_area_id", "vehicle_id"})
    @Query("select assignment from TripAssignment assignment " +
            "where assignment.driver_user_id.id = :driverId and assignment.trip_id.id = :tripId")
    Optional<TripAssignment> findDriverTripForUpdate(@Param("driverId") UUID driverId,
                                                      @Param("tripId") UUID tripId);

    @EntityGraph(attributePaths = {"vehicle_id", "company_id", "trip_id"})
    @Query("select assignment from TripAssignment assignment where assignment.driver_user_id.id = :driverId " +
            "and assignment.assignment_status in :statuses " +
            "and assignment.trip_id.status in :tripStatuses " +
            "order by case when assignment.trip_id.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.IN_PROGRESS " +
            "then 0 else 1 end, assignment.trip_id.departure_at")
    List<TripAssignment> findDriverVehicleAssignments(@Param("driverId") UUID driverId,
                                                       @Param("statuses") List<DriverAssignmentStatus> statuses,
                                                       @Param("tripStatuses") List<TripStatus> tripStatuses);

    @Query("select case when count(assignment) > 0 then true else false end from TripAssignment assignment " +
            "where assignment.company_id.id = :companyId and assignment.trip_id.id = :tripId")
    boolean existsByCompanyAndTrip(@Param("companyId") UUID companyId, @Param("tripId") UUID tripId);

    @Query("select case when count(assignment) > 0 then true else false end from TripAssignment assignment " +
            "where assignment.vehicle_id.id = :vehicleId")
    boolean existsAssignmentForVehicle(@Param("vehicleId") UUID vehicleId);

    @Query("select case when count(assignment) > 0 then true else false end " +
            "from TripAssignment assignment where assignment.vehicle_id.id = :vehicleId " +
            "and assignment.trip_id.id <> :tripId " +
            "and assignment.trip_id.departure_at < :arrivalAt " +
            "and assignment.trip_id.expected_arrival_at > :departureAt " +
            "and assignment.trip_id.status not in (com.example.Vi_vu_Viet.Util.Enums.TripStatus.CANCELLED, " +
            "com.example.Vi_vu_Viet.Util.Enums.TripStatus.TERMINATED)")
    boolean hasVehicleTimeConflict(@Param("vehicleId") UUID vehicleId, @Param("tripId") UUID tripId,
                                   @Param("departureAt") LocalDateTime departureAt,
                                   @Param("arrivalAt") LocalDateTime arrivalAt);

    @Query("select case when count(assignment) > 0 then true else false end " +
            "from TripAssignment assignment where assignment.driver_user_id.id = :driverId " +
            "and assignment.trip_id.id <> :tripId " +
            "and assignment.trip_id.departure_at < :arrivalAt " +
            "and assignment.trip_id.expected_arrival_at > :departureAt " +
            "and assignment.trip_id.status not in (com.example.Vi_vu_Viet.Util.Enums.TripStatus.CANCELLED, " +
            "com.example.Vi_vu_Viet.Util.Enums.TripStatus.TERMINATED)")
    boolean hasDriverTimeConflict(@Param("driverId") UUID driverId, @Param("tripId") UUID tripId,
                                 @Param("departureAt") LocalDateTime departureAt,
                                 @Param("arrivalAt") LocalDateTime arrivalAt);
}
