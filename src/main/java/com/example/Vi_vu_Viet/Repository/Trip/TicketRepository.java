package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @Query("select count(ticket) from Ticket ticket where ticket.trip_id.id = :tripId")
    long countByTripId(@Param("tripId") UUID tripId);

    @EntityGraph(attributePaths = "trip_seat_id")
    @Query("select ticket from Ticket ticket where ticket.booking_id.id = :bookingId " +
            "order by ticket.created_at, ticket.id")
    List<Ticket> findAllByBookingId(@Param("bookingId") UUID bookingId);

    @EntityGraph(attributePaths = "trip_seat_id")
    @Query("select ticket from Ticket ticket where ticket.booking_id.id in :bookingIds " +
            "order by ticket.created_at, ticket.id")
    List<Ticket> findAllByBookingIds(@Param("bookingIds") Collection<UUID> bookingIds);

    @Query("select ticket from Ticket ticket where ticket.trip_id.id = :tripId " +
            "and ticket.status in :statuses")
    List<Ticket> findAllActiveByTripId(
            @Param("tripId") UUID tripId,
            @Param("statuses") Collection<TicketStatus> statuses
    );

    @EntityGraph(attributePaths = {"trip_seat_id"})
    @Query("select ticket from Ticket ticket where ticket.id = :ticketId and ticket.trip_id.id = :tripId")
    Optional<Ticket> findByIdAndTripId(@Param("ticketId") UUID ticketId, @Param("tripId") UUID tripId);

    @Query("select count(ticket) from Ticket ticket where ticket.company_id.id = :companyId " +
            "and lower(ticket.ticket_code) = lower(:ticketCode)")
    long countByCompanyIdAndTicketCode(
            @Param("companyId") UUID companyId,
            @Param("ticketCode") String ticketCode
    );
}
