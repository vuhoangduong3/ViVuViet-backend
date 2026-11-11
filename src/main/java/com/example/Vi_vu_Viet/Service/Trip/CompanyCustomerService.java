package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyCustomerDetailResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyCustomerResponse;

import java.util.List;
import java.util.UUID;

public interface CompanyCustomerService {
    BaseResponse<List<CompanyCustomerResponse>> getCustomers(UUID companyId, String q,
                                                              UserAccountStatus accountStatus,
                                                              BookingStatus bookingStatus);
    BaseResponse<CompanyCustomerDetailResponse> getCustomer(UUID companyId, UUID customerId);
}
