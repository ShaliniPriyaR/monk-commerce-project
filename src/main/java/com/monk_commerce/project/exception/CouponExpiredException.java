package com.monk_commerce.project.exception;

public class CouponExpiredException extends RuntimeException {
    public CouponExpiredException(Long id) {
        super("Coupon " + id + " is expired");
    }
}
