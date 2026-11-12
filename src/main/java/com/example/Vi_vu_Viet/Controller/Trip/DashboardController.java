package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Service.Trip.DashboardService;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardDriverResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardSummaryResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardTripPointResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.RevenueReportResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardTodayTripResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardRouteResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardIncidentResponse;
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
            description = "Mặc định 14 ngày gồm hôm nay theo múi giờ hãng. Có kỳ trước cùng số ngày và " +
                    "% thay đổi; null khi kỳ trước bằng 0 và kỳ này khác 0. Giá trị vé CONFIRMED, không phải tiền thực thu.")
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

    @Operation(summary = "Số chuyến và giá trị vé theo ngày khởi hành",
            description = "Mặc định 14 ngày. Trả đủ ngày và trạng thái bằng 0; revenue là VND, " +
                    "ticketCount là số vé CONFIRMED của chuyến khởi hành trong ngày. Tối đa 3660 ngày.")
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
            description = "Mặc định 14 ngày, tối đa 10 tài xế. Revenue là giá trị vé CONFIRMED " +
                    "của chuyến được phân công, không phải lương/tiền công. Rating null khi chưa có dữ liệu đánh giá.")
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

    @Operation(summary = "Các chuyến hôm nay của hãng",
            description = "Theo múi giờ hãng; bỏ chuyến nháp, sắp xếp giờ khởi hành tăng dần. " +
                    "Phân công bị từ chối/thay thế được coi là chưa phân công. lateMinutes đo trễ khởi hành.")
    @GetMapping("/dashboard/today-trips")
    public BaseResponse<List<DashboardTodayTripResponse>> getTodayTrips(
            @PathVariable("company_id") UUID companyId,
            @Parameter(description = "Số dòng từ 1 đến 100, mặc định 10")
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        return dashboardService.getTodayTrips(companyId, limit);
    }

    @Operation(summary = "Top tuyến theo giá trị vé CONFIRMED",
            description = "Mặc định toàn bộ thời gian; thiếu một cận ngày thì không giới hạn phía đó. " +
                    "Bỏ chuyến nháp. passengerCount là số vé; occupancyRate từ 0 đến 1, " +
                    "null nếu chưa có snapshot ghế. Xếp theo giá trị vé rồi số chuyến giảm dần.")
    @GetMapping("/dashboard/top-routes")
    public BaseResponse<List<DashboardRouteResponse>> getTopRoutes(
            @PathVariable("company_id") UUID companyId,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Số dòng từ 1 đến 100, mặc định 5")
            @RequestParam(name = "limit", defaultValue = "5") int limit
    ) {
        return dashboardService.getTopRoutes(companyId, from, to, limit);
    }

    @Operation(summary = "Sự cố đang mở hoặc đang xử lý",
            description = "Chỉ OPEN/IN_PROGRESS, mới nhất trước, không cắt theo ngày. " +
                    "Thông tin tài xế lấy từ người báo; incidentCode là mã ổn định từ UUID.")
    @GetMapping("/dashboard/open-incidents")
    public BaseResponse<List<DashboardIncidentResponse>> getOpenIncidents(
            @PathVariable("company_id") UUID companyId,
            @Parameter(description = "Số dòng từ 1 đến 100, mặc định 10")
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        return dashboardService.getOpenIncidents(companyId, limit);
    }

    @Operation(summary = "Báo cáo giá trị vé theo kỳ",
            description = "Mặc định 14 ngày, vé CONFIRMED; trả đủ ngày không có vé, kỳ trước và " +
                    "revenueChangePct cho badge biểu đồ. Giá trị vé không phải tiền thực thu. Tối đa 3660 ngày.")
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
