CREATE TABLE tracking_entries
(
    id          BIGSERIAL    NOT NULL,
    tracking_id UUID         NOT NULL,
    order_id    UUID         NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    location    VARCHAR(255),
    note        VARCHAR(1000),
    recorded_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_tracking_entries PRIMARY KEY (id),
    CONSTRAINT uq_tracking_entries_tracking_id UNIQUE (tracking_id)
);

CREATE INDEX idx_tracking_entries_order_id ON tracking_entries (order_id);
