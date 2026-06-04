CREATE TABLE processed_events
(
    id           BIGSERIAL   NOT NULL,
    event_id     UUID        NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_processed_events PRIMARY KEY (id),
    CONSTRAINT uq_processed_events_event_id UNIQUE (event_id)
);
