package com.monk_commerce.project.model.dto;

import java.util.List;

import lombok.*;

@Data
public class CartRequest {
    private List<Item> items;

    @Data
    public static class Item {
        private Long product_id;
        private int quantity;
        private double price;
    }
}
