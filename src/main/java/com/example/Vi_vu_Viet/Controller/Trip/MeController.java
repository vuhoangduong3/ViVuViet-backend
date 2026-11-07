package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Service.Trip.CompanyService;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.MyCompanyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MeController {

    CompanyService companyService;

    @Operation(
            summary = "Hãng xe của tôi",
            description = "Trả về các hãng đang hoạt động mà người dùng hiện tại là thành viên, cùng vai trò tại từng hãng.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách hãng thành công"),
            @ApiResponse(responseCode = "401", description = "Access token không hợp lệ hoặc đã hết hạn")
    })
    @GetMapping("/companies")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public BaseResponse<List<MyCompanyResponse>> getMyCompanies(
            @AuthenticationPrincipal UserAccount user
    ) {
        return companyService.getMyCompanies(user.getId());
    }
}
