package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Service.Trip.BookingService;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Request.Booking.CreateBookingRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.BookingResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/bookings")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    BookingService bookingService;

    @Operation(summary = "Tạo đặt vé và giữ ghế", description = "Giữ các ghế đã chọn trong 5 phút; giá do server tính.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đã giữ ghế và tạo đặt vé"),
            @ApiResponse(responseCode = "400", description = "Thông tin hành khách hoặc ghế không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyến"),
            @ApiResponse(responseCode = "409", description = "Ghế không còn trống hoặc chuyến đã ngừng nhận đặt")
    })
    @PostMapping
    public BaseResponse<BookingResponse> createBooking(
            @Valid @RequestBody BaseRequest<CreateBookingRequest> request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount customer
    ) {
        return bookingService.createBooking(request.getWsRequest(), customer);
    }

    @Operation(summary = "Danh sách đặt vé của tôi",
            description = "Lọc theo trạng thái, tuyến và từ khóa; sắp xếp theo thời gian tạo mới nhất.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trang đặt vé của người dùng hiện tại"),
            @ApiResponse(responseCode = "400", description = "Bộ lọc hoặc phân trang không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping
    public BaseResponse<PageResponse<BookingResponse>> getMyBookings(
            @RequestParam(name = "status", required = false) BookingStatus status,
            @RequestParam(name = "routeId", required = false) UUID routeId,
            @Parameter(description = "Tìm mã đặt vé, mã vé, mã chuyến, mã/tên tuyến, hãng hoặc tên hành khách; tối đa 200 ký tự")
            @RequestParam(name = "q", required = false) String q,
            @Parameter(description = "Trang bắt đầu từ 0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Số đơn mỗi trang, từ 1 đến 100")
            @RequestParam(name = "size", defaultValue = "20") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount customer
    ) {
        return bookingService.getMyBookings(customer.getId(), status, routeId, q, page, size);
    }

    @Operation(summary = "Chuyến sắp tới của tôi",
            description = "Liệt kê toàn bộ đơn của khách đang đăng nhập trên chuyến SCHEDULED, " +
                    "gồm đơn CONFIRMED hoặc HOLDING còn hạn. Không lọc hãng/ngày và không phân trang.")
    @GetMapping("/upcoming")
    public BaseResponse<List<BookingResponse>> getMyUpcomingBookings(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount customer
    ) {
        return bookingService.getMyUpcomingBookings(customer.getId());
    }

    @Operation(summary = "Chuyến đang thực hiện của tôi",
            description = "Liệt kê toàn bộ đơn CONFIRMED của khách đang đăng nhập trên chuyến IN_PROGRESS. " +
                    "Không lọc hãng/ngày và không phân trang.")
    @GetMapping("/in-progress")
    public BaseResponse<List<BookingResponse>> getMyInProgressBookings(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount customer
    ) {
        return bookingService.getMyInProgressBookings(customer.getId());
    }

    @Operation(summary = "Chuyến hoàn thành của tôi",
            description = "Liệt kê toàn bộ đơn CONFIRMED của khách đang đăng nhập trên chuyến COMPLETED. " +
                    "Không lọc hãng/ngày và không phân trang.")
    @GetMapping("/completed")
    public BaseResponse<List<BookingResponse>> getMyCompletedBookings(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount customer
    ) {
        return bookingService.getMyCompletedBookings(customer.getId());
    }

    @Operation(summary = "Chuyến đã hủy của tôi",
            description = "Liệt kê toàn bộ đơn CANCELLED, EXPIRED, HOLDING quá hạn hoặc đơn trên chuyến " +
                    "CANCELLED/TERMINATED của khách đang đăng nhập. Không lọc hãng/ngày và không phân trang.")
    @GetMapping("/cancelled")
    public BaseResponse<List<BookingResponse>> getMyCancelledBookings(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount customer
    ) {
        return bookingService.getMyCancelledBookings(customer.getId());
    }

    @Operation(summary = "Chi tiết đặt vé của tôi",
            description = "Thông tin vé điện tử: hãng, tuyến, lịch chuyến, liên hệ, hành khách, ghế, địa chỉ đón/trả và giá đã đặt.")
    @GetMapping("/{booking_id}")
    public BaseResponse<BookingResponse> getMyBooking(
            @PathVariable("booking_id") UUID bookingId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount customer
    ) {
        return bookingService.getMyBooking(customer.getId(), bookingId);
    }

    @Operation(summary = "Xác nhận đặt vé")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đặt vé đã được xác nhận"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đặt vé"),
            @ApiResponse(responseCode = "409", description = "Giữ ghế đã hết hạn hoặc trạng thái không cho phép xác nhận")
    })
    @PostMapping("/{booking_id}/confirm")
    public BaseResponse<BookingResponse> confirmBooking(
            @PathVariable("booking_id") UUID bookingId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount customer
    ) {
        return bookingService.confirmBooking(customer.getId(), bookingId);
    }

    @Operation(summary = "Hủy đặt vé")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đặt vé đã được hủy"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đặt vé"),
            @ApiResponse(responseCode = "409", description = "Trạng thái đặt vé không cho phép hủy")
    })
    @PostMapping("/{booking_id}/cancel")
    public BaseResponse<BookingResponse> cancelBooking(
            @PathVariable("booking_id") UUID bookingId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount customer
    ) {
        return bookingService.cancelBooking(customer.getId(), bookingId);
    }
}
