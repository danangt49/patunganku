CREATE TABLE IF NOT EXISTS public.settlement_plan (
    id           BIGSERIAL PRIMARY KEY,
    event_id     BIGINT NOT NULL,
    generated_at BIGINT,
    created_at   BIGINT NOT NULL,
    updated_at   BIGINT,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS public.settlement_transaction (
    id                BIGSERIAL PRIMARY KEY,
    plan_id           BIGINT NOT NULL REFERENCES public.settlement_plan(id) ON DELETE CASCADE,
    from_member_id    BIGINT NOT NULL,
    from_member_name  VARCHAR(100) NOT NULL,
    to_member_id      BIGINT NOT NULL,
    to_member_name    VARCHAR(100) NOT NULL,
    amount            NUMERIC(12,2) NOT NULL,
    is_paid           BOOLEAN NOT NULL DEFAULT FALSE,
    paid_at           BIGINT,
    created_at        BIGINT NOT NULL,
    updated_at        BIGINT,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100)
);