-- =====================================================
-- Таблица пользователей (users)
-- =====================================================
-- Хранит информацию о зарегистрированных пользователях.
-- Пароль хранится в виде хеша.
-- =====================================================

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,

    -- Метаданные
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE
);

COMMENT ON TABLE users IS 'Пользователи системы';
COMMENT ON COLUMN users.id IS 'ID пользователя';
COMMENT ON COLUMN users.email IS 'Email пользователя (логин)';
COMMENT ON COLUMN users.password_hash IS 'Хеш пароля';
COMMENT ON COLUMN users.full_name IS 'Полное имя пользователя';
COMMENT ON COLUMN users.created_at IS 'Дата регистрации';
COMMENT ON COLUMN users.updated_at IS 'Дата последнего обновления профиля';
COMMENT ON COLUMN users.last_login_at IS 'Дата последнего входа';
COMMENT ON COLUMN users.is_active IS 'Активен ли пользователь';