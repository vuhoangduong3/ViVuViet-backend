package com.example.Vi_vu_Viet.dto.Response.Location;

import com.example.Vi_vu_Viet.Util.Enums.StopStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Điểm đón/trả của hãng xe")
public class StopResponse {

    UUID id;

    UUID companyId;

    UUID areaId;

    String areaCode;

    String areaName;

    String name;

    String address;

    String pickupInstructions;

    StopStatus status;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
