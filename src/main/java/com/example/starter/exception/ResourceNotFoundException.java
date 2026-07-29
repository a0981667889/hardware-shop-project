package com.example.starter.exception;

import com.example.starter.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 找不到資源時拋出
 * 對應 HTTP 404
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(ErrorCode errorCode, Long id) {
        super(errorCode, HttpStatus.NOT_FOUND,
                errorCode.getDefaultMessage() + "，ID: " + id);
    }
}