-- =====================================================
-- Таблица документов (documents)
-- =====================================================
-- Хранит медицинские документы, загруженные пользователями.
-- Поддерживает офлайн-синхронизацию через поля local_id и is_synced.
-- =====================================================

CREATE TABLE IF NOT EXISTS documents (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    local_id TEXT NOT NULL UNIQUE,
    user_id INTEGER NOT NULL,
    
    -- Основная информация
    title TEXT,
    document_type TEXT,
    content TEXT,
    image_url TEXT,
    
    -- Метаданные для синхронизации
    is_synced INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT,
    last_sync_at TEXT,
    deleted_at TEXT,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);