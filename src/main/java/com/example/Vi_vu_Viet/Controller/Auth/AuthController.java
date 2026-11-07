package com.example.Vi_vu_Viet.Controller.Auth;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Service.Auth.AuthService;
import com.example.Vi_vu_Viet.dto.Request.Auth.LoginRequest;
import com.example.Vi_vu_Viet.dto.Request.Auth.RegisterRequest;
import com.example.Vi_vu_Viet.dto.Request.Auth.RefreshRequest;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Response.Auth.MeResponse;
import com.example.Vi_vu_Viet.dto.Response.Auth.TokenResponse;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthController {

    AuthService authService;

    @Operation(summary = "Đăng ký", description = "Tạo tài khoản khách hàng. Quyền hãng xe không được nhận từ request đăng ký.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng ký thành công"),
            @ApiResponse(responseCode = "400", description = "Thông tin không hợp lệ"),
            @ApiResponse(responseCode = "409", description = "Email hoặc số điện thoại đã được sử dụng")
    })
    @PostMapping("/auth/register")
    public BaseResponse<MeResponse> register(@Valid @RequestBody BaseRequest<RegisterRequest> request) {
        return authService.register(request);
    }

    @Operation(summary = "Đăng nhập", description = "Đăng nhập bằng email hoặc số điện thoại và mật khẩu.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Thông tin đăng nhập không đúng")
    })
    @PostMapping("/auth/login")
    public BaseResponse<TokenResponse> login(@Valid @RequestBody BaseRequest<LoginRequest> request) {
        return authService.login(request);
    }

    @Operation(summary = "Làm mới token", description = "Xoay refresh token; token cũ bị thu hồi sau khi làm mới thành công.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Làm mới token thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ hoặc hết hạn")
    })
    @PostMapping("/auth/refresh")
    public BaseResponse<TokenResponse> refresh(@Valid @RequestBody BaseRequest<RefreshRequest> request) {
        return authService.refresh(request);
    }

    @Operation(summary = "Đăng xuất", description = "Thu hồi phiên và đưa access token hiện tại vào blacklist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ")
    })
    @PostMapping("/auth/logout")
    public BaseResponse<Void> logout(@RequestHeader("Authorization") String authorization) {
        String accessToken = authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : "";
        return authService.logout(accessToken);
    }

    @Operation(
            summary = "Thông tin tài khoản hiện tại",
            description = "Lấy thông tin của người dùng từ access token.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "401", description = "Access token thiếu, không hợp lệ hoặc hết hạn")
    })
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public BaseResponse<MeResponse> getMe(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAccount user,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return authService.getMe(
                user,
                authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .toList()
        );
    }
}
