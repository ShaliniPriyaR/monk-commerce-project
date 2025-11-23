CREATE TABLE IF NOT EXISTS coupon (
                                      id serial PRIMARY KEY,
                                      name text,
                                      type text NOT NULL,
                                      details text,
                                      created_at timestamptz DEFAULT now()
    );

-- Example cart-wise coupon: 10% off cart > 100
INSERT INTO coupon (name, type, details)
VALUES ('CART10OVER100', 'CART_WISE', '{"threshold":100.0,"discount_percent":10.0}');
