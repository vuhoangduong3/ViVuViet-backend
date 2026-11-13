package com.example.Vi_vu_Viet.dto.Response.Booking;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Số đơn theo tab, áp dụng các bộ lọc trừ viewStatus và không phân trang. " +
        "all gồm cả holding/expired; năm tab nghiệp vụ chỉ tính đơn đã xác nhận hoặc đã hủy.")
public record CompanyBookingStatusCountsResponse(long all, long booked, long pickedUp, long completed,
                                                 long noShow, long cancelled, long holding, long expired) { }
