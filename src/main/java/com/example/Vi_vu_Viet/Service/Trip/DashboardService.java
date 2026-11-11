package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardDriverResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardSummaryResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardTripPointResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.RevenueReportResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DashboardService {

    BaseResponse<DashboardSummaryResponse> getSummary(UUID companyId, LocalDate from, LocalDate to);

    BaseResponse<List<DashboardTripPointResponse>> getTripPoints(UUID companyId, LocalDate from, LocalDate to);

    BaseResponse<List<DashboardDriverResponse>> getTopDrivers(UUID companyId, LocalDate from, LocalDate to);

    BaseResponse<RevenueReportResponse> getRevenue(UUID companyId, LocalDate from, LocalDate to,
                                                    TicketStatus status);
}
