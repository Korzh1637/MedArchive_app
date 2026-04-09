-- =====================================================
-- Таблица пользователей (users)
-- =====================================================
-- Хранит информацию о зарегистрированных пользователях.
-- Пароль хранится в виде хеша.
-- =====================================================

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,

    -- Метаданные
    is_synced INTEGER DEFAULT 0,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT,
    last_login_at TEXT,
    is_active INTEGER DEFAULT 1
);