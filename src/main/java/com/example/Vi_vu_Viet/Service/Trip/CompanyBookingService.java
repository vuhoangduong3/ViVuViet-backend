package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.dto.Request.Booking.CancelCompanyBookingRequest;
import com.example.Vi_vu_Viet.dto.Request.Booking.CompanyBookingFilter;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingSummaryResponse;
import com.example.Vi_vu_Viet.dto.Response.PageResponse;

import java.util.UUID;

public interface CompanyBookingService {
    BaseResponse<PageResponse<CompanyBookingSummaryResponse>> getCompanyBookings(
            UUID companyId, CompanyBookingFilter filter);

    BaseResponse<CompanyBookingResponse> getCompanyBooking(UUID companyId, UUID bookingId);

    BaseResponse<CompanyBookingResponse> cancelCompanyBooking(
            UUID companyId, UUID bookingId, CancelCompanyBookingRequest request, UserAccount actor);
}
