package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DashboardRepository extends JpaRepository<Trip, UUID> {

    @Query("select trip from Trip trip join fetch trip.route_id route " +
            "where trip.company_id.id = :companyId and trip.departure_at >= :fromAt " +
            "and trip.departure_at < :toAt and (:routeId is null or route.id = :routeId) " +
            "and trip.status <> com.example.Vi_vu_Viet.Util.Enums.TripStatus.DRAFT " +
            "order by trip.departure_at desc, trip.id desc")
    List<Trip> findReportTrips(@Param("companyId") UUID companyId, @Param("fromAt") LocalDateTime fromAt,
                               @Param("toAt") LocalDateTime toAt, @Param("routeId") UUID routeId);

    @Query("select ticket from Ticket ticket join fetch ticket.trip_id trip " +
            "join fetch ticket.booking_id booking where ticket.company_id.id = :companyId " +
            "and trip.company_id.id = :companyId and trip.departure_at >= :fromAt " +
            "and trip.departure_at < :toAt and (:routeId is null or trip.route_id.id = :routeId) " +
            "and trip.status <> com.example.Vi_vu_Viet.Util.Enums.TripStatus.DRAFT")
    List<Ticket> findReportTickets(@Param("companyId") UUID companyId, @Param("fromAt") LocalDateTime fromAt,
                                   @Param("toAt") LocalDateTime toAt, @Param("routeId") UUID routeId);

    @Query("select count(ticket), coalesce(sum(ticket.fare_amount), 0) from Ticket ticket " +
            "where ticket.company_id.id = :companyId and ticket.status = :status and ticket.trip_id.route_id.id = :routeId " +
            "and ticket.trip_id.departure_at >= :fromAt and ticket.trip_id.departure_at < :toAt")
    List<Object[]> getRouteRevenueSummary(@Param("companyId") UUID companyId, @Param("fromAt") LocalDateTime fromAt,
            @Param("toAt") LocalDateTime toAt, @Param("status") TicketStatus status, @Param("routeId") UUID routeId);

    @Query("select extract(year from trip.departure_at), extract(month from trip.departure_at), " +
            "extract(day from trip.departure_at), count(ticket), coalesce(sum(ticket.fare_amount), 0) " +
            "from Ticket ticket join ticket.trip_id trip where ticket.company_id.id = :companyId and ticket.status = :status " +
            "and trip.route_id.id = :routeId and trip.departure_at >= :fromAt and trip.departure_at < :toAt " +
            "group by extract(year from trip.departure_at), extract(month from trip.departure_at), extract(day from trip.departure_at)")
    List<Object[]> getRouteRevenueByDepartureDate(@Param("companyId") UUID companyId, @Param("fromAt") LocalDateTime fromAt,
            @Param("toAt") LocalDateTime toAt, @Param("status") TicketStatus status, @Param("routeId") UUID routeId);

    @Query("select count(trip), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.SCHEDULED then 1 else 0 end), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.IN_PROGRESS then 1 else 0 end), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.COMPLETED then 1 else 0 end), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.CANCELLED then 1 else 0 end), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.DRAFT then 1 else 0 end), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.TERMINATED then 1 else 0 end) " +
            "from Trip trip where trip.company_id.id = :companyId " +
            "and trip.departure_at >= :fromAt and trip.departure_at < :toAt")
    List<Object[]> getTripSummary(@Param("companyId") UUID companyId,
                                  @Param("fromAt") LocalDateTime fromAt,
                                  @Param("toAt") LocalDateTime toAt);

    @Query("select count(ticket), coalesce(sum(ticket.fare_amount), 0) from Ticket ticket " +
            "where ticket.company_id.id = :companyId " +
            "and ticket.status = :status " +
            "and ticket.trip_id.departure_at >= :fromAt and ticket.trip_id.departure_at < :toAt")
    List<Object[]> getTicketValueSummary(@Param("companyId") UUID companyId,
                                         @Param("fromAt") LocalDateTime fromAt,
                                         @Param("toAt") LocalDateTime toAt,
                                         @Param("status") TicketStatus status);

    @Query("select extract(year from trip.departure_at), extract(month from trip.departure_at), " +
            "extract(day from trip.departure_at), trip.status, count(trip) from Trip trip " +
            "where trip.company_id.id = :companyId " +
            "and trip.departure_at >= :fromAt and trip.departure_at < :toAt " +
            "group by extract(year from trip.departure_at), extract(month from trip.departure_at), " +
            "extract(day from trip.departure_at), trip.status " +
            "order by extract(year from trip.departure_at), extract(month from trip.departure_at), " +
            "extract(day from trip.departure_at)")
    List<Object[]> getTripsByDepartureDate(@Param("companyId") UUID companyId,
                                           @Param("fromAt") LocalDateTime fromAt,
                                           @Param("toAt") LocalDateTime toAt);

    @Query("select driver.id, driver.full_name, count(trip.id), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.COMPLETED then 1 else 0 end), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.IN_PROGRESS then 1 else 0 end), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.SCHEDULED then 1 else 0 end), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.CANCELLED then 1 else 0 end), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.TERMINATED then 1 else 0 end) " +
            "from TripAssignment assignment join assignment.trip_id trip " +
            "join assignment.driver_user_id driver " +
            "where assignment.company_id.id = :companyId and trip.company_id.id = :companyId " +
            "and assignment.assignment_status not in " +
            "(com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus.REJECTED, " +
            "com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus.REPLACED) " +
            "and trip.status <> com.example.Vi_vu_Viet.Util.Enums.TripStatus.DRAFT " +
            "and trip.departure_at >= :fromAt and trip.departure_at < :toAt " +
            "group by driver.id, driver.full_name order by count(trip.id) desc, driver.full_name, driver.id")
    List<Object[]> getTopDrivers(@Param("companyId") UUID companyId,
                                 @Param("fromAt") LocalDateTime fromAt,
                                 @Param("toAt") LocalDateTime toAt,
                                 Pageable pageable);

    @Query("select assignment.driver_user_id.id, coalesce(sum(ticket.fare_amount), 0) " +
            "from TripAssignment assignment join assignment.trip_id trip " +
            "join Ticket ticket on ticket.trip_id = trip " +
            "where assignment.company_id.id = :companyId and trip.company_id.id = :companyId " +
            "and ticket.company_id.id = :companyId and ticket.status = :ticketStatus " +
            "and assignment.driver_user_id.id in :driverIds " +
            "and assignment.assignment_status not in :excludedStatuses " +
            "and trip.status <> com.example.Vi_vu_Viet.Util.Enums.TripStatus.DRAFT " +
            "and trip.departure_at >= :fromAt and trip.departure_at < :toAt " +
            "group by assignment.driver_user_id.id")
    List<Object[]> getDriverRevenue(@Param("companyId") UUID companyId,
                                    @Param("driverIds") List<UUID> driverIds,
                                    @Param("fromAt") LocalDateTime fromAt,
                                    @Param("toAt") LocalDateTime toAt,
                                    @Param("ticketStatus") TicketStatus ticketStatus,
                                    @Param("excludedStatuses") List<DriverAssignmentStatus> excludedStatuses);

    @Query("select trip.id, trip.trip_code, route.name, origin.name, destination.name, " +
            "trip.departure_at, trip.actual_departure_at, trip.status, " +
            "vehicle.id, vehicle.plate_normalized, driver.id, driver.full_name, assignment.assignment_status " +
            "from Trip trip join trip.route_id route " +
            "join route.origin_area_id origin join route.destination_area_id destination " +
            "left join TripAssignment assignment on assignment.trip_id = trip " +
            "and assignment.company_id.id = :companyId " +
            "and assignment.assignment_status not in :excludedStatuses " +
            "left join assignment.vehicle_id vehicle left join assignment.driver_user_id driver " +
            "where trip.company_id.id = :companyId " +
            "and trip.status <> com.example.Vi_vu_Viet.Util.Enums.TripStatus.DRAFT " +
            "and trip.departure_at >= :fromAt and trip.departure_at < :toAt " +
            "order by trip.departure_at, trip.id")
    List<Object[]> getTodayTrips(@Param("companyId") UUID companyId,
                                 @Param("fromAt") LocalDateTime fromAt,
                                 @Param("toAt") LocalDateTime toAt,
                                 @Param("excludedStatuses") List<DriverAssignmentStatus> excludedStatuses,
                                 Pageable pageable);

    // Chỉ join vé tại đây; tổng ghế được lấy riêng để tránh nhân bản số liệu.
    @Query("select route.id, route.code, route.name, count(distinct trip.id), " +
            "count(ticket.id), coalesce(sum(ticket.fare_amount), 0) " +
            "from Trip trip join trip.route_id route " +
            "left join Ticket ticket on ticket.trip_id = trip " +
            "and ticket.company_id.id = :companyId and ticket.status = :ticketStatus " +
            "where trip.company_id.id = :companyId and route.company_id.id = :companyId " +
            "and trip.status <> com.example.Vi_vu_Viet.Util.Enums.TripStatus.DRAFT " +
            "and trip.departure_at >= :fromAt and trip.departure_at < :toAt " +
            "group by route.id, route.code, route.name " +
            "order by coalesce(sum(ticket.fare_amount), 0) desc, count(distinct trip.id) desc, route.id")
    List<Object[]> getTopRoutes(@Param("companyId") UUID companyId,
                                @Param("fromAt") LocalDateTime fromAt,
                                @Param("toAt") LocalDateTime toAt,
                                @Param("ticketStatus") TicketStatus ticketStatus,
                                Pageable pageable);

    @Query("select trip.route_id.id, count(seat.id) from TripSeat seat join seat.trip_id trip " +
            "where seat.company_id.id = :companyId and trip.company_id.id = :companyId " +
            "and trip.route_id.id in :routeIds " +
            "and trip.status <> com.example.Vi_vu_Viet.Util.Enums.TripStatus.DRAFT " +
            "and trip.departure_at >= :fromAt and trip.departure_at < :toAt " +
            "group by trip.route_id.id")
    List<Object[]> getRouteSeatCounts(@Param("companyId") UUID companyId,
                                      @Param("routeIds") List<UUID> routeIds,
                                      @Param("fromAt") LocalDateTime fromAt,
                                      @Param("toAt") LocalDateTime toAt);

    @Query("select count(ticket), coalesce(sum(ticket.fare_amount), 0) from Ticket ticket " +
            "where ticket.company_id.id = :companyId and ticket.status = :status " +
            "and ticket.trip_id.departure_at >= :fromAt and ticket.trip_id.departure_at < :toAt")
    List<Object[]> getRevenueSummary(@Param("companyId") UUID companyId,
                                     @Param("fromAt") LocalDateTime fromAt,
                                     @Param("toAt") LocalDateTime toAt,
                                     @Param("status") TicketStatus status);

    @Query("select extract(year from trip.departure_at), extract(month from trip.departure_at), " +
            "extract(day from trip.departure_at), count(ticket), coalesce(sum(ticket.fare_amount), 0) " +
            "from Ticket ticket join ticket.trip_id trip " +
            "where ticket.company_id.id = :companyId and ticket.status = :status " +
            "and trip.departure_at >= :fromAt and trip.departure_at < :toAt " +
            "group by extract(year from trip.departure_at), extract(month from trip.departure_at), " +
            "extract(day from trip.departure_at) " +
            "order by extract(year from trip.departure_at), extract(month from trip.departure_at), " +
            "extract(day from trip.departure_at)")
    List<Object[]> getRevenueByDepartureDate(@Param("companyId") UUID companyId,
                                             @Param("fromAt") LocalDateTime fromAt,
                                             @Param("toAt") LocalDateTime toAt,
                                             @Param("status") TicketStatus status);
}
