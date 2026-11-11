package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DashboardRepository extends JpaRepository<Trip, UUID> {

    @Query("select count(trip), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.SCHEDULED then 1 else 0 end), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.IN_PROGRESS then 1 else 0 end), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.COMPLETED then 1 else 0 end), " +
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.CANCELLED then 1 else 0 end) " +
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
            "sum(case when trip.status = com.example.Vi_vu_Viet.Util.Enums.TripStatus.CANCELLED then 1 else 0 end) " +
            "from TripAssignment assignment join assignment.trip_id trip " +
            "join assignment.driver_user_id driver " +
            "where assignment.company_id.id = :companyId " +
            "and assignment.assignment_status not in " +
            "(com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus.REJECTED, " +
            "com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus.REPLACED) " +
            "and trip.status <> com.example.Vi_vu_Viet.Util.Enums.TripStatus.DRAFT " +
            "and trip.departure_at >= :fromAt and trip.departure_at < :toAt " +
            "group by driver.id, driver.full_name order by count(trip.id) desc, driver.full_name")
    List<Object[]> getTopDrivers(@Param("companyId") UUID companyId,
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
