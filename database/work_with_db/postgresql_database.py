# database/postgresql_database.py
import psycopg2
from psycopg2.extras import RealDictCursor
from contextlib import contextmanager
from pathlib import Path
from datetime import datetime
from typing import List, Dict, Optional, Any
import sys

sys.path.insert(0, str(Path(__file__).parent.parent.parent))

# from database.models.users import User
# from database.models.documents import Document
# from database.models.health_entries import HealthEntry


class PostgreSQLDatabase:
    '''
    Класс для работы с PostgreSQL на сервере.
    Предоставляет методы для синхронизации данных с SQLite клиентов.
    '''
    
    def __init__(self, host: str, port: int, database: str, user: str, password: str):
        """
        Инициализация параметров подключения к PostgreSQL.
        
        Args:
            host: Адрес сервера PostgreSQL
            port: Порт (5432)
            database: Имя базы данных
            user: Имя пользователя
            password: Пароль
        """
        self.connection_params = {
            'host': host,
            'port': port,
            'database': database,
            'user': user,
            'password': password
        }
        self._init_db()
    
    def _get_connection(self):
        """Создает новое соединение с БД. """
        return psycopg2.connect(**self.connection_params, cursor_factory=RealDictCursor)
    
    @contextmanager
    def get_connection(self):
        """Контекстный менеджер для работы с соединением."""
        conn = self._get_connection()
        try:
            yield conn
        finally:
            conn.close()
    
    def _init_db(self):
        """Инициализация таблиц PostgreSQL из SQL файлов."""
        schema_dir = Path(__file__).parent.parent / "postgresql"
        
        with self.get_connection() as conn:
            with conn.cursor() as cur:
                for sql_file in sorted(schema_dir.glob("*.sql")):
                    with open(sql_file, 'r', encoding='utf-8') as f:
                        cur.execute(f.read())
                conn.commit()
    
    # ==================== Методы для синхронизации ====================
    
    def sync_user(self, email, password_hash, full_name, 
                  created_at, updated_at, last_login_at, is_active):
        """
        синхронизация пользователя с сервером.
        
        Returns:
            ID пользователя в PostgreSQL или None при ошибке
        """
        with self.get_connection() as conn:
            with conn.cursor() as cur:
                try:
                    cur.execute("""
                        INSERT INTO users (email, password_hash, full_name, created_at,
                                updated_at, last_login_at, is_active)
                        VALUES (%s, %s, %s, %s, %s, %s, %s)
                        ON CONFLICT (email) DO UPDATE SET
                            password_hash = EXCLUDED.password_hash,
                            full_name = EXCLUDED.full_name,
                            updated_at = NOW(),
                            last_login_at = EXCLUDED.last_login_at,
                            is_active = EXCLUDED.is_active
                        RETURNING id""",
                        (email, password_hash, full_name, created_at,
                         updated_at, last_login_at, is_active))
                    
                    conn.commit()
                    return cur.fetchone()[0]
                except Exception as e:
                    print(f"Ошибка синхронизации пользователя: {e}")
                    conn.rollback()
                    return None
    
    def sync_document(self, local_id, user_id, title, document_type,
                      content, image_data, created_at, updated_at,
                      deleted_at):
        """
        синхронизация документа с сервером.
        
        Returns:
            True если успешно, False при ошибке
        """
        with self.get_connection() as conn:
            with conn.cursor() as cur:
                try:
                    cur.execute("""
                        INSERT INTO documents 
                        (local_id, user_id, title, document_type, content,
                         image_data, created_at, updated_at, deleted_at)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                        ON CONFLICT (local_id) DO UPDATE SET
                            title = EXCLUDED.title,
                            document_type = EXCLUDED.document_type,
                            content = EXCLUDED.content,
                            image_data = EXCLUDED.image_data,
                            updated_at = EXCLUDED.updated_at,
                            deleted_at = EXCLUDED.deleted_at""",
                        (local_id, user_id, title, document_type, content, 
                          image_data, created_at, updated_at, deleted_at))
                    
                    conn.commit()
                    return True
                except Exception as e:
                    print(f"Ошибка синхронизации документа: {e}")
                    conn.rollback()
                    return False
    
    def sync_health_entry(self, local_id, user_id, entry_type, value1, unit,
                          entry_date, created_at, updated_at, deleted_at = None,
                          value2 = None, value3 = None, notes = None):
        """
        синхронизация записи здоровья с сервером. 

        Returns:
            True если успешно, False при ошибке
        """
        with self.get_connection() as conn:
            with conn.cursor() as cur:
                try:
                    cur.execute("""
                        INSERT INTO health_entries 
                        (local_id, user_id, entry_type, value1, value2, value3, 
                         unit, notes, entry_date, created_at, updated_at, deleted_at)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                        ON CONFLICT (local_id) DO UPDATE SET
                            value1 = EXCLUDED.value1,
                            value2 = EXCLUDED.value2,
                            value3 = EXCLUDED.value3,
                            unit = EXCLUDED.unit,
                            notes = EXCLUDED.notes,
                            entry_date = EXCLUDED.entry_date,
                            updated_at = EXCLUDED.updated_at,
                            deleted_at = EXCLUDED.deleted_at""",
                         (local_id, user_id, entry_type, value1, value2, value3,
                          unit, notes, entry_date, created_at, updated_at, deleted_at))
                    
                    conn.commit()
                    return True
                except Exception as e:
                    print(f"Ошибка синхронизации записи: {e}")
                    conn.rollback()
                    return False
                
    # ==================== Методы для получения корректных данных на устройство ====================

    def get_user_by_email(self, email: str):
        with self.get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT id, email, password_hash, full_name FROM users WHERE email = %s", (email,))
                return cur.fetchone()