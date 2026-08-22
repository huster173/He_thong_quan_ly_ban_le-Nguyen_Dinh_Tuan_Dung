package com.shop.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST),
    INVALID_PAGE(HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_ID(HttpStatus.BAD_REQUEST),
    EMPTY_ORDER(HttpStatus.BAD_REQUEST),
    INVALID_COUPON(HttpStatus.BAD_REQUEST),
    INVALID_REGION(HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK(HttpStatus.CONFLICT),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
