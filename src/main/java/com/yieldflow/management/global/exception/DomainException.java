package com.yieldflow.management.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DomainException extends RuntimeException {

    HttpStatus status;
    String code;

    public DomainException(DomainExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.status = exceptionCode.getStatus();
        this.code = exceptionCode.name();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
