package com.example.Vi_vu_Viet.Util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApplicationException extends RuntimeException {
    private int code;
    private String message;
    private String language;

    public ApplicationException(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        if (!StringUtils.hasText(message)) {
            return ApplicationCode.getMessage(code);
        }
        return message;
    }

}