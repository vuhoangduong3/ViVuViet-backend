package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Service.Trip.DashboardService;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardDriverResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardSummaryResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardTripPointResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.RevenueReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bus-companies/{company_id}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("@companyAuthorization.hasCompanyRole(authentication, #p0, 'COMPANY_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    DashboardService dashboardService;

    @Operation(summary = "KPI tổng quan của hãng",
            description = "Giá trị vé được tính từ vé đã xác nhận, không đại diện cho tiền thực thu.")
    @GetMapping("/dashboard/summary")
    public BaseResponse<DashboardSummaryResponse> getSummary(
            @PathVariable("company_id") UUID companyId,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return dashboardService.getSummary(companyId, from, to);
    }

    @Operation(summary = "Tổng hợp chuyến theo ngày khởi hành và trạng thái")
    @GetMapping("/dashboard/trips")
    public BaseResponse<List<DashboardTripPointResponse>> getTripPoints(
            @PathVariable("company_id") UUID companyId,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return dashboardService.getTripPoints(companyId, from, to);
    }

    @Operation(summary = "Top tài xế theo số chuyến được phân công",
            description = "Chỉ trả số liệu vận hành, không bao gồm lương hoặc tiền công.")
    @GetMapping("/dashboard/top-drivers")
    public BaseResponse<List<DashboardDriverResponse>> getTopDrivers(
            @PathVariable("company_id") UUID companyId,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return dashboardService.getTopDrivers(companyId, from, to);
    }

    @Operation(summary = "Báo cáo giá trị vé theo kỳ",
            description = "Mặc định lọc vé CONFIRMED; giá trị vé không thể hiện khoản tiền đã thu.")
    @GetMapping("/reports/revenue")
    public BaseResponse<RevenueReportResponse> getRevenue(
            @PathVariable("company_id") UUID companyId,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Trạng thái vé; mặc định CONFIRMED")
            @RequestParam(name = "status", required = false) TicketStatus status
    ) {
        return dashboardService.getRevenue(companyId, from, to, status);
    }
}
