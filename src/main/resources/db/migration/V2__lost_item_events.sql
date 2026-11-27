CREATE TABLE lost_item_event (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id uuid NOT NULL REFERENCES lost_item(id) ON DELETE CASCADE,
    actor text NOT NULL,
    from_status text NOT NULL,
    to_status text NOT NULL,
    happened_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX lost_item_event_item_idx ON lost_item_event(item_id);
