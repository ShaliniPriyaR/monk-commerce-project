package com.monk_commerce.project.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk_commerce.project.exception.CouponExpiredException;
import com.monk_commerce.project.exception.CouponInvalidException;
import com.monk_commerce.project.exception.CouponNotFoundException;
import com.monk_commerce.project.model.Coupon;
import com.monk_commerce.project.model.CouponStatus;
import com.monk_commerce.project.model.dto.CartRequest;
import com.monk_commerce.project.model.dto.CouponResponse;
import com.monk_commerce.project.repository.CouponRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepo;
    private final ObjectMapper mapper;

    public List<Coupon> getAll() {
        return couponRepo.findAll();
    }

    public Coupon get(Long id) {
         return couponRepo.findById(id).orElseThrow(() -> new CouponNotFoundException(id));
    }

    public Coupon create(Coupon c) {
        return couponRepo.save(c);
    }

    public Coupon update(Long id, Coupon c) {
        c.setId(id);
        return couponRepo.save(c);
    }

    public void delete(Long id) {
        Coupon coupon = couponRepo.findById(id).orElseThrow(() -> new CouponNotFoundException(id));
        if (coupon.getStatus() == CouponStatus.USED) {
            throw new CouponInvalidException("Used coupons cannot be deleted");
        }

        couponRepo.delete(coupon);
    }

    public CouponResponse toDTO(Coupon coupon) throws Exception {

        CouponResponse dto = new CouponResponse();
        dto.setId(coupon.getId());
        dto.setType(coupon.getType());
        dto.setStatus(coupon.getStatus());
        dto.setExpiryDate(coupon.getExpiryDate());

        if (coupon.getDetails() != null) {
            dto.setDetails(
                    mapper.readValue(coupon.getDetails(), Map.class)
            );
        }

        if (coupon.getConditions() != null) {
            dto.setConditions(
                    mapper.readValue(coupon.getConditions(), Map.class)
            );
        }

        return dto;
    }

    public double calculateCartTotal(CartRequest cart) {
        return cart.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
    }

    public double applyCartWise(Map<String, Object> details, CartRequest cart) {
        Number t = (Number) details.get("threshold");
        Number d = (Number) details.get("discount");

        double threshold = t.doubleValue();
        double discountPercent = d.doubleValue();
        double total = calculateCartTotal(cart);

        return total > threshold ? (total * discountPercent) / 100.0 : 0;
    }

    public double applyProductWise(Map<String, Object> details, CartRequest cart) {
        Long pid = Long.valueOf(details.get("product_id").toString());
        Number d = (Number) details.get("discount");
        double discountPercent = d.doubleValue();

        return cart.getItems().stream()
                .filter(i -> i.getProduct_id().equals(pid))
                .mapToDouble(i -> (i.getPrice() * i.getQuantity()) * (discountPercent / 100))
                .sum();
    }

    public double applyBxGy(Map<String, Object> details, CartRequest cart) {
        List<Map<String, Object>> buy = (List<Map<String, Object>>) details.get("buy_products");
        List<Map<String, Object>> get = (List<Map<String, Object>>) details.get("get_products");

        int requiredBuyQty = ((Number) buy.get(0).get("quantity")).intValue();
        int freeQty = ((Number) get.get(0).get("quantity")).intValue();

        long freeProductId = Long.parseLong(get.get(0).get("product_id").toString());

        int count = 0;
        for (CartRequest.Item item : cart.getItems()) {
            for (Map<String, Object> b : buy) {
                long buyId = Long.parseLong(b.get("product_id").toString());
                if (item.getProduct_id().equals(buyId)) {
                    count += item.getQuantity();
                }
            }
        }

        if (count < requiredBuyQty) return 0;

        for (CartRequest.Item item : cart.getItems()) {
            if (item.getProduct_id().equals(freeProductId)) {
                return freeQty * item.getPrice();
            }
        }
        return 0;
    }

    public double calculateDiscount(Coupon coupon, CartRequest cart) throws Exception {

        Map<String, Object> details = mapper.readValue(coupon.getDetails(), Map.class);

        return switch (coupon.getType()) {
            case CART_WISE -> applyCartWise(details, cart);
            case PRODUCT_WISE -> applyProductWise(details, cart);
            case BXGY -> applyBxGy(details, cart);
        };
    }

    public void validateCoupon(Coupon coupon, CartRequest cart) {
        if (coupon.getStatus() == CouponStatus.USED) {
            throw new CouponInvalidException("Coupon already used");
        }

        LocalDate today = LocalDate.now();
        if (coupon.getExpiryDate() != null &&
                coupon.getExpiryDate().isBefore(today)) {
            coupon.setStatus(CouponStatus.EXPIRED);
            couponRepo.save(coupon);
            throw new CouponExpiredException(coupon.getId());
        }

        if (coupon.getConditions() != null) {
            try {
                Map<String, Object> rules =
                        mapper.readValue(coupon.getConditions(), Map.class);

                if (rules.containsKey("min_items")) {
                    int min = ((Number) rules.get("min_items")).intValue();
                    int totalItems = cart.getItems().stream()
                            .mapToInt(i -> i.getQuantity()).sum();

                    if (totalItems < min) {
                        throw new CouponInvalidException("Cart has fewer items than required");
                    }
                }

            } catch (Exception e) {
                throw new CouponInvalidException("Invalid coupon conditions");
            }
        }
    }

    public boolean isCouponApplicable(Coupon coupon, CartRequest cart) {
        try {
            validateCoupon(coupon, cart);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
