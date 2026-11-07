package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Service.Trip.TripService;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateTripRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bus-companies/{company_id}/trips")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("@companyAuthorization.hasCompanyRole(authentication, #p0, 'COMPANY_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class TripManagementController {

    TripService tripService;

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
            @PathVariable("company_id") UUID companyId
    ) {
        return tripService.getCompanyTrips(companyId);
    }

    @Operation(
            summary = "Tạo chuyến nháp",
            description = "Tạo chuyến từ tuyến đang hoạt động và chụp danh sách điểm dừng hiện tại của tuyến."
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
            description = "Lấy chi tiết chuyến và các điểm dừng đã được chụp vào lịch trình."
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
            description = "Cập nhật mã chuyến và thời gian; khi đổi giờ khởi hành, giờ các điểm dừng được dịch theo cùng khoảng thời gian."
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
            description = "Chuyển chuyến nháp sang SCHEDULED/OPEN sau khi kiểm tra tuyến, điểm dừng, thời gian và giá mặc định."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mở bán chuyến thành công"),
            @ApiResponse(responseCode = "400", description = "Chuyến chưa đủ dữ liệu để mở bán"),
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
}
