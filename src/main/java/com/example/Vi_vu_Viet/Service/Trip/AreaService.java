package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.dto.Response.Location.AreaResponse;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;

import java.util.List;
import java.util.UUID;

public interface AreaService {

    BaseResponse<List<AreaResponse>> getAreas(String keyword, UUID parentId, String type);
}
