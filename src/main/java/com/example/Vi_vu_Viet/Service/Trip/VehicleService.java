package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.Util.Enums.VehicleStatus;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateVehicleRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateVehicleRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.VehicleResponse;

import java.util.List;
import java.util.UUID;

public interface VehicleService {
    BaseResponse<List<VehicleResponse>> getVehicles(UUID companyId, String q, VehicleStatus status);
    BaseResponse<VehicleResponse> createVehicle(UUID companyId, CreateVehicleRequest request);
    BaseResponse<VehicleResponse> getVehicle(UUID companyId, UUID vehicleId);
    BaseResponse<VehicleResponse> updateVehicle(UUID companyId, UUID vehicleId, UpdateVehicleRequest request);
    BaseResponse<Void> deleteVehicle(UUID companyId, UUID vehicleId);
}
