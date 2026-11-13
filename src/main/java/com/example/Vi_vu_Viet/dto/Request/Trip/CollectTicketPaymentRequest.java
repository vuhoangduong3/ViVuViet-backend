package com.example.Vi_vu_Viet.dto.Request.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectTicketPaymentRequest {

    @NotNull
    @Positive
    @Schema(description = "Số tiền mặt thực tế đã thu của vé, đơn vị VND; ghi nhận một lần, không cộng dồn",
            example = "250000")
    Long collectedAmount;
}
