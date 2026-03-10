-- =====================================================
-- Таблица записей здоровья (health_entries)
-- =====================================================
-- Хранит показатели: давление, сахар, боль и т.д.
-- =====================================================

CREATE TABLE IF NOT EXISTS health_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    local_id TEXT NOT NULL UNIQUE,
    user_id INTEGER NOT NULL,
    
    entry_type TEXT NOT NULL,
    value1 NUMERIC,
    value2 NUMERIC,
    value3 NUMERIC,
    unit TEXT,
    notes TEXT,
    
    entry_date TEXT NOT NULL,
    is_synced INTEGER DEFAULT 0,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT,
    deleted_at TEXT,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);