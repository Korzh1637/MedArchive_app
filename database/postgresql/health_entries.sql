-- =====================================================
-- Таблица записей здоровья (health_entries)
-- =====================================================
-- Хранит показатели: давление, сахар, боль и т.д.
-- =====================================================

CREATE TABLE health_entries (
    id SERIAL PRIMARY KEY,
    local_id VARCHAR(100) NOT NULL UNIQUE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    entry_type VARCHAR(20) NOT NULL,
    value1 DECIMAL(10,2),
    value2 DECIMAL(10,2),
    value3 DECIMAL(10,2),
    unit VARCHAR(10),
    notes TEXT,
    
    entry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT f_key_health_for_user FOREIGN KEY (user_id) REFERENCES users(id)
);

COMMENT ON TABLE health_entries IS 'Записи дневника здоровья';
COMMENT ON COLUMN health_entries.entry_type IS 'Тип показателя: давление, сахар, боль';
COMMENT ON COLUMN health_entries.value1 IS 'Основное значение';
COMMENT ON COLUMN health_entries.value2 IS 'Первое доп значение (для давления)';
COMMENT ON COLUMN health_entries.value3 IS 'Второе доп значение (для давления)';
COMMENT ON COLUMN health_entries.unit IS 'Единица измерения';
COMMENT ON COLUMN health_entries.entry_date IS 'Дата и время замера';