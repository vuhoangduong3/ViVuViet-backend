package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Entity.Trip.TripAssignment;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.BookingViewStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.Util.Enums.PassengerTripStatus;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.dto.Request.Booking.CompanyBookingFilter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class CompanyBookingSpecifications {
    private CompanyBookingSpecifications() { }

    public static Specification<Booking> matching(UUID companyId, CompanyBookingFilter filter,
                                                   LocalDateTime now, boolean includeViewStatus) {
        return (booking, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(booking.get("company_id").get("id"), companyId));
            if (filter.getStatus() != null) predicates.add(status(booking, cb, filter.getStatus(), now));
            if (filter.getTripId() != null) {
                predicates.add(cb.equal(booking.get("trip_id").get("id"), filter.getTripId()));
            }
            if (filter.getRouteId() != null) {
                predicates.add(cb.equal(booking.get("trip_id").get("route_id").get("id"), filter.getRouteId()));
            }
            if (filter.getCustomerId() != null) {
                predicates.add(cb.equal(booking.get("customer_user_id").get("id"), filter.getCustomerId()));
            }
            if (filter.getCreatedFrom() != null) predicates.add(cb.greaterThanOrEqualTo(
                    booking.get("created_at"), filter.getCreatedFrom()));
            if (filter.getCreatedTo() != null) predicates.add(cb.lessThanOrEqualTo(
                    booking.get("created_at"), filter.getCreatedTo()));
            if (filter.getDepartureFrom() != null) predicates.add(cb.greaterThanOrEqualTo(
                    booking.get("trip_id").get("departure_at"), filter.getDepartureFrom()));
            if (filter.getDepartureTo() != null) predicates.add(cb.lessThanOrEqualTo(
                    booking.get("trip_id").get("departure_at"), filter.getDepartureTo()));
            if (filter.getDriverId() != null) {
                var assignmentQuery = query.subquery(Integer.class);
                var assignment = assignmentQuery.from(TripAssignment.class);
                assignmentQuery.select(cb.literal(1)).where(
                        cb.equal(assignment.get("company_id").get("id"), companyId),
                        cb.equal(assignment.get("trip_id"), booking.get("trip_id")),
                        cb.equal(assignment.get("driver_user_id").get("id"), filter.getDriverId()),
                        cb.not(assignment.get("assignment_status").in(
                                DriverAssignmentStatus.REJECTED, DriverAssignmentStatus.REPLACED)));
                predicates.add(cb.exists(assignmentQuery));
            }
            if (StringUtils.hasText(filter.getQ())) {
                String pattern = "%" + filter.getQ().trim().toLowerCase(Locale.ROOT)
                        .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
                var ticketQuery = query.subquery(Integer.class);
                var ticket = ticketQuery.from(Ticket.class);
                ticketQuery.select(cb.literal(1)).where(cb.equal(ticket.get("booking_id"), booking), cb.or(
                        cb.like(cb.lower(ticket.get("ticket_code")), pattern, '!'),
                        cb.like(cb.lower(ticket.get("passenger_name")), pattern, '!'),
                        cb.like(cb.lower(ticket.get("passenger_phone")), pattern, '!')));
                predicates.add(cb.or(
                        cb.like(cb.lower(booking.get("booking_code")), pattern, '!'),
                        cb.like(cb.lower(booking.get("contact_name")), pattern, '!'),
                        cb.like(cb.lower(booking.get("contact_phone")), pattern, '!'),
                        cb.like(cb.lower(booking.get("contact_email")), pattern, '!'),
                        cb.like(cb.lower(booking.get("trip_id").get("trip_code")), pattern, '!'),
                        cb.like(cb.lower(booking.get("trip_id").get("route_id").get("code")), pattern, '!'),
                        cb.like(cb.lower(booking.get("trip_id").get("route_id").get("name")), pattern, '!'),
                        cb.exists(ticketQuery)));
            }
            if (filter.getPassengerTripStatus() != null) {
                predicates.add(journey(booking, query, cb, filter.getPassengerTripStatus()));
            }
            if (includeViewStatus && filter.getViewStatus() != null) {
                predicates.add(view(booking, query, cb, filter.getViewStatus(), now));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Booking> withView(BookingViewStatus view, LocalDateTime now) {
        return (booking, query, cb) -> view(booking, query, cb, view, now);
    }

    private static Predicate status(Root<Booking> booking, CriteriaBuilder cb,
                                    BookingStatus status, LocalDateTime now) {
        Predicate expiredHold = cb.and(cb.equal(booking.get("status"), BookingStatus.HOLDING),
                cb.lessThanOrEqualTo(booking.get("hold_expires_at"), now));
        return switch (status) {
            case EXPIRED -> cb.or(cb.equal(booking.get("status"), BookingStatus.EXPIRED), expiredHold);
            case HOLDING -> cb.and(cb.equal(booking.get("status"), BookingStatus.HOLDING),
                    cb.greaterThan(booking.get("hold_expires_at"), now));
            default -> cb.equal(booking.get("status"), status);
        };
    }

    private static Predicate view(Root<Booking> booking, CriteriaQuery<?> query, CriteriaBuilder cb,
                                  BookingViewStatus view, LocalDateTime now) {
        return switch (view) {
            case HOLDING -> status(booking, cb, BookingStatus.HOLDING, now);
            case EXPIRED -> status(booking, cb, BookingStatus.EXPIRED, now);
            case CANCELLED -> status(booking, cb, BookingStatus.CANCELLED, now);
            case BOOKED -> journey(booking, query, cb, PassengerTripStatus.WAITING_PICKUP);
            case PICKED_UP -> journey(booking, query, cb, PassengerTripStatus.PICKED_UP);
            case COMPLETED -> journey(booking, query, cb, PassengerTripStatus.DROPPED_OFF);
            case NO_SHOW -> journey(booking, query, cb, PassengerTripStatus.NO_SHOW);
        };
    }

    private static Predicate journey(Root<Booking> booking, CriteriaQuery<?> query, CriteriaBuilder cb,
                                     PassengerTripStatus status) {
        Predicate waiting = cb.or(hasTicket(booking, query, cb, PassengerTripStatus.WAITING_PICKUP),
                cb.not(hasTicket(booking, query, cb, null)));
        Predicate pickedUp = hasTicket(booking, query, cb, PassengerTripStatus.PICKED_UP);
        Predicate noShow = hasTicket(booking, query, cb, PassengerTripStatus.NO_SHOW);
        Predicate progress = switch (status) {
            case WAITING_PICKUP -> waiting;
            case PICKED_UP -> cb.and(cb.not(waiting), pickedUp);
            case NO_SHOW -> cb.and(cb.not(waiting), cb.not(pickedUp), noShow);
            case DROPPED_OFF -> cb.and(cb.not(waiting), cb.not(pickedUp), cb.not(noShow),
                    hasTicket(booking, query, cb, PassengerTripStatus.DROPPED_OFF));
        };
        return cb.and(cb.equal(booking.get("status"), BookingStatus.CONFIRMED), progress);
    }

    private static Predicate hasTicket(Root<Booking> booking, CriteriaQuery<?> query, CriteriaBuilder cb,
                                       PassengerTripStatus status) {
        var ticketQuery = query.subquery(Integer.class);
        var ticket = ticketQuery.from(Ticket.class);
        List<Predicate> predicates = new ArrayList<>(List.of(cb.equal(ticket.get("booking_id"), booking),
                cb.equal(ticket.get("status"), TicketStatus.CONFIRMED)));
        if (status != null) {
            Predicate progress = cb.equal(ticket.get("passenger_trip_status"), status);
            if (status == PassengerTripStatus.WAITING_PICKUP) {
                progress = cb.or(progress, cb.isNull(ticket.get("passenger_trip_status")));
            }
            predicates.add(progress);
        }
        ticketQuery.select(cb.literal(1)).where(predicates.toArray(Predicate[]::new));
        return cb.exists(ticketQuery);
    }
}
