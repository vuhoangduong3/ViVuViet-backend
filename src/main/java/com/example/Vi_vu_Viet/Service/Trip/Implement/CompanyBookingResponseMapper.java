package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.dto.Response.Booking.BookingTripResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingSummaryResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.TicketResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CompanyBookingResponseMapper {

    public CompanyBookingSummaryResponse toSummary(Booking booking, List<Ticket> tickets, LocalDateTime now) {
        var trip = booking.getTrip_id();
        var route = trip.getRoute_id();
        CompanyBookingSummaryResponse response = new CompanyBookingSummaryResponse();
        response.setId(booking.getId());
        response.setBookingCode(booking.getBooking_code());
        response.setCompanyId(booking.getCompany_id().getId());
        response.setCustomerId(booking.getCustomer_user_id().getId());
        response.setContactName(booking.getContact_name());
        response.setContactPhone(booking.getContact_phone());
        response.setContactEmail(booking.getContact_email());
        response.setTripId(trip.getId());
        response.setTripCode(trip.getTrip_code());
        response.setTripStatus(trip.getStatus());
        response.setDepartureAt(trip.getDeparture_at());
        response.setExpectedArrivalAt(trip.getExpected_arrival_at());
        response.setTimezone(booking.getCompany_id().getTimezone());
        response.setRouteId(route.getId());
        response.setRouteName(route.getName());
        response.setOriginAreaName(route.getOrigin_area_id().getName());
        response.setDestinationAreaName(route.getDestination_area_id().getName());
        response.setTicketCount(tickets.size());
        response.setSeatCodes(tickets.stream().map(Ticket::getSeat_label_snapshot).toList());
        response.setQuotedTotalAmount(booking.getQuoted_total_amount());
        response.setCurrency(booking.getCurrency());
        response.setStatus(effectiveStatus(booking, now));
        response.setHoldExpiresAt(booking.getHold_expires_at());
        response.setCreatedAt(booking.getCreated_at());
        response.setConfirmedAt(booking.getConfirmed_at());
        response.setCancelledAt(booking.getCancelled_at());
        response.setUpdatedAt(booking.getUpdated_at());
        return response;
    }

    public CompanyBookingResponse toDetail(Booking booking, List<Ticket> tickets, LocalDateTime now) {
        CompanyBookingResponse response = new CompanyBookingResponse();
        response.setId(booking.getId());
        response.setCompanyId(booking.getCompany_id().getId());
        response.setCustomerId(booking.getCustomer_user_id().getId());
        response.setTripId(booking.getTrip_id().getId());
        response.setTrip(toTripResponse(booking));
        response.setBookingCode(booking.getBooking_code());
        response.setContactName(booking.getContact_name());
        response.setContactPhone(booking.getContact_phone());
        response.setContactEmail(booking.getContact_email());
        response.setStatus(effectiveStatus(booking, now));
        response.setHoldExpiresAt(booking.getHold_expires_at());
        response.setQuotedTotalAmount(booking.getQuoted_total_amount());
        response.setCurrency(booking.getCurrency());
        response.setConfirmedAt(booking.getConfirmed_at());
        response.setCancelledAt(booking.getCancelled_at());
        response.setCancellationReason(booking.getCancellation_reason());
        response.setCancelledBy(booking.getCancelled_by() == null ? null : booking.getCancelled_by().getId());
        response.setCreatedAt(booking.getCreated_at());
        response.setUpdatedAt(booking.getUpdated_at());
        boolean expiredHold = booking.getStatus() == BookingStatus.HOLDING
                && effectiveStatus(booking, now) == BookingStatus.EXPIRED;
        response.setTickets(tickets.stream().map(ticket -> toTicketResponse(ticket, expiredHold)).toList());
        return response;
    }

    private BookingStatus effectiveStatus(Booking booking, LocalDateTime now) {
        return booking.getStatus() == BookingStatus.HOLDING && !booking.getHold_expires_at().isAfter(now)
                ? BookingStatus.EXPIRED : booking.getStatus();
    }

    private BookingTripResponse toTripResponse(Booking booking) {
        var trip = booking.getTrip_id();
        var company = booking.getCompany_id();
        var route = trip.getRoute_id();
        BookingTripResponse response = new BookingTripResponse();
        response.setId(trip.getId());
        response.setTripCode(trip.getTrip_code());
        response.setStatus(trip.getStatus());
        response.setDepartureAt(trip.getDeparture_at());
        response.setExpectedArrivalAt(trip.getExpected_arrival_at());
        response.setCompanyId(company.getId());
        response.setCompanyName(company.getName());
        response.setCompanyHotline(company.getHotline());
        response.setTimezone(company.getTimezone());
        response.setRouteId(route.getId());
        response.setRouteCode(route.getCode());
        response.setRouteName(route.getName());
        response.setOriginAreaId(route.getOrigin_area_id().getId());
        response.setOriginAreaName(route.getOrigin_area_id().getName());
        response.setDestinationAreaId(route.getDestination_area_id().getId());
        response.setDestinationAreaName(route.getDestination_area_id().getName());
        return response;
    }

    private TicketResponse toTicketResponse(Ticket ticket, boolean expiredHold) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTicketCode(ticket.getTicket_code());
        response.setPassengerName(ticket.getPassenger_name());
        response.setPassengerPhone(ticket.getPassenger_phone());
        response.setPickupAddress(ticket.getPickup_address());
        response.setDropoffAddress(ticket.getDropoff_address());
        response.setSeatId(ticket.getTrip_seat_id().getId());
        response.setSeatCode(ticket.getSeat_label_snapshot());
        response.setFareAmount(ticket.getFare_amount());
        response.setStatus(expiredHold && ticket.getStatus() == TicketStatus.HOLDING
                ? TicketStatus.EXPIRED : ticket.getStatus());
        response.setPassengerTripStatus(ticket.getPassenger_trip_status());
        response.setCreatedAt(ticket.getCreated_at());
        return response;
    }
}
