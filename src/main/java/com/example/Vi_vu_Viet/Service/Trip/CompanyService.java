package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.MyCompanyResponse;

import java.util.List;
import java.util.UUID;

public interface CompanyService {

    BaseResponse<List<MyCompanyResponse>> getMyCompanies(UUID userId);
}
