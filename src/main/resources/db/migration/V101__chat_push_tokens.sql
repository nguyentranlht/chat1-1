-- =====================================================================
-- Thong bao day (push) qua Firebase Cloud Messaging cho Chat.
-- Moi trinh duyet / thiet bi co mot FCM token. Mot nguoi co the co nhieu token.
-- Token la duy nhat: khi nguoi khac dang nhap tren cung trinh duyet thi token chuyen sang nguoi do.
-- =====================================================================
CREATE TABLE chat_push_tokens (
    token       VARCHAR(512) PRIMARY KEY,
    user_id     UUID         NOT NULL,
    platform    VARCHAR(10)  NOT NULL DEFAULT 'WEB',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_chat_push_platform CHECK (platform IN ('WEB', 'ANDROID', 'IOS'))
);

CREATE INDEX idx_chat_push_tokens_user ON chat_push_tokens (user_id);
