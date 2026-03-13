import sys
from pathlib import Path
from datetime import datetime

current_file = Path(__file__).resolve()
project_root = current_file.parent.parent.parent  # MedArchive_app/
sys.path.insert(0, str(project_root))

from database.work_with_db.database_work import SQLiteDatabase
from database.models.users import User
from database.models.documents import Document
from database.models.health_entries import HealthEntry
from database.models.enums import HealthEntryType, Unit


def test_user_operations():
    """Тестирование всех операций с пользователями"""
    
    db = SQLiteDatabase("test_medarchive.db")
    clear_test_database(db)
    
    # ===== ТЕСТ 1: Создание пользователя =====
    print("\nТЕСТ 1: Создание пользователя")
    try:
        user = db.create_user(
            email="test@example.com",
            password="secret123",
            full_name="Тестовый Пользователь"
        )
        
        if user:
            print(f"Пользователь создан успешно:")
            print(f"   ID: {user.id}")
            print(f"   Email: {user.email}")
            print(f"   Имя: {user.full_name}")
            print(f"   Создан: {user.created_at}")
        else:
            print("Не удалось создать пользователя")
    except Exception as e:
        print(f"Ошибка при создании: {e}")
    
    # ===== ТЕСТ 2: Создание дубликата =====
    print("\nТЕСТ 2: Попытка создать пользователя с существующим email")
    try:
        user_duplicate = db.create_user(
            email="test@example.com",
            password="another123",
            full_name="Дубликат"
        )
        
        if user_duplicate is None:
            print("Система корректно отклонила создание дубликата")
        else:
            print("Ошибка: создан дубликат пользователя!")
    except Exception as e:
        print(f"Ошибка: {e}")
    
    # ===== ТЕСТ 3: Получение пользователя =====
    print("\nТЕСТ 3: Получение пользователя по email")
    try:
        user_data = db.get_user("test@example.com")
        
        if user_data:
            # Преобразуем Row в словарь для удобного вывода
            user_dict = dict(user_data)
            print(f"Пользователь найден:")
            print(f"   ID: {user_dict['id']}")
            print(f"   Email: {user_dict['email']}")
            print(f"   Имя: {user_dict['full_name']}")
            print(f"   Хеш пароля: {user_dict['password_hash'][:20]}...")
        else:
            print("Пользователь не найден")
    except Exception as e:
        print(f"Ошибка при получении: {e}")
    
    # ===== ТЕСТ 4: Получение несуществующего пользователя =====
    print("\nТЕСТ 4: Получение несуществующего пользователя")
    try:
        user_data = db.get_user("nonexistent@example.com")
        
        if user_data is None:
            print("Система корректно вернула None для несуществующего пользователя")
        else:
            print("Ошибка: найден несуществующий пользователь!")
    except Exception as e:
        print(f"Ошибка: {e}")
    
    # ===== ТЕСТ 5: Обновление пароля =====
    print("\nТЕСТ 5: Обновление пароля пользователя")
    try:
        # Сначала получаем пользователя
        user = db.get_user("test@example.com")
        if user:
            # Преобразуем в объект User (вам нужно адаптировать под вашу модель)
            user_obj = User(
                id=user['id'],
                email=user['email'],
                password_hash=user['password_hash'],
                full_name=user['full_name'],
                created_at=user['created_at'],
                updated_at=user['updated_at']
            )
            
            # Обновляем пароль
            updated_user = db.update_user(
                email="test@example.com",
                user=user_obj,
                password="newpassword456"
            )
            
            if updated_user:
                print(f"Пароль обновлен успешно:")
                print(f"   Новый хеш: {updated_user.password_hash[:20]}...")
                print(f"   Время обновления: {updated_user.updated_at}")
                
                # Проверяем, что пароль действительно изменился
                if updated_user.password_hash != user['password_hash']:
                    print("Хеш пароля изменился (корректно)")
                else:
                    print("Хеш пароля не изменился!")
            else:
                print("Не удалось обновить пароль")
    except Exception as e:
        print(f"Ошибка при обновлении пароля: {e}")
    
    # ===== ТЕСТ 6: Обновление имени =====
    print("\nТЕСТ 6: Обновление имени пользователя")
    try:
        user = db.get_user("test@example.com")
        if user:
            user_obj = User(
                id=user['id'],
                email=user['email'],
                password_hash=user['password_hash'],
                full_name=user['full_name'],
                created_at=user['created_at'],
                updated_at=user['updated_at']
            )
            
            updated_user = db.update_user(
                email="test@example.com",
                user=user_obj,
                full_name="Новое Имя Пользователя"
            )
            
            if updated_user:
                print(f"Имя обновлено успешно:")
                print(f"   Новое имя: {updated_user.full_name}")
                print(f"   Время обновления: {updated_user.updated_at}")
            else:
                print("Не удалось обновить имя")
    except Exception as e:
        print(f"Ошибка при обновлении имени: {e}")
    
    # конфликт с тестами документов
    # # ===== ТЕСТ 7: Мягкое удаление пользователя =====
    # print("\nТЕСТ 7: Мягкое удаление пользователя")
    # try:
    #     result = db.delete_user("test@example.com")
        
    #     if result == 1:
    #         print("Пользователь успешно помечен как удаленный")
            
    #         # Проверяем, что пользователь все еще в БД, но неактивен
    #         user = db.get_user("test@example.com")
    #         if user:
    #             user_dict = dict(user)
    #             print(f"   is_active: {user_dict.get('is_active', 'поле отсутствует')}")
    #             print(f"   last_login_at: {user_dict.get('last_login_at', 'поле отсутствует')}")
    #         else:
    #             print("Пользователь не найден после удаления")
    #     else:
    #         print("Не удалось удалить пользователя")
    # except Exception as e:
    #     print(f"Ошибка при удалении: {e}")

    print("============ End of tests for users =============")

    # Получаем актуальные данные пользователя для тестов документов
    user_row = db.get_user("test@example.com")
    if not user_row:
        print("Ошибка: пользователь не найден")
        return
    
    user_id = user_row['id']
    
    # ===== ТЕСТ 8: Создание документа =====
    print("\nТЕСТ 8: Создание документа")
    try:
        document = db.create_document(
            user_id=user_id,
            title='Анализ крови',
            document_type='Анализ крови',
            text="Гемоглобин: 135, Эритроциты: 4.5",
            image_path="/path/to/image.jpg"
        )
        
        if document:
            print(f"Документ создан успешно:")
            print(f"   local_id: {document.local_id}")
            print(f"   Название: {document.title}")
            print(f"   Тип: {document.document_type}")
            print(f"   Создан: {document.created_at}")
        else:
            print("Не удалось создать документ")
    except Exception as e:
        print(f"Ошибка при создании документа: {e}")
    
    # ===== ТЕСТ 9: Создание второго документа =====
    print("\nТЕСТ 9: Создание второго документа")
    try:
        document2 = db.create_document(
            user_id=user_id,
            title="МРТ",
            document_type="МРТ",
            text="Заключение: без патологий",
            image_path="/path/to/mri.jpg"
        )
        
        if document2:
            print(f"Второй документ создан успешно:")
            print(f"   local_id: {document2.local_id}")
            print(f"   Название: {document2.title}")
        else:
            print("Не удалось создать второй документ")
    except Exception as e:
        print(f"Ошибка при создании второго документа: {e}")
    
    # ===== ТЕСТ 10: Получение документа по ID =====
    print("\nТЕСТ 10: Получение документа по local_id")
    try:
        if 'document' in locals() and document:
            doc_data = db.get_document(user_id, document.local_id)
            
            if doc_data:
                doc_dict = dict(doc_data)
                print(f"Документ найден:")
                print(f"   local_id: {doc_dict.get('local_id', 'N/A')}")
                print(f"   Название: {doc_dict.get('title', 'N/A')}")
                print(f"   Тип: {doc_dict.get('document_type', 'N/A')}")
                print(f"   Текст: {doc_dict.get('content', 'N/A')[:30]}...")
            else:
                print("Документ не найден")
        else:
            print("Нет документа для теста")
    except Exception as e:
        print(f"Ошибка при получении документа: {e}")
    
    # ===== ТЕСТ 11: Получение несуществующего документа =====
    print("\nТЕСТ 11: Получение несуществующего документа")
    try:
        doc_data = db.get_document(user_id, "nonexistent_doc_id")
        
        if doc_data is None:
            print("Система корректно вернула None для несуществующего документа")
        else:
            print("Ошибка: найден несуществующий документ!")
    except Exception as e:
        print(f"Ошибка: {e}")
    
    # ===== ТЕСТ 12: Получение документа с неверным user_id =====
    print("\nТЕСТ 12: Получение документа с неверным user_id")
    try:
        if 'document' in locals() and document:
            doc_data = db.get_document(999999, document.local_id)
            
            if doc_data is None:
                print("Система корректно не нашла документ для чужого user_id")
            else:
                print("Ошибка: документ доступен чужому пользователю!")
        else:
            print("Нет документа для теста")
    except Exception as e:
        print(f"Ошибка: {e}")
    
    # ===== ТЕСТ 13: Мягкое удаление документа =====
    print("\nТЕСТ 13: Мягкое удаление документа")
    try:
        if 'document' in locals() and document:
            result = db.delete_document(user_id, document.local_id)
            
            if result == 1:
                print("Документ успешно помечен как удаленный")
                
                # Проверяем, что документ больше не доступен через get_document
                doc_data = db.get_document(user_id, document.local_id)
                if doc_data is None:
                    print("Документ действительно не доступен после удаления")
                else:
                    print("Документ все еще доступен!")
            else:
                print("Не удалось удалить документ")
        else:
            print("Нет документа для теста")
    except Exception as e:
        print(f"Ошибка при удалении документа: {e}")
    
    # ===== ТЕСТ 14: Попытка удалить несуществующий документ =====
    print("\nТЕСТ 14: Попытка удалить несуществующий документ")
    try:
        result = db.delete_document(user_id, "nonexistent_doc_id")
        
        if result == 0:
            print("Система корректно вернула 0 для несуществующего документа")
        else:
            print(f"Ошибка: удалось удалить несуществующий документ! result={result}")
    except Exception as e:
        print(f"Ошибка: {e}")

    print("============ End of tests for documents =============")

    # ===== ТЕСТ 15: Создание записи в health_entries (давление) =====
    print("\nТЕСТ 15: Создание записи в health_entries (давление)")
    try:
        entry = db.create_entry(
            user_id=user_id,
            entry_type=HealthEntryType.PRESSURE.value,
            unit=Unit.MMHG.value,
            value1=120,
            value2=80,
            value3=60,
            notes="Утреннее измерение",
            entry_date=datetime.now().isoformat()
        )
        
        if entry:
            print(f"Запись создана успешно:")
            print(f"   ID: {entry.id}")
            print(f"   local_id: {entry.local_id}")
            print(f"   Тип: {entry.entry_type}")
            print(f"   Систолическое: {entry.value1}")
            print(f"   Диастолическое: {entry.value2}")
            print(f"   Пульс: {entry.value3}")
            print(f"   Единица: {entry.unit}")
            print(f"   Заметки: {entry.notes}")
            print(f"   Дата: {entry.entry_date}")
            test_entry = entry  # Сохраняем объект для следующих тестов
        else:
            print("Не удалось создать запись")
            test_entry = None
    except Exception as e:
        print(f"Ошибка при создании записи: {e}")
        test_entry = None

    # ===== ТЕСТ 16: Создание записи в health_entries (сахар) =====
    print("\nТЕСТ 16: Создание записи в health_entries (сахар)")
    try:
        entry2 = db.create_entry(
            user_id=user_id,
            entry_type=HealthEntryType.SUGAR.value,
            unit=Unit.MMOLL.value,
            value1=5.5,
            notes="Утренний сахар",
            entry_date=datetime.now().isoformat()
        )
        
        if entry2:
            print(f"Запись создана успешно:")
            print(f"   ID: {entry2.id}")
            print(f"   local_id: {entry2.local_id}")
            print(f"   Тип: {entry2.entry_type}")
            print(f"   Значение: {entry2.value1}")
            print(f"   Единица: {entry2.unit}")
            print(f"   Заметки: {entry2.notes}")
            print(f"   Дата: {entry2.entry_date}")
            test_entry2 = entry2  # Сохраняем объект для следующих тестов
        else:
            print("Не удалось создать запись")
            test_entry2 = None
    except Exception as e:
        print(f"Ошибка при создании записи: {e}")
        test_entry2 = None

    # Проверяем, что записи созданы
    if test_entry is None or test_entry2 is None:
        print("\nПропускаем остальные тесты health_entries из-за ошибок создания")
        print("============ End of tests for health_entries =============")
        return

    # ===== ТЕСТ 18: Получение записи по local_id =====
    print("\nТЕСТ 18: Получение записи по local_id")
    try:
        entry_data = db.get_entry(user_id, test_entry.local_id)  # Используем test_entry
        
        if entry_data:
            entry_dict = dict(entry_data)
            print(f"Запись найдена:")
            print(f"   ID: {entry_dict['id']}")
            print(f"   local_id: {entry_dict['local_id']}")
            print(f"   Тип: {entry_dict['entry_type']}")
            print(f"   Значение 1: {entry_dict['value1']}")
            print(f"   Значение 2: {entry_dict['value2']}")
            print(f"   Значение 3: {entry_dict['value3']}")
            print(f"   Единица: {entry_dict['unit']}")
            print(f"   Заметки: {entry_dict['notes']}")
        else:
            print("Запись не найдена")
    except Exception as e:
        print(f"Ошибка при получении записи: {e}")

    # ===== ТЕСТ 19: Получение несуществующей записи =====
    print("\nТЕСТ 19: Получение несуществующей записи")
    try:
        entry_data = db.get_entry(user_id, "nonexistent_entry")
        
        if entry_data is None:
            print("Система корректно вернула None для несуществующей записи")
        else:
            print("Ошибка: найдена несуществующая запись!")
    except Exception as e:
        print(f"Ошибка: {e}")

    # ===== ТЕСТ 20: Получение записи с неверным user_id =====
    print("\nТЕСТ 20: Получение записи с неверным user_id")
    try:
        entry_data = db.get_entry(999999, test_entry.local_id)
        
        if entry_data is None:
            print("Система корректно не нашла запись для чужого user_id")
        else:
            print("Ошибка: запись доступна чужому пользователю!")
    except Exception as e:
        print(f"Ошибка: {e}")

    # ===== ТЕСТ 21: Обновление типа записи =====
    print("\nТЕСТ 21: Обновление типа записи")
    try:
        updated_entry = db.update_entry(
            entry=test_entry,
            entry_type="Давление (обновлено)"
        )
        
        if updated_entry:
            print(f"Тип записи обновлен успешно:")
            print(f"   Новый тип: {updated_entry.entry_type}")
            print(f"   Время обновления: {updated_entry.updated_at}")
        else:
            print("Не удалось обновить тип записи")
    except Exception as e:
        print(f"Ошибка при обновлении типа: {e}")

    # ===== ТЕСТ 22: Обновление значения записи =====
    print("\nТЕСТ 22: Обновление значения записи")
    try:
        updated_entry = db.update_entry(
            entry=test_entry2,
            value=6.2
        )
        
        if updated_entry:
            print(f"Значение записи обновлено успешно:")
            print(f"   Новое значение: {updated_entry.value1}")
            print(f"   Время обновления: {updated_entry.updated_at}")
        else:
            print("Не удалось обновить значение записи")
    except Exception as e:
        print(f"Ошибка при обновлении значения: {e}")

    # ===== ТЕСТ 23: Обновление единицы измерения =====
    print("\nТЕСТ 23: Обновление единицы измерения")
    try:
        updated_entry = db.update_entry(
            entry=test_entry2,
            unit="mmol/L"
        )
        
        if updated_entry:
            print(f"Единица измерения обновлена успешно:")
            print(f"   Новая единица: {updated_entry.unit}")
            print(f"   Время обновления: {updated_entry.updated_at}")
        else:
            print("Не удалось обновить единицу измерения")
    except Exception as e:
        print(f"Ошибка при обновлении единицы измерения: {e}")

    # ===== ТЕСТ 24: Обновление заметок =====
    print("\nТЕСТ 24: Обновление заметок")
    try:
        updated_entry = db.update_entry(
            entry=test_entry,
            notes="Обновленные заметки: вечернее измерение"
        )
        
        if updated_entry:
            print(f"Заметки обновлены успешно:")
            print(f"   Новые заметки: {updated_entry.notes}")
            print(f"   Время обновления: {updated_entry.updated_at}")
        else:
            print("Не удалось обновить заметки")
    except Exception as e:
        print(f"Ошибка при обновлении заметок: {e}")

    # ===== ТЕСТ 26: Мягкое удаление записи =====
    print("\nТЕСТ 26: Мягкое удаление записи")
    try:
        result = db.delete_entry(user_id, test_entry.local_id)
        
        if result == 1:
            print("Запись успешно помечена как удаленная")
            
            # Проверяем, что запись больше не доступна через get_entry
            entry_data = db.get_entry(user_id, test_entry.local_id)
            if entry_data is None:
                print("Запись действительно не доступна после удаления")
            else:
                print("Запись все еще доступна!")
        else:
            print("Не удалось удалить запись")
    except Exception as e:
        print(f"Ошибка при удалении записи: {e}")

    # ===== ТЕСТ 27: Попытка удалить несуществующую запись =====
    print("\nТЕСТ 27: Попытка удалить несуществующую запись")
    try:
        result = db.delete_entry(user_id, "nonexistent_entry")
        
        if result == 0:
            print("Система корректно вернула 0 для несуществующей записи")
        else:
            print(f"Ошибка: удалось удалить несуществующую запись! result={result}")
    except Exception as e:
        print(f"Ошибка: {e}")

    # ===== ТЕСТ 30: Создание записи без даты =====
    print("\nТЕСТ 30: Создание записи без даты")
    try:
        entry_no_date = db.create_entry(
            user_id=user_id,
            entry_type="Боль",
            unit="балл",
            value1=3,
            notes="Запись без указания даты"
        )
        
        if entry_no_date:
            print(f"Запись без даты создана успешно:")
            print(f"   ID: {entry_no_date.id}")
            print(f"   local_id: {entry_no_date.local_id}")
            print(f"   Дата: {entry_no_date.entry_date}")
        else:
            print("Не удалось создать запись без даты")
    except Exception as e:
        print(f"Ошибка при создании записи без даты: {e}")

    print("============ End of tests for health_entries =============")


def clear_test_database(db):
    """Очистка тестовой БД"""
    with db.get_connection() as conn:
        conn.execute("DELETE FROM users")
        conn.execute("DELETE FROM documents")
        conn.execute("DELETE FROM health_entries")
        conn.commit()

if __name__ == "__main__":
    test_user_operations()