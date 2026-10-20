CREATE TABLE goals (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    daily_target VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);