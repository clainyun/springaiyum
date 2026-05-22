package com.ssafy.yumyum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ProblemDetail handleCustomException(CustomException e) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            status == null ? HttpStatus.BAD_REQUEST : status,
            e.getMessage()
        );
        detail.setProperty("code", "CUSTOM_EXCEPTION");
        log.warn("REST custom exception", e);
        return detail;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            e.getMessage()
        );
        detail.setProperty("code", "ILLEGAL_STATE");
        log.error("REST illegal state", e);
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "처리 중 오류가 발생했습니다."
        );
        detail.setProperty("code", "INTERNAL_SERVER_ERROR");
        log.error("REST unexpected exception", e);
        return detail;
    }
}
