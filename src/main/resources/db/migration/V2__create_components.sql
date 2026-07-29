CREATE TABLE components (
                            id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            name        VARCHAR(200)   NOT NULL,
                            category    VARCHAR(50)    NOT NULL,   -- CPU / MOTHERBOARD / GPU / RAM / PSU / CASE
                            brand       VARCHAR(100),
                            price       DECIMAL(10, 2) NOT NULL,
                            stock       INT            NOT NULL DEFAULT 0,
                            socket      VARCHAR(50),               -- CPU / 主機板用，例如 LGA1700
                            power_watt  INT,                       -- GPU 建議瓦數 / PSU 供應瓦數
                            created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_components_category ON components(category);