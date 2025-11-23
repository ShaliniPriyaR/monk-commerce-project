package com.monk_commerce.project.model;


import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CouponType type;

    @Enumerated(EnumType.STRING)
    private CouponStatus status = CouponStatus.ACTIVE;

    private LocalDate expiryDate;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(columnDefinition = "TEXT")
    private String conditions;
}
