package com.example.Vi_vu_Viet.Service.Auth;

import com.example.Vi_vu_Viet.dto.Request.Auth.UpdateProfileRequest;
import com.example.Vi_vu_Viet.dto.Response.Auth.ProfileResponse;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;

import java.util.UUID;

public interface ProfileService {
    BaseResponse<ProfileResponse> getProfile(UUID userId);

    BaseResponse<ProfileResponse> updateProfile(UUID userId, UpdateProfileRequest request);
}
