ALTER TABLE analytics_event
    ADD COLUMN event_id varchar(64),
    ADD COLUMN occurred_at timestamptz;

UPDATE analytics_event
SET event_id = 'legacy-' || id,
    occurred_at = received_at;

ALTER TABLE analytics_event
    ALTER COLUMN event_id SET NOT NULL,
    ALTER COLUMN occurred_at SET NOT NULL,
    ALTER COLUMN received_at SET NOT NULL;

CREATE UNIQUE INDEX analytics_event_event_id_uq ON analytics_event(event_id);
DROP INDEX events_idx;
CREATE INDEX events_idx ON analytics_event(receiver_id, event_type, occurred_at DESC);
