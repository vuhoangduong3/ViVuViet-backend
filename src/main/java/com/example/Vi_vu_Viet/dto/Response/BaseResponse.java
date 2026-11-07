package com.example.Vi_vu_Viet.dto.Response;

import com.example.Vi_vu_Viet.Util.ApplicationCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BaseResponse<T>{

    Integer code;

    String message;

    T wsResponse;

    public BaseResponse() {
    }

    public BaseResponse(Integer code) {
        this.code = code;
        this.message = ApplicationCode.getMessage(code);
    }

    public BaseResponse(Integer errorCode, String message) {
        this.code = errorCode;
        this.message = message;
    }
}
