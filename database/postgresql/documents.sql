-- =====================================================
-- Таблица документов (documents)
-- =====================================================
-- Хранит медицинские документы, загруженные пользователями.
-- Поддерживает офлайн-синхронизацию через поля local_id и is_synced.
-- =====================================================

CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    local_id VARCHAR(100) NOT NULL UNIQUE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Основная информация
    title VARCHAR(20),
    document_type VARCHAR(50),
    content TEXT,
    image_path TEXT,
    image_url TEXT,
    
    -- Метаданные для синхронизации
    is_synced BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    last_sync_at TIMESTAMP WITH TIME ZONE, 
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT f_key_for_id_users FOREIGN KEY (user_id) REFERENCES users(id)
);

COMMENT ON TABLE documents IS 'Медицинские документы';
COMMENT ON COLUMN documents.local_id IS 'Уникальный идентификатор на устройстве';
COMMENT ON COLUMN documents.user_id IS 'Владелец документа';
COMMENT ON COLUMN documents.title IS 'Анализ/заключение';
COMMENT ON COLUMN documents.document_type IS 'Тип документа';
COMMENT ON COLUMN documents.content IS 'Распознанный текст';
COMMENT ON COLUMN documents.image_url IS 'Ссылка на изображение';
COMMENT ON COLUMN documents.is_synced IS 'Флаг синхронизации с сервером';