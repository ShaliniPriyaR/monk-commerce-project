package com.monk_commerce.project.model.dto;

import java.time.LocalDate;
import java.util.Map;

import com.monk_commerce.project.model.CouponStatus;
import com.monk_commerce.project.model.CouponType;

import lombok.Data;

@Data
public class CouponResponse {
    private Long id;
    private CouponType type;
    private CouponStatus status;
    private LocalDate expiryDate;
    private Map<String, Object> details;
    private Map<String, Object> conditions;

    //For Applicable Coupons
    private Double applicableDiscount;

    //For Apply Coupon
    private Double totalPrice;
    private Double finalPrice;
    private CartRequest updatedCart;
}
