package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Service.Trip.StopService;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateStopRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Location.StopResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bus-companies/{company_id}/stops")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("@companyAuthorization.hasCompanyRole(authentication, #p0, 'COMPANY_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class StopController {

    StopService stopService;

    @Operation(
            summary = "Danh sách điểm đón/trả của hãng",
            description = "Lấy các điểm đón/trả thuộc hãng trong URL. Chỉ quản trị viên của hãng đó được truy cập."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hãng hoặc hãng không hoạt động")
    })
    @GetMapping
    public BaseResponse<List<StopResponse>> getStops(
            @Parameter(description = "ID hãng xe")
            @PathVariable("company_id") UUID companyId
    ) {
        return stopService.getStops(companyId);
    }

    @Operation(
            summary = "Tạo điểm đón/trả",
            description = "Tạo điểm đón/trả thuộc hãng trong URL. Trạng thái mới luôn là ACTIVE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tạo điểm thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc area không hoạt động"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hãng hoặc hãng không hoạt động")
    })
    @PostMapping
    public BaseResponse<StopResponse> createStop(
            @Parameter(description = "ID hãng xe")
            @PathVariable("company_id") UUID companyId,
            @Valid @RequestBody BaseRequest<CreateStopRequest> request
    ) {
        return stopService.createStop(companyId, request.getWsRequest());
    }
}
