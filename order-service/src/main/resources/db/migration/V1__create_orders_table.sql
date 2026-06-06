CREATE TABLE orders
(
    id                BIGSERIAL       NOT NULL,
    order_id          UUID            NOT NULL,
    customer_id       UUID            NOT NULL,
    status            VARCHAR(20)     NOT NULL,
    total_amount      NUMERIC(19, 4)  NOT NULL,
    currency          VARCHAR(3)      NOT NULL,
    payment_intent_id VARCHAR(255),
    street            VARCHAR(255)    NOT NULL,
    city              VARCHAR(255)    NOT NULL,
    postal_code       VARCHAR(20)     NOT NULL,
    country           VARCHAR(100)    NOT NULL,
    created_at        TIMESTAMPTZ     NOT NULL,
    updated_at        TIMESTAMPTZ     NOT NULL,
    expires_at        TIMESTAMPTZ     NOT NULL,
    version           BIGINT          NOT NULL DEFAULT 0,

    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT uq_orders_order_id UNIQUE (order_id)
);

CREATE TABLE order_items
(
    id           BIGSERIAL       NOT NULL,
    order_id     BIGINT          NOT NULL,
    product_id   UUID            NOT NULL,
    product_name VARCHAR(255)    NOT NULL,
    quantity     INT             NOT NULL,
    unit_price   NUMERIC(19, 4)  NOT NULL,

    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE INDEX idx_orders_order_id        ON orders (order_id);
CREATE INDEX idx_orders_customer_id     ON orders (customer_id);
CREATE INDEX idx_orders_status_expires  ON orders (status, expires_at);
CREATE INDEX idx_order_items_order_id   ON order_items (order_id);
