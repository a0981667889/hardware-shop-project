CREATE TABLE cart_items (
                            id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            user_id      BIGINT NOT NULL REFERENCES users(id),
                            component_id BIGINT NOT NULL REFERENCES components(id),
                            quantity     INT    NOT NULL,
                            created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE (user_id, component_id)
);

CREATE INDEX idx_cart_items_user_id ON cart_items(user_id);