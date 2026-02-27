import sqlite3
from contextlib import contextmanager
from pathlib import Path
from typing import Optional, List, Dict, Any
from datetime import datetime
import uuid


class SQLiteDatabase:
    """
    Класс для работы с SQLite.
    
    Пример использования:
        db = SQLiteDatabase("medarchive.db")
        doc_id = db.insert_document({
            "local_id": "doc_123",
            "user_id": 1,
            "title": "Анализ крови",
            "content": "Гемоглобин 135",
            "created_at": datetime.now().isoformat()
        })
        docs = db.get_all_documents(1)
    """
    
    def __init__(self, db_path: str = "medarchive.db"):
        self.db_path = db_path
        self._init_db()
    
    def _init_db(self):
        """Инициализация базы данных: создание таблиц, если их нет."""
        schema_dir = Path(__file__).parent.parent / "sqlite"

        with self.get_connection() as conn:
            for sql_file in sorted(schema_dir.glob("*.sql")):
                with open(sql_file, 'r', encoding='utf-8') as f:
                    conn.executescript(f.read())
    
    @contextmanager
    def get_connection(self):
        """Контекстный менеджер для безопасного получения соединения."""
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        try:
            yield conn
        finally:
            conn.close()
    
    # ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
    
    # ==================== РАБОТА С ДОКУМЕНТАМИ ====================
    
    # ==================== РАБОТА С ЗАПИСЯМИ ЗДОРОВЬЯ ====================