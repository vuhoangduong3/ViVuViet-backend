package com.example.Vi_vu_Viet.Controller.Trip;

import org.springframework.web.bind.annotation.PutMapping;
import com.example.Vi_vu_Viet.dto.Response.Trip.VehicleSeatLayoutResponse;
import com.example.Vi_vu_Viet.dto.Request.Trip.ConfigureVehicleSeatsRequest;
import com.example.Vi_vu_Viet.Service.Trip.VehicleService;
import com.example.Vi_vu_Viet.Util.Enums.VehicleStatus;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateVehicleRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateVehicleRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bus-companies/{company_id}/vehicles")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("@companyAuthorization.hasCompanyRole(authentication, #p0, 'COMPANY_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {

    VehicleService vehicleService;

    @Operation(summary = "Get vehicle seat layout")
    @GetMapping("/{vehicle_id}/seats")
    public BaseResponse<VehicleSeatLayoutResponse> getSeats(
            @PathVariable("company_id") UUID companyId, @PathVariable("vehicle_id") UUID vehicleId) {
        return vehicleService.getSeats(companyId, vehicleId);
    }

    @Operation(summary = "Configure vehicle seat layout", description = "Creates an immutable single-deck layout with no seat classes. Row and column coordinates start at 1; gaps represent aisles. Seat count sets passengerCapacity. Existing trip snapshots are preserved.")
    @PutMapping("/{vehicle_id}/seats")
    public BaseResponse<VehicleSeatLayoutResponse> configureSeats(
            @PathVariable("company_id") UUID companyId, @PathVariable("vehicle_id") UUID vehicleId,
            @Valid @RequestBody BaseRequest<ConfigureVehicleSeatsRequest> request) {
        return vehicleService.configureSeats(companyId, vehicleId, request.getWsRequest());
    }

    @Operation(summary = "Danh sách xe của hãng", description = "Tìm theo biển số/mẫu xe và lọc theo trạng thái.")
    @GetMapping
    public BaseResponse<List<VehicleResponse>> getVehicles(
            @PathVariable("company_id") UUID companyId,
            @Parameter(description = "Tìm theo biển số hoặc mẫu xe; tối đa 100 ký tự")
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) VehicleStatus status
    ) {
        return vehicleService.getVehicles(companyId, q, status);
    }

    @Operation(summary = "Thêm xe cho hãng")
    @PostMapping
    public BaseResponse<VehicleResponse> createVehicle(
            @PathVariable("company_id") UUID companyId,
            @Valid @RequestBody BaseRequest<CreateVehicleRequest> request
    ) {
        return vehicleService.createVehicle(companyId, request.getWsRequest());
    }

    @Operation(summary = "Chi tiết xe")
    @GetMapping("/{vehicle_id}")
    public BaseResponse<VehicleResponse> getVehicle(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("vehicle_id") UUID vehicleId
    ) {
        return vehicleService.getVehicle(companyId, vehicleId);
    }

    @Operation(summary = "Cập nhật thông tin hoặc trạng thái xe")
    @PatchMapping("/{vehicle_id}")
    public BaseResponse<VehicleResponse> updateVehicle(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("vehicle_id") UUID vehicleId,
            @Valid @RequestBody BaseRequest<UpdateVehicleRequest> request
    ) {
        return vehicleService.updateVehicle(companyId, vehicleId, request.getWsRequest());
    }

    @Operation(summary = "Ngừng hoặc xóa xe")
    @DeleteMapping("/{vehicle_id}")
    public BaseResponse<Void> deleteVehicle(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("vehicle_id") UUID vehicleId
    ) {
        return vehicleService.deleteVehicle(companyId, vehicleId);
    }
}
