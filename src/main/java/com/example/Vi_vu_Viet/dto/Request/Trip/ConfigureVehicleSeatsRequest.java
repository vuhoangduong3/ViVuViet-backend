package com.example.Vi_vu_Viet.dto.Request.Trip;

import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Sơ đồ ghế của xe; tầng, hàng và cột bắt đầu từ 1")
public class ConfigureVehicleSeatsRequest {

    @NotBlank
    @Size(max = 120)
    String name;

    @NotEmpty
    @Size(max = 100)
    List<@NotNull @Valid SeatInput> seats;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SeatInput {

        @NotBlank
        @Size(max = 20)
        @Pattern(regexp = "[A-Za-z0-9_-]+", message = "Mã ghế chỉ gồm chữ, số, gạch ngang hoặc gạch dưới")
        String seatCode;

        @NotBlank
        @Size(max = 30)
        String seatClass = "STANDARD";

        @NotNull
        @Positive
        Integer deck;

        @NotNull
        @Positive
        Integer rowNo;

        @NotNull
        @Positive
        Integer columnNo;
    }
}
