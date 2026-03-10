import sys
from pathlib import Path

current_file = Path(__file__).resolve()
project_root = current_file.parent.parent.parent  # MedArchive_app/
sys.path.insert(0, str(project_root))

from database.work_with_db.database_work import SQLiteDatabase
from database.models.users import User


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
    
    # ===== ТЕСТ 7: Мягкое удаление пользователя =====
    print("\nТЕСТ 7: Мягкое удаление пользователя")
    try:
        result = db.delete_user("test@example.com")
        
        if result == 1:
            print("Пользователь успешно помечен как удаленный")
            
            # Проверяем, что пользователь все еще в БД, но неактивен
            user = db.get_user("test@example.com")
            if user:
                user_dict = dict(user)
                print(f"   is_active: {user_dict.get('is_active', 'поле отсутствует')}")
                print(f"   last_login_at: {user_dict.get('last_login_at', 'поле отсутствует')}")
            else:
                print("Пользователь не найден после удаления")
        else:
            print("Не удалось удалить пользователя")
    except Exception as e:
        print(f"Ошибка при удалении: {e}")

    print("============ End of tests =============")

def clear_test_database(db):
    """Очистка тестовой БД"""
    with db.get_connection() as conn:
        conn.execute("DELETE FROM users")
        conn.commit()

if __name__ == "__main__":
    test_user_operations()