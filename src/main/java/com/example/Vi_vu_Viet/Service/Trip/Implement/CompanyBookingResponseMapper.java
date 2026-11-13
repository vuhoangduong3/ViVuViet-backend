package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Repository.Trip.TripAssignmentRepository;
import com.example.Vi_vu_Viet.Util.CompanyBookingState;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.BookingViewStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.dto.Response.Booking.BookingTripResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingSummaryResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.TicketResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingAssignmentResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingResponse.HistoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class CompanyBookingResponseMapper {
    private final TripAssignmentRepository assignmentRepository;

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
        response.setStatus(CompanyBookingState.effectiveStatus(booking, now));
        response.setViewStatus(CompanyBookingState.viewStatus(booking, tickets, now));
        response.setPassengerTripStatus(booking.getStatus() == BookingStatus.CONFIRMED
                ? CompanyBookingState.passengerStatus(tickets) : null);
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
        response.setStatus(CompanyBookingState.effectiveStatus(booking, now));
        response.setViewStatus(CompanyBookingState.viewStatus(booking, tickets, now));
        response.setPassengerTripStatus(booking.getStatus() == BookingStatus.CONFIRMED
                ? CompanyBookingState.passengerStatus(tickets) : null);
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
                && CompanyBookingState.effectiveStatus(booking, now) == BookingStatus.EXPIRED;
        response.setTickets(tickets.stream().map(ticket -> toTicketResponse(ticket, expiredHold)).toList());
        if (!tickets.isEmpty()) {
            response.setPickupAddress(tickets.get(0).getPickup_address());
            response.setDropoffAddress(tickets.get(0).getDropoff_address());
        }
        List<Ticket> confirmedTickets = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.CONFIRMED).toList();
        response.setBoardedAt(sharedTime(confirmedTickets, Ticket::getBoarded_at));
        response.setCompletedAt(sharedTime(confirmedTickets, Ticket::getCompleted_at));
        response.setAssignment(assignmentRepository.findByCompanyAndTrip(
                        booking.getCompany_id().getId(), booking.getTrip_id().getId())
                .filter(assignment -> assignment.getAssignment_status() != DriverAssignmentStatus.REJECTED
                        && assignment.getAssignment_status() != DriverAssignmentStatus.REPLACED)
                .map(assignment -> new CompanyBookingAssignmentResponse(
                        assignment.getDriver_user_id().getId(), assignment.getDriver_user_id().getFull_name(),
                        assignment.getDriver_user_id().getPhone(), assignment.getVehicle_id().getId(),
                        assignment.getVehicle_id().getPlate_normalized(), assignment.getVehicle_id().getVehicle_model(),
                        assignment.getVehicle_id().getPassenger_capacity(), assignment.getAssignment_status()))
                .orElse(null));
        response.setHistory(history(booking, response));
        boolean consistent = confirmedTickets.stream().map(Ticket::getPassenger_trip_status).distinct().count() <= 1;
        response.setHistoryComplete(consistent && response.getHistory().stream()
                .filter(item -> "DONE".equals(item.state())).allMatch(item -> item.occurredAt() != null));
        return response;
    }

    private LocalDateTime sharedTime(List<Ticket> tickets, Function<Ticket, LocalDateTime> timestamp) {
        if (tickets.isEmpty() || tickets.stream().anyMatch(ticket -> timestamp.apply(ticket) == null)) return null;
        return tickets.stream().map(timestamp).max(Comparator.naturalOrder()).orElse(null);
    }

    private List<HistoryItem> history(Booking booking, CompanyBookingResponse response) {
        List<HistoryItem> items = new ArrayList<>();
        items.add(new HistoryItem("CREATED", "Đặt vé", booking.getCreated_at(), "DONE", null));
        if (booking.getConfirmed_at() != null || booking.getStatus() == BookingStatus.CONFIRMED) {
            items.add(new HistoryItem("CONFIRMED", "Xác nhận đơn", booking.getConfirmed_at(), "DONE", null));
        }
        BookingViewStatus view = response.getViewStatus();
        if (response.getBoardedAt() != null || view == BookingViewStatus.PICKED_UP
                || view == BookingViewStatus.COMPLETED) {
            items.add(new HistoryItem("PICKED_UP", "Đã đón", response.getBoardedAt(), "DONE", null));
        }
        if (response.getCompletedAt() != null || view == BookingViewStatus.COMPLETED) {
            items.add(new HistoryItem("COMPLETED", "Hoàn thành", response.getCompletedAt(), "DONE", null));
        }
        if (view == BookingViewStatus.CANCELLED) {
            items.add(new HistoryItem("CANCELLED", "Đã hủy", booking.getCancelled_at(), "DONE",
                    booking.getCancellation_reason()));
        } else if (view == BookingViewStatus.EXPIRED) {
            items.add(new HistoryItem("EXPIRED", "Hết hạn giữ chỗ", booking.getHold_expires_at(), "DONE", null));
        } else if (view == BookingViewStatus.NO_SHOW) {
            // Entity chưa lưu no_show_at: trả trạng thái thật, để trống giờ thay vì đoán từ updated_at.
            items.add(new HistoryItem("NO_SHOW", "Không lên xe", null, "DONE", "Chưa lưu thời điểm không lên xe"));
        }
        items.sort(Comparator.comparing(HistoryItem::occurredAt, Comparator.nullsLast(Comparator.naturalOrder())));
        if (view == BookingViewStatus.HOLDING) {
            items.add(new HistoryItem("CONFIRMED", "Xác nhận đơn", null, "PENDING", null));
        }
        if (view == BookingViewStatus.HOLDING || view == BookingViewStatus.BOOKED) {
            items.add(new HistoryItem("PICKED_UP", "Đã đón", null, "PENDING", null));
        }
        if (view == BookingViewStatus.HOLDING || view == BookingViewStatus.BOOKED
                || view == BookingViewStatus.PICKED_UP) {
            items.add(new HistoryItem("COMPLETED", "Hoàn thành", null, "PENDING", null));
        }
        return List.copyOf(items);
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
