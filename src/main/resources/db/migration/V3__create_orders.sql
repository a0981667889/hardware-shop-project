CREATE TABLE orders (
                        id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        user_id      BIGINT NOT NULL REFERENCES users(id),
                        status       VARCHAR(20)    NOT NULL DEFAULT 'PENDING',  -- PENDING / SHIPPED / COMPLETED
                        total_amount DECIMAL(10, 2) NOT NULL,
                        created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
                             id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             order_id     BIGINT NOT NULL REFERENCES orders(id),
                             component_id BIGINT NOT NULL REFERENCES components(id),
                             quantity     INT            NOT NULL,
                             unit_price   DECIMAL(10, 2) NOT NULL
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);