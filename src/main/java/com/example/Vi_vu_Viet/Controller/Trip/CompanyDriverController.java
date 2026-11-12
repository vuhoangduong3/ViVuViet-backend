package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Service.Trip.CompanyDriverService;
import com.example.Vi_vu_Viet.Util.Enums.CompanyDriverStatus;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateCompanyDriverRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.ResetCompanyDriverPasswordRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateCompanyDriverRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateCompanyDriverStatusRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.PageResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyDriverCredentialResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyDriverResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyDriverTripResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bus-companies/{company_id}/drivers")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("@companyAuthorization.hasCompanyRole(authentication, #p0, 'COMPANY_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class CompanyDriverController {
    CompanyDriverService driverService;

    @Operation(summary = "Danh sách tài xế của hãng", description = "page bắt đầu từ 0, size từ 1 đến 100.")
    @GetMapping
    public BaseResponse<PageResponse<CompanyDriverResponse>> getDrivers(
            @PathVariable("company_id") UUID companyId,
            @Parameter(description = "Tìm theo tên, số điện thoại hoặc email; tối đa 200 ký tự")
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) CompanyDriverStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return driverService.getDrivers(companyId, search, status, page, size);
    }

    @Operation(summary = "Tạo tài khoản tài xế", description = "Tạo tài khoản mới và gán quyền DRIVER tại hãng. " +
            "Backend sinh mật khẩu và chỉ trả về trong response này.")
    @PostMapping
    public ResponseEntity<BaseResponse<CompanyDriverCredentialResponse>> createDriver(
            @PathVariable("company_id") UUID companyId,
            @Valid @RequestBody BaseRequest<CreateCompanyDriverRequest> request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(driverService.createDriver(companyId, request.getWsRequest()));
    }

    @Operation(summary = "Chi tiết tài xế", description = "driver_id là ID tài khoản dùng khi phân công chuyến.")
    @GetMapping("/{driver_id}")
    public BaseResponse<CompanyDriverResponse> getDriver(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("driver_id") UUID driverId) {
        return driverService.getDriver(companyId, driverId);
    }

    @Operation(summary = "Cập nhật thông tin cá nhân và GPLX của tài xế")
    @PatchMapping("/{driver_id}")
    public BaseResponse<CompanyDriverResponse> updateDriver(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("driver_id") UUID driverId,
            @Valid @RequestBody BaseRequest<UpdateCompanyDriverRequest> request) {
        return driverService.updateDriver(companyId, driverId, request.getWsRequest());
    }

    @Operation(summary = "Khóa hoặc mở khóa tài xế tại hãng", description = "LOCKED chặn phân công mới; " +
            "không thay đổi chuyến đã giao hoặc quyền tại hãng khác.")
    @PatchMapping("/{driver_id}/status")
    public BaseResponse<CompanyDriverResponse> updateStatus(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("driver_id") UUID driverId,
            @Valid @RequestBody BaseRequest<UpdateCompanyDriverStatusRequest> request) {
        return driverService.updateStatus(companyId, driverId, request.getWsRequest());
    }

    @Operation(summary = "Đặt lại mật khẩu tài xế", description = "Thu hồi các phiên đăng nhập cũ. " +
            "Mật khẩu mới chỉ trả về một lần; không áp dụng cho tài khoản admin hoặc tài khoản dùng chung nhiều hãng.")
    @PostMapping("/{driver_id}/reset-password")
    public ResponseEntity<BaseResponse<CompanyDriverCredentialResponse>> resetPassword(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("driver_id") UUID driverId,
            @Valid @RequestBody BaseRequest<ResetCompanyDriverPasswordRequest> request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(driverService.resetPassword(companyId, driverId, request.getWsRequest()));
    }

    @Operation(summary = "Chuyến được phân công gần đây của tài xế", description = "Sắp xếp theo lần phân công " +
            "mới nhất; gồm chuyến sắp tới, đang chạy, hoàn thành và đã hủy; bỏ phân công bị từ chối hoặc thay thế.")
    @GetMapping("/{driver_id}/trips")
    public BaseResponse<PageResponse<CompanyDriverTripResponse>> getDriverTrips(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("driver_id") UUID driverId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return driverService.getDriverTrips(companyId, driverId, page, size);
    }
}
