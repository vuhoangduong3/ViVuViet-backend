package com.example.Vi_vu_Viet.dto.Request.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin đăng ký tài khoản")
public class RegisterRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 160, message = "Họ tên tối đa 160 ký tự")
    String fullName;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 254, message = "Email tối đa 254 ký tự")
    String email;

    @Pattern(
            regexp = "^(?:\\+[1-9]\\d{7,14}|0\\d{8,10})$",
            message = "Số điện thoại phải theo dạng quốc tế hoặc số nội địa bắt đầu bằng 0"
    )
    String phone;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(max = 72, message = "Mật khẩu tối đa 72 ký tự")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$",
            message = "Mật khẩu cần ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    @Schema(description = "Ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt")
    String password;
}
