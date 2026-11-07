package com.example.Vi_vu_Viet.Exception;

import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.ApplicationException;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
    private static final Map<Integer, HttpStatus> ERROR_CODE_TO_HTTP_STATUS = new HashMap<>();

    static {
        ERROR_CODE_TO_HTTP_STATUS.put(ApplicationCode.SUCCESS, HttpStatus.OK);
        ERROR_CODE_TO_HTTP_STATUS.put(ApplicationCode.UNKNOWN_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        ERROR_CODE_TO_HTTP_STATUS.put(ApplicationCode.USER_DEACTIVATE, HttpStatus.BAD_REQUEST);
        ERROR_CODE_TO_HTTP_STATUS.put(ApplicationCode.USER_EXITS, HttpStatus.CONFLICT);
        ERROR_CODE_TO_HTTP_STATUS.put(ApplicationCode.INPUT_INVALID, HttpStatus.UNAUTHORIZED);
        ERROR_CODE_TO_HTTP_STATUS.put(ApplicationCode.INVALID_PASSWORD, HttpStatus.UNAUTHORIZED);
        ERROR_CODE_TO_HTTP_STATUS.put(ApplicationCode.INVALID_TOKEN, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler({ApplicationException.class})
    public ResponseEntity<BaseResponse> handleResponseException(ApplicationException e, HttpServletRequest request) {
        log.error("Failed to handle request {}: {}", request.getRequestURI(), e.getMessage(), e);
        HttpStatus status = ERROR_CODE_TO_HTTP_STATUS.getOrDefault(e.getCode(), HttpStatus.OK);
        return ResponseEntity.status(status)
                .body(new BaseResponse(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new BaseResponse(
                        ApplicationCode.INVALID_TOKEN,
                        "Bạn không có quyền truy cập tài nguyên này"
                ));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<BaseResponse> handleResponseException(Exception e, HttpServletRequest request) {
        log.error("Failed to handle request " + request.getRequestURI() + ": " + e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse(ApplicationCode.UNKNOWN_ERROR, ApplicationCode.getMessage(ApplicationCode.UNKNOWN_ERROR)));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<BaseResponse> handleStatusException(ResponseStatusException e) {
        String message = e.getReason() == null ? e.getStatusCode().toString() : e.getReason();
        int code = switch (HttpStatus.valueOf(e.getStatusCode().value())) {
            case BAD_REQUEST -> ApplicationCode.INPUT_INVALID;
            case CONFLICT -> ApplicationCode.USER_EXITS;
            case UNAUTHORIZED, FORBIDDEN -> ApplicationCode.INVALID_TOKEN;
            default -> ApplicationCode.UNKNOWN_ERROR;
        };
        return ResponseEntity.status(e.getStatusCode())
                .body(new BaseResponse(code, message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new BaseResponse(ApplicationCode.USER_EXITS));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        BaseResponse baseResponse = new BaseResponse(ApplicationCode.INPUT_INVALID);
        baseResponse.setMessage(message);
        return ResponseEntity.badRequest().body(baseResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value for parameter: " + ex.getName();
        return ResponseEntity.badRequest()
                .body(new BaseResponse(ApplicationCode.INPUT_INVALID, message));
    }
}
