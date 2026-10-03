-- =====================================================================
-- Module Chat 1-1 (HR Connect)
-- Chu so huu: thanh vien phu trach Chat. Dai version Flyway danh cho Chat: V100 - V199
--
-- Ghi chu: chua co khoa ngoai toi bang users vi Core chua xong.
-- Khi ghep voi Core, them migration rieng (vd. V1xx__chat_fk_users.sql) de gan FK.
-- =====================================================================

-- Moi cap nguoi dung co DUNG MOT hoi thoai.
-- Quy uoc: user_a_id < user_b_id (so sanh uuid cua PostgreSQL), nen A->B va B->A cung tro ve mot dong.
CREATE TABLE chat_conversations (
    id               UUID        PRIMARY KEY,
    user_a_id        UUID        NOT NULL,
    user_b_id        UUID        NOT NULL,
    last_message_id  UUID,
    last_message_at  TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_chat_pair_order CHECK (user_a_id < user_b_id),
    CONSTRAINT uq_chat_pair        UNIQUE (user_a_id, user_b_id)
);

-- Trang thai rieng cua tung nguoi trong hoi thoai (moi hoi thoai dung 2 dong).
CREATE TABLE chat_participants (
    conversation_id       UUID        NOT NULL REFERENCES chat_conversations (id) ON DELETE CASCADE,
    user_id               UUID        NOT NULL,
    last_read_message_id  UUID,
    hidden_at             TIMESTAMPTZ,
    is_muted              BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (conversation_id, user_id)
);

CREATE TABLE chat_messages (
    id               UUID         PRIMARY KEY,
    conversation_id  UUID         NOT NULL REFERENCES chat_conversations (id) ON DELETE CASCADE,
    sender_id        UUID         NOT NULL,
    content          TEXT         NOT NULL,
    type             VARCHAR(10)  NOT NULL DEFAULT 'TEXT',
    recalled_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_chat_message_type CHECK (type IN ('TEXT', 'IMAGE', 'FILE'))
);

-- Tai lich su theo trang (cursor) va danh sach hoi thoai cua mot nguoi
CREATE INDEX idx_chat_messages_conv_time ON chat_messages (conversation_id, created_at DESC, id DESC);
CREATE INDEX idx_chat_participants_user  ON chat_participants (user_id);
CREATE INDEX idx_chat_conversations_b    ON chat_conversations (user_b_id);
