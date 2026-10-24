CREATE TABLE lost_item (
    id            uuid PRIMARY KEY,
    business_key  text NOT NULL UNIQUE,
    status        text NOT NULL,
    title         text NOT NULL,
    created_at    timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT lost_item_status_known
        CHECK (status IN ('REPORTED', 'FOUND', 'RETURNED'))
);
