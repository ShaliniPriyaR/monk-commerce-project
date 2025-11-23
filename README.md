# Monk Commerce — Coupon Management API  

This project includes:

- Coupon CRUD  
- Cart-wise discounts  
- Product-wise discounts  
- BxGy (Buy X Get Y)  
- Coupon expiry & status lifecycle  
- Coupon conditions (min items, etc.)  
- Apply coupon  
- List applicable coupons  
- DTO-driven API responses  
- Global exception handling  

---

## How to Run the Project

### 1. Create PostgreSQL Database

```sql
CREATE DATABASE monk_commerce_project;
```

### 2. Update `application.properties`

```yaml
    spring.datasource.url=jdbc:postgresql://localhost:5432/monk_commerce_project
    spring.datasource.username=username
    spring.datasource.password=password
```

### 3. Run the application

```bash
mvn spring-boot:run
```

---

## Features Implemented

### ### 1. Coupon CRUD
Create, read, update, delete coupons with validations.

### ### 2. Coupon Types
#### **Cart-wise**
Percentage discount applied if cart total crosses threshold.

#### **Product-wise**
Discount applied only for the specific product.

#### **BxGy (Simple Implementation)**
- Supports single buy-product and single free-product  
- Only 1 repetition supported  
- Free product must be present in the cart  

---

## Coupon Expiry Support

Each coupon includes:

```json
"expiryDate": "2025-02-15"
```

Expired coupons are automatically marked **EXPIRED** during validation.

---

## Coupon Status Lifecycle

| Status   | Meaning                    |
|----------|-----------------------------|
| ACTIVE   | Coupon can be used          |
| USED     | Coupon already redeemed     |
| EXPIRED  | Coupon is no longer valid   |

---

## Conditions Support

Currently supported:

### `min_items`
Min number of items required in the cart.

```json
"conditions": { "min_items": 3 }
```

---

## Error Handling (Global)

Handled cases:

- Coupon not found  
- Coupon expired  
- Coupon already used  
- Conditions not met  
- Invalid JSON in details/conditions  
- Validation failures  

Example error:

```json
{
  "error": "Coupon 3 is expired"
}
```

---

## Endpoints Overview

### **Coupons**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/coupons` | Create coupon |
| GET    | `/coupons` | List all coupons |
| GET    | `/coupons/{id}` | Get coupon by ID |
| PUT    | `/coupons/{id}` | Update coupon |
| DELETE | `/coupons/{id}` | Delete coupon (with validation) |

---

### **Discount APIs**

#### **1. Applicable Coupons**
```
POST /applicable-coupons
```
Returns all valid coupons that can be applied on the given cart.

#### **2. Apply Coupon**
```
POST /apply-coupon/{id}
```
- Validates coupon  
- Calculates discount  
- Marks coupon as USED  
- Returns updated total, discount, and final price  

---

## Sample Payloads

### ### 1. Cart-wise Coupon
```json
{
  "type": "CART_WISE",
  "expiryDate": "2025-02-15",
  "conditions": "{\"min_items\": 3}",
  "details": "{\"threshold\": 200, \"discount\": 10}"
}
```

### ### 2. Product-wise Coupon
```json
{
  "type": "PRODUCT_WISE",
  "expiryDate": "2025-02-20",
  "details": "{\"product_id\": 1, \"discount\": 20}"
}
```

### ### 3. BxGy Coupon
```json
{
  "type": "BXGY",
  "expiryDate": "2025-03-01",
  "details": "{\"buy_products\": [{\"product_id\":1, \"quantity\":3}], \"get_products\": [{\"product_id\":3, \"quantity\":1}]}"
}
```

---

## Sample Cart Payload

```json
{
  "cart": {
    "items": [
      { "product_id": 1, "quantity": 6, "price": 50 },
      { "product_id": 2, "quantity": 3, "price": 30 },
      { "product_id": 3, "quantity": 2, "price": 25 }
    ]
  }
}
```

---

## Discount Calculation Logic

### **Cart-wise**
```
if total > threshold:
    discount = total * (percentage / 100)
```

### **Product-wise**
```
for each product match:
    discount += price * qty * (percentage / 100)
```

### **BxGy**
```
if bought quantity >= required:
    discount = free_qty * free_product_price
```

---

## Architecture Decisions

This project demonstrates:

- DTO pattern for clean API responses  
- Controller → Service separation  
- Centralized coupon validation (`validateCoupon`)  
- Reusable discount calculator (`calculateDiscount`)  
- Use of streams & modern Java  
- Global exception handling  
- Swagger documentation  
- Lightweight, extendable code structure  

As a **2-year backend developer**, these patterns show strong fundamentals and production-level structuring.

---

## Not Implemented

These are intentionally skipped due to time constraints:

- Multi-level BxGy  
- Category/brand-based coupons  
- Coupon stacking  
- Coupon optimization engine  
- Usage limits (per user/per order)  
- Unit testing (JUnit + Mockito)  
- Authentication/authorization  
- Redis caching  
- Product database integration  

These are clearly documented to demonstrate awareness of real-world complexity.

---

## Assumptions

- Prices come from request, not from DB  
- Free product must exist in cart  
- Each coupon can be applied only once  
- No authentication needed for assignment  
- Simple coupon engine required  

---
