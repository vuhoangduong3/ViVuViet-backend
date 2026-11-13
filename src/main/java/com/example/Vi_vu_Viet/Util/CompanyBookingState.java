package com.example.Vi_vu_Viet.Util;

import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.BookingViewStatus;
import com.example.Vi_vu_Viet.Util.Enums.PassengerTripStatus;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;

public final class CompanyBookingState {
    private CompanyBookingState() { }

    public static BookingStatus effectiveStatus(Booking booking, LocalDateTime now) {
        return booking.getStatus() == BookingStatus.HOLDING && !booking.getHold_expires_at().isAfter(now)
                ? BookingStatus.EXPIRED : booking.getStatus();
    }

    // Một đơn có chung hành trình. Với dữ liệu cũ lệch trạng thái, ưu tiên bước chưa hoàn tất.
    // Thứ tự này phải khớp CompanyBookingSpecifications.journey().
    public static PassengerTripStatus passengerStatus(List<Ticket> tickets) {
        List<PassengerTripStatus> statuses = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.CONFIRMED)
                .map(ticket -> ticket.getPassenger_trip_status() == null
                        ? PassengerTripStatus.WAITING_PICKUP : ticket.getPassenger_trip_status()).toList();
        if (statuses.isEmpty() || statuses.contains(PassengerTripStatus.WAITING_PICKUP)) {
            return PassengerTripStatus.WAITING_PICKUP;
        }
        if (statuses.contains(PassengerTripStatus.PICKED_UP)) return PassengerTripStatus.PICKED_UP;
        if (statuses.contains(PassengerTripStatus.NO_SHOW)) return PassengerTripStatus.NO_SHOW;
        return PassengerTripStatus.DROPPED_OFF;
    }

    public static BookingViewStatus viewStatus(Booking booking, List<Ticket> tickets, LocalDateTime now) {
        return switch (effectiveStatus(booking, now)) {
            case CANCELLED -> BookingViewStatus.CANCELLED;
            case EXPIRED -> BookingViewStatus.EXPIRED;
            case HOLDING -> BookingViewStatus.HOLDING;
            case CONFIRMED -> switch (passengerStatus(tickets)) {
                case WAITING_PICKUP -> BookingViewStatus.BOOKED;
                case PICKED_UP -> BookingViewStatus.PICKED_UP;
                case DROPPED_OFF -> BookingViewStatus.COMPLETED;
                case NO_SHOW -> BookingViewStatus.NO_SHOW;
            };
        };
    }
}
