package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.Util.Enums.CompanyDriverStatus;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateCompanyDriverRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.ResetCompanyDriverPasswordRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateCompanyDriverRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateCompanyDriverStatusRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.PageResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyDriverCredentialResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyDriverResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyDriverTripResponse;

import java.util.UUID;

public interface CompanyDriverService {
    BaseResponse<PageResponse<CompanyDriverResponse>> getDrivers(UUID companyId, String search,
                                                                CompanyDriverStatus status, int page, int size);

    BaseResponse<CompanyDriverCredentialResponse> createDriver(UUID companyId, CreateCompanyDriverRequest request);

    BaseResponse<CompanyDriverResponse> getDriver(UUID companyId, UUID driverId);

    BaseResponse<CompanyDriverResponse> updateDriver(UUID companyId, UUID driverId, UpdateCompanyDriverRequest request);

    BaseResponse<CompanyDriverResponse> updateStatus(UUID companyId, UUID driverId,
                                                    UpdateCompanyDriverStatusRequest request);

    BaseResponse<CompanyDriverCredentialResponse> resetPassword(UUID companyId, UUID driverId,
                                                               ResetCompanyDriverPasswordRequest request);

    BaseResponse<PageResponse<CompanyDriverTripResponse>> getDriverTrips(UUID companyId, UUID driverId,
                                                                       int page, int size);
}
