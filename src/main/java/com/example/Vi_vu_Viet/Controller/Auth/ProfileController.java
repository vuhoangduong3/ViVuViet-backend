package com.example.Vi_vu_Viet.Controller.Auth;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Service.Auth.ProfileService;
import com.example.Vi_vu_Viet.dto.Request.Auth.UpdateProfileRequest;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Response.Auth.ProfileResponse;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/profile")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {
    private final ProfileService profileService;

    @Operation(summary = "Hồ sơ của tôi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hồ sơ hiện tại"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping
    public BaseResponse<ProfileResponse> getProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount customer) {
        return profileService.getProfile(customer.getId());
    }

    @Operation(summary = "Cập nhật hồ sơ của tôi",
            description = "Cập nhật họ tên, email hoặc số điện thoại. Trường không gửi hoặc null được giữ nguyên.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hồ sơ đã cập nhật"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc không có trường cập nhật"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "409", description = "Email hoặc số điện thoại đã được sử dụng")
    })
    @PatchMapping
    public BaseResponse<ProfileResponse> updateProfile(
            @Valid @RequestBody BaseRequest<UpdateProfileRequest> request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount customer) {
        return profileService.updateProfile(customer.getId(), request.getWsRequest());
    }
}
