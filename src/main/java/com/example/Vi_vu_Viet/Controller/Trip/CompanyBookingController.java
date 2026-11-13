package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Service.Trip.CompanyBookingService;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Request.Booking.CancelCompanyBookingRequest;
import com.example.Vi_vu_Viet.dto.Request.Booking.CompanyBookingFilter;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingSummaryResponse;
import com.example.Vi_vu_Viet.dto.Response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bus-companies/{company_id}/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("@companyAuthorization.hasCompanyRole(authentication, #p0, 'COMPANY_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy hãng đang hoạt động hoặc đơn thuộc hãng")
})
public class CompanyBookingController {

    CompanyBookingService bookingService;

    @Operation(summary = "Danh sách đơn đặt vé toàn hãng",
            description = "Tìm kiếm, lọc và phân trang; sắp xếp đơn mới nhất trước. " +
                    "Đơn giữ ghế quá hạn được tính là EXPIRED ngay trong bộ lọc và response.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trang đơn đặt vé của hãng"),
            @ApiResponse(responseCode = "400", description = "Bộ lọc, khoảng thời gian hoặc phân trang không hợp lệ")
    })
    @GetMapping
    public BaseResponse<PageResponse<CompanyBookingSummaryResponse>> getCompanyBookings(
            @PathVariable("company_id") UUID companyId,
            @Valid @ModelAttribute @ParameterObject CompanyBookingFilter filter
    ) {
        return bookingService.getCompanyBookings(companyId, filter);
    }

    @Operation(summary = "Chi tiết đơn đặt vé của hãng",
            description = "Gồm liên hệ, chuyến, tuyến, toàn bộ vé/hành khách, ghế, địa chỉ đón/trả và giá đã đặt.")
    @ApiResponse(responseCode = "200", description = "Chi tiết đơn đặt vé")
    @GetMapping("/{booking_id}")
    public BaseResponse<CompanyBookingResponse> getCompanyBooking(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("booking_id") UUID bookingId
    ) {
        return bookingService.getCompanyBooking(companyId, bookingId);
    }

    @Operation(summary = "Admin hãng hủy đơn đặt vé",
            description = "Hủy đơn HOLDING còn hạn hoặc CONFIRMED trên chuyến chưa bắt đầu; " +
                    "giải phóng ghế bằng cách hủy vé, lưu lý do và người hủy. " +
                    "Gọi lại đơn đã hủy trả chi tiết và giữ nguyên thông tin hủy ban đầu.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chi tiết đơn sau hủy"),
            @ApiResponse(responseCode = "400", description = "Lý do hủy bị thiếu hoặc vượt quá 500 ký tự"),
            @ApiResponse(responseCode = "409", description = "Đơn hết hạn hoặc chuyến không còn cho phép hủy đơn")
    })
    @PostMapping("/{booking_id}/cancel")
    public BaseResponse<CompanyBookingResponse> cancelCompanyBooking(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("booking_id") UUID bookingId,
            @Valid @RequestBody BaseRequest<CancelCompanyBookingRequest> request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount actor
    ) {
        return bookingService.cancelCompanyBooking(companyId, bookingId, request.getWsRequest(), actor);
    }
}
