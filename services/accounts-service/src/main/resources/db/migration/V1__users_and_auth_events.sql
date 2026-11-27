CREATE TABLE users (
    id uuid PRIMARY KEY,
    username varchar(40) NOT NULL UNIQUE,
    display_name varchar(80) NOT NULL,
    password_hash text NOT NULL,
    role varchar(20) NOT NULL CHECK (role IN ('FINDER', 'CUSTODIAN')),
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE auth_event (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id),
    action varchar(30) NOT NULL,
    happened_at timestamptz NOT NULL DEFAULT now()
);
