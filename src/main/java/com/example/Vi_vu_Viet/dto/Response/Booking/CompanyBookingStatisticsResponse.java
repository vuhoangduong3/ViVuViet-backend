package com.example.Vi_vu_Viet.dto.Response.Booking;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Thống kê toàn hãng. Số vé và tổng giá trị chỉ tính vé CONFIRMED trong đơn CONFIRMED, " +
        "trên toàn bộ thời gian; không phải tiền thực thu. Số đơn hôm nay tính theo ngày khởi hành, gồm mọi trạng thái.")
public record CompanyBookingStatisticsResponse(LocalDate date, String timezone, long todayBookings,
                                               long bookedTickets, long totalTicketAmount, String currency) { }
