package com.example.Vi_vu_Viet.dto.Response.Booking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Chi tiết đơn của hãng, gồm chuyến và toàn bộ vé/hành khách")
public class CompanyBookingResponse extends BookingResponse {
    UUID companyId;
    UUID customerId;
    String cancellationReason;
    @Schema(description = "ID tài khoản admin thực hiện hủy; null với dữ liệu cũ hoặc hủy qua luồng khác")
    UUID cancelledBy;
}
