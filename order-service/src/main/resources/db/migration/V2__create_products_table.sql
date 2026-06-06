CREATE TABLE products
(
    id          BIGSERIAL       NOT NULL,
    product_id  UUID            NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    description TEXT,
    price       NUMERIC(19, 4)  NOT NULL,
    currency    VARCHAR(3)      NOT NULL,
    available   BOOLEAN         NOT NULL DEFAULT true,

    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uq_products_product_id UNIQUE (product_id)
);
