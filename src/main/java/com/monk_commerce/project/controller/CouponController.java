package com.monk_commerce.project.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk_commerce.project.exception.CouponInvalidException;
import com.monk_commerce.project.model.Coupon;
import com.monk_commerce.project.model.CouponStatus;
import com.monk_commerce.project.model.dto.CartRequest;
import com.monk_commerce.project.model.dto.CouponResponse;
import com.monk_commerce.project.service.CouponService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CouponController {
    private final CouponService service;
    private final ObjectMapper mapper;

    @PostMapping("/coupons")
    public CouponResponse create(@RequestBody Coupon c) throws Exception {
        return service.toDTO(service.create(c));
    }

    @GetMapping("/coupons")
    public List<CouponResponse> getAll() throws Exception {
        List<CouponResponse> list = new ArrayList<>();
        for (Coupon coupon : service.getAll()) {
            list.add(service.toDTO(coupon));
        }
        return list;
    }

    @GetMapping("/coupons/{id}")
    public CouponResponse get(@PathVariable Long id) throws Exception {
        return service.toDTO(service.get(id));
    }

    @PutMapping("/coupons/{id}")
    public CouponResponse update(@PathVariable Long id, @RequestBody Coupon c) throws Exception {
        return service.toDTO(service.update(id, c));
    }

    @DeleteMapping("/coupons/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/applicable-coupons")
    public List<CouponResponse> applicable(@RequestBody Map<String, Object> req) throws Exception {

       CartRequest cart = mapper.convertValue(req.get("cart"), CartRequest.class);
        List<CouponResponse> result = new ArrayList<>();
        for (Coupon coupon : service.getAll()) {

            if (!service.isCouponApplicable(coupon, cart)) {
                continue;
            }

            double discount = service.calculateDiscount(coupon, cart);

            if (discount > 0) {
                CouponResponse dto = service.toDTO(coupon);
                dto.setApplicableDiscount(discount);
                result.add(dto);
            }
        }
        return result;
    }


    @PostMapping("/apply-coupon/{id}")
    public CouponResponse applyCoupon(@PathVariable Long id, @RequestBody Map<String, Object> req) throws Exception {

        CartRequest cart = mapper.convertValue(req.get("cart"), CartRequest.class);
        Coupon coupon = service.get(id);

        service.validateCoupon(coupon, cart);

        double discount = service.calculateDiscount(coupon, cart);

        if (discount == 0) {
            throw new CouponInvalidException("Coupon conditions not met");
        }

        double totalPrice = service.calculateCartTotal(cart);
        double finalPrice = totalPrice - discount;

        coupon.setStatus(CouponStatus.USED);
        service.update(id, coupon);

        CouponResponse dto = service.toDTO(coupon);
        dto.setApplicableDiscount(discount);
        dto.setTotalPrice(totalPrice);
        dto.setFinalPrice(finalPrice);
        dto.setUpdatedCart(cart);

        return dto;
    }
}
