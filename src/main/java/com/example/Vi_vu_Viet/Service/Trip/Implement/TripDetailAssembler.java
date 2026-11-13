package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.TripAssignment;
import com.example.Vi_vu_Viet.Repository.Trip.BookingRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TicketRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripAssignmentRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripSeatRepository;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripSalesStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TripDetailAssembler {
    private final TripResponseMapper tripResponseMapper;
    private final TripAssignmentRepository assignmentRepository;
    private final TripSeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final TripHistoryRecorder historyRecorder;

    public TripDetailResponse toDetail(Trip trip) {
        UUID companyId = trip.getCompany_id().getId();
        UUID tripId = trip.getId();
        LocalDateTime now = LocalDateTime.now();
        TripDetailResponse response = tripResponseMapper.populateTripResponse(trip, new TripDetailResponse());
        response.setTimezone(trip.getCompany_id().getTimezone());
        response.setCurrency("VND");
        TripAssignment assignment = assignmentRepository.findByCompanyAndTrip(companyId, tripId).orElse(null);
        response.setAssignment(assignment == null ? null : toAssignment(assignment));

        List<Booking> bookings = bookingRepository.findAllForCompanyTrip(companyId, tripId);
        List<UUID> bookingIds = bookings.stream().map(Booking::getId).toList();
        Map<UUID, List<Ticket>> ticketsByBooking = bookingIds.isEmpty() ? Map.of()
                : ticketRepository.findAllByBookingIds(bookingIds).stream()
                    .collect(Collectors.groupingBy(ticket -> ticket.getBooking_id().getId()));
        var confirmedSeatIds = new HashSet<UUID>();
        var heldSeatIds = new HashSet<UUID>();
        List<TripDetailResponse.Passenger> passengers = new ArrayList<>();
        int activeTicketCount = 0;
        for (Booking booking : bookings) {
            boolean expiredHold = booking.getStatus() == BookingStatus.HOLDING
                    && !booking.getHold_expires_at().isAfter(now);
            BookingStatus bookingStatus = expiredHold ? BookingStatus.EXPIRED : booking.getStatus();
            for (Ticket ticket : ticketsByBooking.getOrDefault(booking.getId(), List.of())) {
                TicketStatus ticketStatus = switch (bookingStatus) {
                    case EXPIRED -> TicketStatus.EXPIRED;
                    case CANCELLED -> TicketStatus.CANCELLED;
                    default -> ticket.getStatus();
                };
                passengers.add(toPassenger(booking, bookingStatus, ticket, ticketStatus));
                if (bookingStatus == BookingStatus.CONFIRMED && ticketStatus == TicketStatus.CONFIRMED) {
                    confirmedSeatIds.add(ticket.getTrip_seat_id().getId());
                    activeTicketCount++;
                } else if (bookingStatus == BookingStatus.HOLDING && ticketStatus == TicketStatus.HOLDING) {
                    heldSeatIds.add(ticket.getTrip_seat_id().getId());
                    activeTicketCount++;
                }
            }
        }
        long totalSeats = seatRepository.countByCompanyIdAndTripId(companyId, tripId);
        var occupiedSeatIds = new HashSet<>(confirmedSeatIds);
        occupiedSeatIds.addAll(heldSeatIds);
        long occupiedSeats = occupiedSeatIds.size();
        response.setSeats(new TripDetailResponse.SeatSummary(totalSeats, confirmedSeatIds.size(), heldSeatIds.size(),
                occupiedSeats, Math.max(0, totalSeats - occupiedSeats),
                totalSeats == 0 ? 0 : Math.round(occupiedSeats * 1000.0 / totalSeats) / 10.0));
        response.setPassengers(passengers);
        response.setTicketCount(passengers.size());
        response.setActiveTicketCount(activeTicketCount);
        var history = historyRecorder.timeline(trip, assignment);
        response.setHistory(history.items());
        response.setHistoryComplete(history.complete());
        boolean draft = trip.getStatus() == TripStatus.DRAFT && trip.getSales_status() == TripSalesStatus.CLOSED;
        boolean canCancel = (trip.getStatus() == TripStatus.DRAFT || trip.getStatus() == TripStatus.SCHEDULED)
                && trip.getActual_departure_at() == null;
        boolean canAssign = trip.getStatus() != TripStatus.COMPLETED && trip.getStatus() != TripStatus.CANCELLED
                && trip.getStatus() != TripStatus.TERMINATED;
        // Các cờ chỉ xét trạng thái; API mutation vẫn kiểm tra dữ liệu và điều kiện nghiệp vụ.
        response.setActions(new TripDetailResponse.Actions(draft, canAssign, draft, canCancel));
        return response;
    }

    private TripDetailResponse.Assignment toAssignment(TripAssignment assignment) {
        var driver = assignment.getDriver_user_id();
        var vehicle = assignment.getVehicle_id();
        var status = assignment.getAssignment_status() == null
                ? DriverAssignmentStatus.ASSIGNED : assignment.getAssignment_status();
        TripDetailResponse.Assignment response = new TripDetailResponse.Assignment();
        response.setTripId(assignment.getTrip_id().getId());
        response.setVehicleId(vehicle.getId());
        response.setDriverId(driver.getId());
        response.setDriverName(driver.getFull_name());
        response.setDriverPhone(driver.getPhone());
        response.setAssignedAt(assignment.getAssigned_at());
        response.setAssignmentStatus(status);
        response.setAcceptedAt(assignment.getAccepted_at());
        response.setRejectionReason(assignment.getRejection_reason());
        response.setAssignedBy(assignment.getAssigned_by() == null ? null : assignment.getAssigned_by().getId());
        response.setAssignedByName(assignment.getAssigned_by() == null
                ? null : assignment.getAssigned_by().getFull_name());
        response.setPlateNumber(vehicle.getPlate_normalized());
        response.setVehicleModel(vehicle.getVehicle_model());
        response.setPassengerCapacity(vehicle.getPassenger_capacity());
        response.setActive(status != DriverAssignmentStatus.REJECTED && status != DriverAssignmentStatus.REPLACED);
        return response;
    }

    private TripDetailResponse.Passenger toPassenger(Booking booking, BookingStatus bookingStatus,
                                                    Ticket ticket, TicketStatus ticketStatus) {
        TripDetailResponse.Passenger response = new TripDetailResponse.Passenger();
        response.setId(ticket.getId());
        response.setTicketCode(ticket.getTicket_code());
        response.setPassengerName(ticket.getPassenger_name());
        response.setPassengerPhone(ticket.getPassenger_phone());
        response.setPickupAddress(ticket.getPickup_address());
        response.setDropoffAddress(ticket.getDropoff_address());
        response.setSeatId(ticket.getTrip_seat_id().getId());
        response.setSeatCode(ticket.getSeat_label_snapshot());
        response.setFareAmount(ticket.getFare_amount());
        response.setStatus(ticketStatus);
        response.setPassengerTripStatus(ticket.getPassenger_trip_status());
        response.setCreatedAt(ticket.getCreated_at());
        response.setBookingId(booking.getId());
        response.setBookingCode(booking.getBooking_code());
        response.setBookingStatus(bookingStatus);
        response.setContactName(booking.getContact_name());
        response.setContactPhone(booking.getContact_phone());
        response.setPaymentCollected(null);
        return response;
    }
}
