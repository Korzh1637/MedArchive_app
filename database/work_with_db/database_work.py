import sqlite3
from contextlib import contextmanager
from pathlib import Path
from datetime import datetime
import sys
from pathlib import Path

import bcrypt

current_file = Path(__file__).resolve()
project_root = current_file.parent.parent.parent  # MedArchive_app/
sys.path.insert(0, str(project_root))

from database.models.users import User


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

    # ==================== Работа с пользователями ====================

    def create_user(self, email, password, full_name):
        """ создание пользователя в sqlite """
        with self.get_connection() as conn:
            hash_pwd = self._hash_password(password)
            cursor = conn.cursor()

            try:
                created_at = datetime.now()
                cursor.execute(
                    ''' INSERT INTO users (email, password_hash, full_name, created_at)
                    VALUES (?, ?, ?, ?)''',
                    (email, hash_pwd, full_name, created_at)
                )

                conn.commit()

                user_id = cursor.lastrowid
                new_user = User(
                    id=user_id,
                    email=email, 
                    password_hash=hash_pwd, 
                    full_name=full_name,
                    created_at=created_at
                    )

                return new_user
            
            except sqlite3.IntegrityError:
                print(f'пользователь {email} уже есть в базе данных')
                return None
            

    def get_user(self, email):
        """ получение данных пользователя в sqlite """
        with self.get_connection() as conn:
            cursor = conn.cursor()

            try:
                cursor.execute('SELECT * FROM users WHERE email = ?', (email,))

                user_data = cursor.fetchone()
                return user_data
            
            except sqlite3.Error as error:
                print(f'произошла ошибка при получение данных пользователя: {error}')
                return None


    def update_user(self, email, user: User, password=None, full_name=None):
        """ обновление данных пользователя в sqlite """
        with self.get_connection() as conn:
            cursor = conn.cursor()

            if password is not None:
                try:
                    hash_pwd = self._hash_password(password)
                    updated_at = datetime.now()
                    cursor.execute('''UPDATE users 
                                SET password_hash = ?, updated_at = ? 
                                WHERE email = ?''',
                                (hash_pwd, updated_at, email))

                    conn.commit()

                    user.password_hash = hash_pwd
                    user.updated_at = updated_at
                
                except sqlite3.IntegrityError as error:
                    print(f'ошибка при обновлении: {error}')
                    return None
                
            if full_name is not None:
                try:
                    updated_at = datetime.now()
                    cursor.execute('''UPDATE users 
                                SET full_name = ?, updated_at = ? 
                                WHERE email = ?''',
                                (full_name, updated_at, email))

                    conn.commit()

                    user.full_name = full_name
                    user.updated_at = updated_at
                
                except sqlite3.IntegrityError as error:
                    print(f'ошибка при обновлении: {error}')
                    return None
            
            return user
            
                

    def delete_user(self, email):
        """ удаление пользователя из sqlite """
        with self.get_connection() as conn:
            cursor = conn.cursor()

            try:
                time_now = datetime.now()
                cursor.execute('''UPDATE users 
                                SET updated_at = ?, last_login_at = ?, is_active = 0
                                WHERE email = ?''',
                                (time_now, time_now, email))

                conn.commit()
                return 1
            
            except sqlite3.IntegrityError:
                print(f'не удалось удалить пользователя {email} из базы данных')
                return 0

    # ==================== Работа с документами (анализы/заключения) ====================
    
    # ==================== Работа с данными дневников ====================

    # ==================== Вспомогательные методы ====================
    
    def _hash_password(self, password):
        """ хэширование пароля """
        salt = bcrypt.gensalt()
        return bcrypt.hashpw(password.encode('utf-8'), salt).decode('utf-8')
    
    def _verify_password(self, input_password, hashed_password):
        """ проверка праивльности пароля """
        return bcrypt.checkpw(
            input_password.encode('utf-8'), 
            hashed_password.encode('utf-8')
        )