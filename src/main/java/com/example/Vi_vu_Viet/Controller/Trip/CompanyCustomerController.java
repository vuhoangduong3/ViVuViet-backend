package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Service.Trip.CompanyCustomerService;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyCustomerDetailResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyCustomerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bus-companies/{company_id}/customers")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("@companyAuthorization.hasCompanyRole(authentication, #p0, 'COMPANY_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class CompanyCustomerController {

    CompanyCustomerService customerService;

    @Operation(summary = "Danh sách khách đã đặt vé tại hãng")
    @GetMapping
    public BaseResponse<List<CompanyCustomerResponse>> getCustomers(
            @PathVariable("company_id") UUID companyId,
            @Parameter(description = "Tìm theo tên, email, số điện thoại, mã booking hoặc tên hành khách")
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "accountStatus", required = false) UserAccountStatus accountStatus,
            @RequestParam(name = "bookingStatus", required = false) BookingStatus bookingStatus
    ) {
        return customerService.getCustomers(companyId, q, accountStatus, bookingStatus);
    }

    @Operation(summary = "Chi tiết khách và lịch sử booking tại hãng")
    @GetMapping("/{customer_id}")
    public BaseResponse<CompanyCustomerDetailResponse> getCustomer(
            @PathVariable("company_id") UUID companyId,
            @PathVariable("customer_id") UUID customerId
    ) {
        return customerService.getCustomer(companyId, customerId);
    }
}
