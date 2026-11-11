package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Service.Trip.TripService;
import com.example.Vi_vu_Viet.Service.Trip.BookingService;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.ConfigureTripSeatsRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.AssignTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateTripRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripAssignmentResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripPassengerBookingResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.TripSeatResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;

@RestController
@RequestMapping("/api/v1/bus-companies/{company_id}/trips")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("@companyAuthorization.hasCompanyRole(authentication, #p0, 'COMPANY_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class TripManagementController {

    TripService tripService;
    BookingService bookingService;

    @Operation(
            summary = "Danh sách chuyến của hãng",
            description = "Lấy chuyến thuộc hãng trong URL, bao gồm cả chuyến nháp và chuyến đã mở bán."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách chuyến thành công"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hãng hoặc hãng không hoạt động")
    })
    @GetMapping
    public BaseResponse<List<TripResponse>> getCompanyTrips(
            @Parameter(description = "ID hãng xe")
            @PathVariable("company_id") UUID companyId,
            @Parameter(description = "Ngày khởi hành (yyyy-MM-dd)")
            @RequestParam(name = "date", required = false) LocalDate date,
            @RequestParam(name = "status", required = false) TripStatus status,
            @RequestParam(name = "routeId", required = false) UUID routeId,
            @Parameter(description = "ASSIGNED hoặc UNASSIGNED")
            @RequestParam(name = "assignmentStatus", required = false) String assignmentStatus
    ) {
        return tripService.getCompanyTrips(companyId, date, status, routeId, assignmentStatus);
    }

    @Operation(summary = "Danh sách đặt vé và hành khách của chuyến")
    @GetMapping("/{trip_id}/bookings")
    public BaseResponse<List<TripPassengerBookingResponse>> getTripBookings(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("trip_id") UUID tripId
    ) {
        return tripService.getTripBookings(companyId, tripId);
    }

    @Operation(summary = "Xem phân công xe và tài xế hiện tại")
    @GetMapping("/{trip_id}/assignments")
    public BaseResponse<TripAssignmentResponse> getTripAssignment(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("trip_id") UUID tripId
    ) {
        return tripService.getTripAssignment(companyId, tripId);
    }

    @Operation(summary = "Gán hoặc thay xe và tài xế cho chuyến")
    @PutMapping("/{trip_id}/assignments")
    public BaseResponse<TripAssignmentResponse> assignTrip(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("trip_id") UUID tripId,
            @Valid @RequestBody BaseRequest<AssignTripRequest> request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount actor
    ) {
        return tripService.assignTrip(companyId, tripId, request.getWsRequest(), actor);
    }

    @Operation(
            summary = "Tạo chuyến nháp",
            description = "Tạo chuyến theo tuyến đang hoạt động; khách nhập địa chỉ đón và trả khi đặt vé."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tạo chuyến nháp thành công"),
            @ApiResponse(responseCode = "400", description = "Tuyến, mã chuyến hoặc lịch trình không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hãng hoặc tuyến của hãng")
    })
    @PostMapping
    public BaseResponse<TripResponse> createTrip(
            @Parameter(description = "ID hãng xe")
            @PathVariable("company_id") UUID companyId,
            @Valid @RequestBody BaseRequest<CreateTripRequest> request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount actor
    ) {
        return tripService.createTrip(companyId, request.getWsRequest(), actor);
    }

    @Operation(
            summary = "Chi tiết chuyến của hãng",
            description = "Lấy chi tiết chuyến cùng khu vực đầu và cuối của tuyến."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết chuyến thành công"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyến trong hãng này")
    })
    @GetMapping("/{trip_id}")
    public BaseResponse<TripResponse> getCompanyTrip(
            @Parameter(description = "ID hãng xe")
            @PathVariable("company_id") UUID companyId,
            @Parameter(description = "ID chuyến")
            @PathVariable("trip_id") UUID tripId
    ) {
        return tripService.getCompanyTrip(companyId, tripId);
    }

    @Operation(
            summary = "Cập nhật chuyến nháp",
            description = "Cập nhật mã chuyến và thời gian khởi hành, đến dự kiến, giờ ngừng nhận đặt chỗ."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật chuyến thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu lịch trình không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyến trong hãng này"),
            @ApiResponse(responseCode = "409", description = "Chỉ có thể cập nhật chuyến nháp")
    })
    @PatchMapping("/{trip_id}")
    public BaseResponse<TripResponse> updateTrip(
            @Parameter(description = "ID hãng xe")
            @PathVariable("company_id") UUID companyId,
            @Parameter(description = "ID chuyến")
            @PathVariable("trip_id") UUID tripId,
            @Valid @RequestBody BaseRequest<UpdateTripRequest> request
    ) {
        return tripService.updateTrip(companyId, tripId, request.getWsRequest());
    }

    @Operation(
            summary = "Công bố và mở bán chuyến",
            description = "Mở bán chuyến sau khi đã cấu hình ghế, tuyến, thời gian và giá mặc định."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mở bán chuyến thành công"),
            @ApiResponse(responseCode = "400", description = "Chuyến chưa có ghế hoặc chưa đủ dữ liệu để mở bán"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyến trong hãng này"),
            @ApiResponse(responseCode = "409", description = "Trạng thái chuyến không cho phép công bố")
    })
    @PostMapping("/{trip_id}/publish")
    public BaseResponse<TripResponse> publishTrip(
            @Parameter(description = "ID hãng xe")
            @PathVariable("company_id") UUID companyId,
            @Parameter(description = "ID chuyến")
            @PathVariable("trip_id") UUID tripId
    ) {
        return tripService.publishTrip(companyId, tripId);
    }

    @Operation(summary = "Cấu hình ghế cho chuyến nháp", description = "Thay toàn bộ danh sách ghế; chỉ dùng khi chuyến chưa có đặt vé.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đã cấu hình ghế"),
            @ApiResponse(responseCode = "400", description = "Danh sách ghế không hợp lệ hoặc trùng mã"),
            @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyến"),
            @ApiResponse(responseCode = "409", description = "Chuyến đã mở bán hoặc đã có đặt vé")
    })
    @PutMapping("/{trip_id}/seats")
    public BaseResponse<List<TripSeatResponse>> configureTripSeats(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("trip_id") UUID tripId,
            @Valid @RequestBody BaseRequest<ConfigureTripSeatsRequest> request
    ) {
        return bookingService.configureTripSeats(companyId, tripId, request.getWsRequest());
    }
}
