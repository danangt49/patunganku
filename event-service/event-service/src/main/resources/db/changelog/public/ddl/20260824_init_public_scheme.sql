CREATE TABLE IF NOT EXISTS public.event (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    created_by  VARCHAR(100) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DONE')),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.member (
    id          BIGSERIAL PRIMARY KEY,
    event_id    BIGINT NOT NULL REFERENCES event(id),
    name        VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.expense (
    id          BIGSERIAL PRIMARY KEY,
    event_id    BIGINT NOT NULL REFERENCES event(id),
    paid_by     BIGINT NOT NULL REFERENCES member(id),
    description VARCHAR(150) NOT NULL,
    amount      NUMERIC(12,2) NOT NULL,
    expense_date DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS public.expense_split (
    id          BIGSERIAL PRIMARY KEY,
    expense_id  BIGINT NOT NULL REFERENCES expense(id),
    member_id   BIGINT NOT NULL REFERENCES member(id),
    share_amount NUMERIC(12,2) NOT NULL
);