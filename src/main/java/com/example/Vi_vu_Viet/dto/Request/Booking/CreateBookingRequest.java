package com.example.Vi_vu_Viet.dto.Request.Booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin giữ ghế và đặt vé cho một chuyến")
public class CreateBookingRequest {

    @NotNull
    UUID tripId;

    @NotBlank
    @Size(max = 120)
    String contactName;

    @NotBlank
    @Size(max = 30)
    String contactPhone;

    @Email
    @Size(max = 255)
    String contactEmail;

    @NotEmpty
    @Size(max = 8)
    List<@NotNull @Valid PassengerInput> passengers;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PassengerInput {

        @NotBlank
        @Size(max = 120)
        String passengerName;

        @Size(max = 30)
        String passengerPhone;

        @NotBlank
        @Size(max = 500)
        String pickupAddress;

        @NotBlank
        @Size(max = 500)
        String dropoffAddress;

        @NotNull
        UUID seatId;
    }
}
