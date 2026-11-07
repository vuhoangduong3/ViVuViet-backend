package com.example.Vi_vu_Viet.Service.Auth;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.dto.Request.Auth.LoginRequest;
import com.example.Vi_vu_Viet.dto.Request.Auth.RegisterRequest;
import com.example.Vi_vu_Viet.dto.Request.Auth.RefreshRequest;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Response.Auth.MeResponse;
import com.example.Vi_vu_Viet.dto.Response.Auth.TokenResponse;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;

import java.util.List;

public interface AuthService {

    BaseResponse<MeResponse> register(BaseRequest<RegisterRequest> request);

    BaseResponse<TokenResponse> login(BaseRequest<LoginRequest> request);

    BaseResponse<TokenResponse> refresh(BaseRequest<RefreshRequest> request);

    BaseResponse<Void> logout(String accessToken);

    BaseResponse<MeResponse> getMe(UserAccount user, List<String> authorities);
}
