package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.dto.Request.Trip.CreateStopRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Location.StopResponse;

import java.util.List;
import java.util.UUID;

public interface StopService {

    BaseResponse<List<StopResponse>> getStops(UUID companyId);

    BaseResponse<StopResponse> createStop(UUID companyId, CreateStopRequest request);
}
