package com.example.Vi_vu_Viet.dto.Response.Location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Địa điểm trong danh mục tìm kiếm")
public class AreaResponse {

    UUID id;

    UUID parentId;

    String code;

    String name;

    String type;
}
