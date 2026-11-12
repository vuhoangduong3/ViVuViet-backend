package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.dto.Request.Booking.CreateBookingRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.PageResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.BookingResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.TripSeatResponse;

import java.util.List;
import java.util.UUID;

public interface BookingService {

    BaseResponse<List<TripSeatResponse>> getAvailableSeats(UUID tripId);

    BaseResponse<BookingResponse> createBooking(CreateBookingRequest request, UserAccount customer);

    BaseResponse<PageResponse<BookingResponse>> getMyBookings(
            UUID customerId, BookingStatus status, UUID routeId, String q, int page, int size);

    BaseResponse<BookingResponse> getMyBooking(UUID customerId, UUID bookingId);

    BaseResponse<BookingResponse> confirmBooking(UUID customerId, UUID bookingId);

    BaseResponse<BookingResponse> cancelBooking(UUID customerId, UUID bookingId);
}
