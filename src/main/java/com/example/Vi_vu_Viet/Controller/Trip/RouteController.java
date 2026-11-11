package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Service.Trip.RouteService;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateRouteRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateRouteRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.RouteResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bus-companies/{company_id}/routes")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("@companyAuthorization.hasCompanyRole(authentication, #p0, 'COMPANY_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class RouteController {

    RouteService routeService;

    @Operation(
            summary = "Danh sách tuyến của hãng",
            description = "Lấy danh sách tuyến của hãng trong URL cùng khu vực đầu và cuối mỗi tuyến."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách tuyến thành công"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hãng hoặc hãng không hoạt động")
    })
    @GetMapping
    public BaseResponse<List<RouteResponse>> getRoutes(
            @Parameter(description = "ID hãng xe")
            @PathVariable("company_id") UUID companyId
    ) {
        return routeService.getRoutes(companyId);
    }

    @Operation(
            summary = "Tạo tuyến cho hãng",
            description = "Tạo tuyến mẫu; trạng thái mới mặc định là ACTIVE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tạo tuyến thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ, mã tuyến bị trùng hoặc khu vực không hoạt động"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hãng hoặc hãng không hoạt động")
    })
    @PostMapping
    public BaseResponse<RouteResponse> createRoute(
            @Parameter(description = "ID hãng xe")
            @PathVariable("company_id") UUID companyId,
            @Valid @RequestBody BaseRequest<CreateRouteRequest> request
    ) {
        return routeService.createRoute(companyId, request.getWsRequest());
    }

    @Operation(
            summary = "Chi tiết tuyến",
            description = "Lấy chi tiết tuyến cùng khu vực đầu và cuối tuyến."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy tuyến thành công"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tuyến trong hãng này")
    })
    @GetMapping("/{route_id}")
    public BaseResponse<RouteResponse> getRoute(
            @Parameter(description = "ID hãng xe")
            @PathVariable("company_id") UUID companyId,
            @Parameter(description = "ID tuyến")
            @PathVariable("route_id") UUID routeId
    ) {
        return routeService.getRoute(companyId, routeId);
    }

    @Operation(
            summary = "Cập nhật tuyến",
            description = "Cập nhật một phần thông tin tuyến; các trường không gửi sẽ được giữ nguyên."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật tuyến thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc mã tuyến bị trùng"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "403", description = "Không có quyền quản lý hãng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tuyến trong hãng này")
    })
    @PatchMapping("/{route_id}")
    public BaseResponse<RouteResponse> updateRoute(
            @Parameter(description = "ID hãng xe")
            @PathVariable("company_id") UUID companyId,
            @Parameter(description = "ID tuyến")
            @PathVariable("route_id") UUID routeId,
            @Valid @RequestBody BaseRequest<UpdateRouteRequest> request
    ) {
        return routeService.updateRoute(companyId, routeId, request.getWsRequest());
    }

}
