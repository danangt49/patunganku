CREATE TABLE IF NOT EXISTS public.event (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DONE')),
    created_at  BIGINT NOT NULL,
    updated_at  BIGINT,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS public.member (
    id                BIGSERIAL PRIMARY KEY,
    event_id          BIGINT NOT NULL    REFERENCES public.event(id)    ON DELETE CASCADE,
    keycloak_user_id  VARCHAR(100) NOT NULL,
    created_at        BIGINT NOT NULL,
    updated_at        BIGINT,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),

    CONSTRAINT uk_member_event_user    UNIQUE (event_id, keycloak_user_id)
);

CREATE TABLE IF NOT EXISTS public.expense (
    id            BIGSERIAL PRIMARY KEY,
    event_id      BIGINT NOT NULL    REFERENCES public.event(id)    ON DELETE CASCADE,
    paid_by       BIGINT NOT NULL    REFERENCES public.member(id),
    description   VARCHAR(150) NOT NULL,
    amount        NUMERIC(12,2) NOT NULL,
    expense_date  BIGINT NOT NULL,
    created_at    BIGINT NOT NULL,
    updated_at    BIGINT,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS public.expense_split (
    id            BIGSERIAL PRIMARY KEY,
    expense_id    BIGINT NOT NULL REFERENCES public.expense(id) ON DELETE CASCADE,
    member_id     BIGINT NOT NULL REFERENCES public.member(id),
    share_amount  NUMERIC(12,2) NOT NULL,
    created_at    BIGINT NOT NULL,
    updated_at    BIGINT,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),

    CONSTRAINT uk_expense_split_expense_member UNIQUE (expense_id, member_id)
);