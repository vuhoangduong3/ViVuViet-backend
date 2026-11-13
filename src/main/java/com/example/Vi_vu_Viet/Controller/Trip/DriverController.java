package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Service.Trip.DriverService;
import com.example.Vi_vu_Viet.Util.Enums.PassengerTripStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.CollectTicketPaymentRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.ReportTripIncidentRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.RejectTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateDriverProfileRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateDriverAssignmentStatusRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdatePassengerTripStatusRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateTripProgressRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.TicketResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.DriverProfileResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.DriverTripResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripIncidentResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Comparator;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/me")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
public class DriverController {

    DriverService driverService;

    @Operation(summary = "Hồ sơ tài xế và giấy phép")
    @GetMapping("/driver-profile")
    public BaseResponse<List<DriverProfileResponse>> getDriverProfiles(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        return driverService.getDriverProfiles(driver.getId());
    }

    @Operation(summary = "Cập nhật mã nhân viên của tài xế",
            description = "companyId xác định hồ sơ tại hãng. Thông tin giấy phép chỉ được cập nhật bởi admin hãng.")
    @PatchMapping("/driver-profile")
    public BaseResponse<DriverProfileResponse> updateDriverProfile(
            @Valid @RequestBody BaseRequest<UpdateDriverProfileRequest> request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        return driverService.updateDriverProfile(driver.getId(), request.getWsRequest());
    }

    @Operation(summary = "Lịch và chuyến được phân công")
    @GetMapping("/driver/trips")
    public BaseResponse<List<DriverTripResponse>> getDriverTrips(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "status", required = false) TripStatus status,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        return driverService.getDriverTrips(driver.getId(), from, to, status);
    }

    @Operation(summary = "Chi tiết chuyến, tuyến, xe và hành khách")
    @GetMapping("/driver/trips/{trip_id}")
    public BaseResponse<DriverTripResponse> getDriverTrip(
            @PathVariable("trip_id") UUID tripId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        return driverService.getDriverTrip(driver.getId(), tripId);
    }

    @Operation(summary = "Xác nhận nhận chuyến")
    @PostMapping("/driver/trips/{trip_id}/accept")
    public BaseResponse<DriverTripResponse> acceptTrip(
            @PathVariable("trip_id") UUID tripId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        return driverService.acceptTrip(driver.getId(), tripId);
    }

    @Operation(summary = "Từ chối chuyến và nêu lý do")
    @PostMapping("/driver/trips/{trip_id}/reject")
    public BaseResponse<DriverTripResponse> rejectTrip(
            @PathVariable("trip_id") UUID tripId,
            @Valid @RequestBody BaseRequest<RejectTripRequest> request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        return driverService.rejectTrip(driver.getId(), tripId, request.getWsRequest());
    }

    @Operation(summary = "Cập nhật tiến trình chuyến")
    @PatchMapping("/driver/trips/{trip_id}/status")
    public BaseResponse<DriverTripResponse> updateTripProgress(
            @PathVariable("trip_id") UUID tripId,
            @Valid @RequestBody BaseRequest<UpdateTripProgressRequest> request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        return driverService.updateTripProgress(driver.getId(), tripId, request.getWsRequest());
    }

    @Operation(summary = "Cập nhật trạng thái chuẩn bị hoặc đã có mặt")
    @PatchMapping("/driver/trips/{trip_id}/assignment-status")
    public BaseResponse<DriverTripResponse> updateAssignmentStatus(
            @PathVariable("trip_id") UUID tripId,
            @Valid @RequestBody BaseRequest<UpdateDriverAssignmentStatusRequest> request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        return driverService.updateAssignmentStatus(driver.getId(), tripId, request.getWsRequest().getStatus());
    }

    @Operation(summary = "Cập nhật trạng thái đón/trả hành khách")
    @PatchMapping("/driver/trips/{trip_id}/passengers/{ticket_id}/status")
    public BaseResponse<TicketResponse> updatePassengerStatus(
            @PathVariable("trip_id") UUID tripId,
            @PathVariable("ticket_id") UUID ticketId,
            @Valid @RequestBody BaseRequest<UpdatePassengerTripStatusRequest> request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        PassengerTripStatus status = request.getWsRequest().getStatus();
        return driverService.updatePassengerStatus(driver.getId(), tripId, ticketId, status);
    }

    @Operation(summary = "Xác nhận đã thu tiền mặt của một vé",
            description = "Chỉ tài xế đã nhận phân công hiện tại được thao tác. " +
                    "Vé và đơn phải CONFIRMED, chuyến đang chờ khởi hành, đang chạy hoặc đã hoàn thành. " +
                    "Gửi lại cùng số tiền trả kết quả đã lưu; số tiền khác trả 409, không cộng dồn. " +
                    "Thời điểm và người thu do server xác định.")
    @PostMapping("/driver/trips/{trip_id}/passengers/{ticket_id}/collect-payment")
    public BaseResponse<TicketResponse> collectTicketPayment(
            @PathVariable("trip_id") UUID tripId,
            @PathVariable("ticket_id") UUID ticketId,
            @Valid @RequestBody BaseRequest<CollectTicketPaymentRequest> request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        return driverService.collectTicketPayment(driver.getId(), tripId, ticketId, request.getWsRequest());
    }

    @Operation(summary = "Báo sự cố chuyến")
    @PostMapping("/driver/trips/{trip_id}/incidents")
    public BaseResponse<TripIncidentResponse> reportIncident(
            @PathVariable("trip_id") UUID tripId,
            @Valid @RequestBody BaseRequest<ReportTripIncidentRequest> request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        return driverService.reportIncident(driver.getId(), tripId, request.getWsRequest());
    }

    @Operation(summary = "Xe được giao cho chuyến sắp tới hoặc đang chạy")
    @GetMapping("/driver/vehicle")
    public BaseResponse<VehicleResponse> getAssignedVehicle(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        return driverService.getAssignedVehicle(driver.getId());
    }

    @Operation(summary = "Lịch sử chuyến gần đây của xe được giao",
            description = "Gồm chuyến COMPLETED và TERMINATED của tài xế trên xe được giao, mới nhất trước.")
    @GetMapping("/driver/vehicle/trips/recent")
    public BaseResponse<List<DriverTripResponse>> getRecentVehicleTrips(
            @RequestParam(defaultValue = "4") int limit,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount driver
    ) {
        if (limit < 1 || limit > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 100");
        UUID vehicleId = driverService.getAssignedVehicle(driver.getId()).getWsResponse().getId();
        List<DriverTripResponse> trips = driverService.getDriverTrips(driver.getId(), null, null, null)
                .getWsResponse().stream()
                .filter(item -> vehicleId.equals(item.getVehicle().getId()))
                .filter(item -> item.getTrip().getStatus() == TripStatus.COMPLETED
                        || item.getTrip().getStatus() == TripStatus.TERMINATED)
                .sorted(Comparator.comparing((DriverTripResponse item) -> item.getTrip().getDepartureAt()).reversed())
                .limit(limit).toList();
        BaseResponse<List<DriverTripResponse>> response = new BaseResponse<>(com.example.Vi_vu_Viet.Util.ApplicationCode.SUCCESS);
        response.setWsResponse(trips);
        return response;
    }
}
