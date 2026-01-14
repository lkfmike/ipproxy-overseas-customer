package com.ipproxy.overseas.customer.exception;

import com.ipproxy.overseas.customer.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PurchaseException.class)
    public ApiResponse<Void> handlePurchaseException(PurchaseException ex) {
        return ApiResponse.error(400, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<Void> handleUnauthorized(UnauthorizedException ex) {
        return ApiResponse.error(401, ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ApiResponse<Void> handleBadRequest(Exception ex) {
        String message = ex.getMessage();
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException e = (MethodArgumentNotValidException) ex;
            if (e.getBindingResult().getFieldError() != null) {
                message = e.getBindingResult().getFieldError().getDefaultMessage();
            } else {
                message = "参数错误";
            }
        } else if (ex instanceof BindException) {
            BindException e = (BindException) ex;
            if (e.getBindingResult().getFieldError() != null) {
                message = e.getBindingResult().getFieldError().getDefaultMessage();
            } else {
                message = "参数错误";
            }
        } else if (ex instanceof HttpMessageNotReadableException) {
            message = "参数错误";
        }
        return ApiResponse.error(400, message == null ? "参数错误" : message);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleServerError(Exception ex) {
        log.error("服务器错误", ex);
        return ApiResponse.error(500, "服务器错误");
    }
}

