package com.example.Vi_vu_Viet.dto.Request.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Cập nhật hồ sơ; trường không gửi hoặc null được giữ nguyên")
public class UpdateProfileRequest {

    @Size(max = 160, message = "Họ tên tối đa 160 ký tự")
    @Pattern(regexp = "(?s).*\\S.*", message = "Họ tên không được để trống")
    String fullName;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 254, message = "Email tối đa 254 ký tự")
    @Pattern(regexp = "(?s).*\\S.*", message = "Email không được để trống")
    String email;

    @Pattern(regexp = "^(?:\\+[1-9]\\d{7,14}|0\\d{8,10})$",
            message = "Số điện thoại phải theo dạng quốc tế hoặc số nội địa bắt đầu bằng 0")
    String phone;

    public void setFullName(String fullName) {
        this.fullName = fullName == null ? null : fullName.trim();
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.replaceAll("[\\s()\\-]", "");
    }
}
