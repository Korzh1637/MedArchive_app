import sqlite3
from contextlib import contextmanager
from pathlib import Path
from datetime import datetime
import sys
from pathlib import Path
import uuid

import bcrypt

current_file = Path(__file__).resolve()
project_root = current_file.parent.parent.parent  # MedArchive_app/
sys.path.insert(0, str(project_root))

from database.models.users import User
from database.models.documents import Document
from database.models.health_entries import HealthEntry


class SQLiteDatabase:
    '''
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
    '''
    
    def __init__(self, db_path: str = "medarchive.db"):
        self.db_path = db_path
        self._init_db()
    
    def _init_db(self):
        '''Инициализация базы данных: создание таблиц, если их нет.'''
        schema_dir = Path(__file__).parent.parent / "sqlite"

        with self.get_connection() as conn:
            for sql_file in sorted(schema_dir.glob("*.sql")):
                with open(sql_file, 'r', encoding='utf-8') as f:
                    conn.executescript(f.read())
    
    @contextmanager
    def get_connection(self):
        '''Контекстный менеджер для безопасного получения соединения.'''
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        try:
            yield conn
        finally:
            conn.close()

    # ==================== Работа с пользователями ====================

    def create_user(self, email, password, full_name):
        ''' создание пользователя в sqlite '''
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

                user_row = self.get_user(email)
                if user_row:
                    return User(
                        id=user_row['id'],
                        email=user_row['email'],
                        password_hash=user_row['password_hash'],
                        full_name=user_row['full_name'],
                        created_at=user_row['created_at'],
                        updated_at=user_row['updated_at']
                    )

                return None
            
            except sqlite3.IntegrityError:
                print(f'пользователь {email} уже есть в базе данных')
                return None
            except Exception as e:
                print(f"Ошибка при создании пользователя: {e}")
                return None
            

    def get_user(self, email):
        ''' получение данных пользователя в sqlite '''
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
        ''' обновление данных пользователя в sqlite '''
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
        ''' удаление пользователя из sqlite '''
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
    
    def create_document(self, user_id, title, document_type, 
                    text, image_path, id=None):
        ''' создание документа в sqlite '''
        with self.get_connection() as conn:
            local_id = str(uuid.uuid4())
            cursor = conn.cursor()

            existing = self.get_document(user_id, local_id)
            if existing:
                return None

            try:
                created_at = datetime.now()
                cursor.execute(
                        ''' INSERT INTO documents
                        (local_id, user_id, title, document_type, content, image_path, 
                         created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)''',
                        (local_id, user_id, title, document_type, 
                         text, image_path, created_at, created_at)
                    )

                conn.commit()

                doc_row = self.get_document(user_id, local_id)
                if doc_row:
                    return Document(
                        local_id=doc_row['local_id'],
                        user_id=doc_row['user_id'],
                        title=doc_row['title'],
                        document_type=doc_row['document_type'],
                        content=doc_row['content'],
                        image_path=doc_row['image_path'],
                        created_at=doc_row['created_at'],
                        updated_at=doc_row['updated_at'],
                        is_synced=doc_row['is_synced'],
                        last_sync_at=doc_row['last_sync_at']
                    )
                return None
            
            except sqlite3.IntegrityError:
                print(f'документ уже есть в базе данных')
                return None
            except Exception as e:
                print(f"Ошибка при создании документа: {e}")
                return None
            

    def get_document(self, user_id, document_id):
        ''' получение документа в sqlite '''
        with self.get_connection() as conn:
            cursor = conn.cursor()

            try:
                cursor.execute('''SELECT * FROM documents
                               WHERE user_id = ? AND local_id = ? AND deleted_at IS NULL''',
                               (user_id, document_id))

                document_data = cursor.fetchone()

                if document_data:
                    return document_data
                return None
            
            except sqlite3.Error as error:
                print(f'произошла ошибка при получении документа пользователя: {error}')
                return None
            
    
    def update_document(self, doc: Document, title=None, 
                        document_type=None, text=None):
        ''' обновление информации о документе в sqlite '''
        with self.get_connection() as conn:
            cursor = conn.cursor()

            if title is not None:
                try:
                    updated_at = datetime.now()
                    cursor.execute('''UPDATE documents 
                                SET title = ?, updated_at = ? 
                                WHERE user_id = ? AND local_id = ?''',
                                (title, updated_at, doc.user_id, doc.local_id))

                    conn.commit()

                    doc.title = title
                    doc.updated_at = updated_at
                
                except sqlite3.IntegrityError as error:
                    print(f'ошибка при обновлении: {error}')
                    return None
                
            if document_type is not None:
                try:
                    updated_at = datetime.now()
                    cursor.execute('''UPDATE documents 
                                SET document_type = ?, updated_at = ? 
                                WHERE user_id = ? AND local_id = ?''',
                                (document_type, updated_at, doc.user_id, doc.local_id))

                    conn.commit()

                    doc.document_type = document_type
                    doc.updated_at = updated_at
                
                except sqlite3.IntegrityError as error:
                    print(f'ошибка при обновлении: {error}')
                    return None
            
            if text is not None:
                try:
                    updated_at = datetime.now()
                    cursor.execute('''UPDATE documents 
                                SET text = ?, updated_at = ? 
                                WHERE user_id = ? AND local_id = ?''',
                                (text, updated_at, doc.user_id, doc.local_id))

                    conn.commit()

                    doc.text = text
                    doc.updated_at = updated_at
                
                except sqlite3.IntegrityError as error:
                    print(f'ошибка при обновлении: {error}')
                    return None

            return doc


    def delete_document(self, user_id, document_id):
        ''' удаление документа пользователя из sqlite '''
        with self.get_connection() as conn:
            cursor = conn.cursor()

            try:
                time_now = datetime.now()
                cursor.execute('''UPDATE documents 
                                SET updated_at = ?, deleted_at = ?
                                WHERE user_id = ? AND local_id = ?''',
                                (time_now, time_now, user_id, document_id))

                conn.commit()
                if cursor.rowcount > 0:
                    return 1
                else:
                    return 0
            
            except sqlite3.IntegrityError:
                print(f'не удалось удалить документ из базы данных')
                return 0

    # ==================== Работа с данными дневников ====================

    def create_entry(self, user_id, entry_type, unit, value1, value2 = None, value3 = None, notes = None, entry_date = None):
        ''' создание записи в health_entries '''
        if entry_date is None:
            entry_date = datetime.now()
        
        with self.get_connection() as conn:
            local_id = str(uuid.uuid4())
            cursor = conn.cursor()
            try:
                created_at = datetime.now()

                if value2 is None and value3 is None:
                    cursor.execute(''' INSERT INTO health_entries
                        (local_id, user_id, entry_type, value1, unit, 
                        notes, entry_date, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)''',
                    (local_id, user_id, entry_type, value1, unit, 
                     notes, entry_date, created_at, created_at))
                else:
                    cursor.execute(''' INSERT INTO health_entries
                        (local_id, user_id, entry_type, value1, value2, value3, unit, 
                        notes, entry_date, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)''',
                    (local_id, user_id, entry_type, value1, value2, value3, unit, 
                     notes, entry_date, created_at, created_at))
                
                conn.commit()
                
                entry_row = self.get_entry(user_id, local_id)

                if value2 is None and value3 is None:
                    if entry_row:
                        return HealthEntry(
                            id=entry_row['id'],
                            local_id=entry_row['local_id'],
                            user_id=entry_row['user_id'],
                            entry_type=entry_row['entry_type'],
                            value1=entry_row['value1'],
                            unit=entry_row['unit'],
                            notes=entry_row['notes'],
                            entry_date=entry_row['entry_date'],
                            createdat=entry_row['created_at'],
                            updatedat=entry_row['updated_at']
                        )
                    return None
                else:
                    if entry_row:
                        return HealthEntry(
                            id=entry_row['id'],
                            local_id=entry_row['local_id'],
                            user_id=entry_row['user_id'],
                            entry_type=entry_row['entry_type'],
                            value1=entry_row['value1'],
                            value2=entry_row['value2'],
                            value3=entry_row['value3'],
                            unit=entry_row['unit'],
                            notes=entry_row['notes'],
                            entry_date=entry_row['entry_date'],
                            createdat=entry_row['created_at'],
                            updatedat=entry_row['updated_at']
                        )
                    return None

            except sqlite3.IntegrityError:
                print(f"запись уже есть в базе данных")
                return None
            except Exception as e:
                print(f"ошибка при создании записи: {e}")
                return None
            

    def get_entry(self, user_id, local_id):
        ''' получение записи из health_entries '''
        with self.get_connection() as conn:
            cursor = conn.cursor()
            try:
                cursor.execute('''SELECT * FROM health_entries 
                    WHERE user_id = ? AND local_id = ? AND deleted_at IS NULL''',
                    (user_id, local_id))
                
                entry_data = cursor.fetchone()
                return entry_data
            
            except sqlite3.Error as error:
                print(f"произошла ошибка при получении записи: {error}")
                return None


    def update_entry(self, entry, entry_type = None, value = None,
                     unit = None, notes = None, note = None):
        ''' обновление записи в health_entries '''
        with self.get_connection() as conn:
            cursor = conn.cursor()
            try:
                updated_at = datetime.now().isoformat()
                
                if entry_type is not None:
                    cursor.execute('''UPDATE health_entries 
                        SET entry_type = ?, updated_at = ? 
                        WHERE user_id = ? AND local_id = ?''',
                        (entry_type, updated_at, entry.user_id, entry.local_id))
                    
                    entry.entry_type = entry_type
                
                if value is not None:
                    cursor.execute('''UPDATE health_entries 
                        SET value1 = ?, updated_at = ? 
                        WHERE user_id = ? AND local_id = ?''',
                        (value, updated_at, entry.user_id, entry.local_id))
                    
                    entry.value1 = value
                
                if unit is not None:
                    cursor.execute('''UPDATE health_entries 
                        SET unit = ?, updated_at = ? 
                        WHERE user_id = ? AND local_id = ?''',
                        (unit, updated_at, entry.user_id, entry.local_id))
                    
                    entry.unit = unit
                
                if notes is not None:
                    cursor.execute('''UPDATE health_entries 
                        SET notes = ?, updated_at = ? 
                        WHERE user_id = ? AND local_id = ?''',
                        (notes, updated_at, entry.user_id, entry.local_id))
                    
                    entry.notes = notes
                
                conn.commit()
                entry.updated_at = updated_at
                return entry
            
            except sqlite3.IntegrityError as error:
                print(f"ошибка при обновлении: {error}")
                return None

    def delete_entry(self, user_id: int, local_id: str) -> int:
        ''' удаление записи из health_entries '''
        with self.get_connection() as conn:
            cursor = conn.cursor()
            try:
                time_now = datetime.now().isoformat()
                cursor.execute('''UPDATE health_entries 
                    SET updated_at = ?, deleted_at = ? 
                    WHERE user_id = ? AND local_id = ?''',
                    (time_now, time_now, user_id, local_id))
                
                conn.commit()
                return cursor.rowcount
            
            except sqlite3.IntegrityError:
                print(f"не удалось удалить запись из базы данных")
                return 0

    # ==================== Вспомогательные методы ====================
    
    def _hash_password(self, password):
        ''' хэширование пароля '''
        salt = bcrypt.gensalt()
        return bcrypt.hashpw(password.encode('utf-8'), salt).decode('utf-8')
    
    def _verify_password(self, input_password, hashed_password):
        ''' проверка правильности пароля '''
        return bcrypt.checkpw(
            input_password.encode('utf-8'), 
            hashed_password.encode('utf-8')
        )