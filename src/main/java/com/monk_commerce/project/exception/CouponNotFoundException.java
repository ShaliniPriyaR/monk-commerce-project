package com.monk_commerce.project.exception;

public class CouponNotFoundException extends RuntimeException {
    public CouponNotFoundException(Long id) {
        super("Coupon with ID " + id + " not found");
    }
}
