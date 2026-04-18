# Структура
 **/models - классы для работы с базой данных, API (в будущем)**
    
 **/postgresql - скрипты для создания схемы PostgreSQL (в будущем для синхронизации данных пользователя с разных устройств)**
- users.sql: Таблица пользователей
- documents.sql: Медицинские документы
- health_entries.sql: Дневник здоровья

 **/sqlite - скрипты для создания схемы SQLite**
- users.sql: Таблица пользователей
- documents.sql: Медицинские документы
- health_entries.sql: Дневник здоровья

 **/work_with_db - основная работа с базой данных и ее таблицами**
- database_work.py: класс для работы с локальной базой данных (SQLite)
- postgresql_database.py: класс для работы с серверной базой данных (PostgreSQL)
- tests.py: тесты для проверки работы методов из database_work

 **create_db_temp.py - временный файл для создания локальной базы данных на устройстве**

# База данных приложения и работа с ней
Чтобы создать локальную бд на своем устройстве, необходимо:
 1. запустить файл create_db_temp.py
 2. открыть базу данных с помощью приложения  https://sqlitebrowser.org/dl/

# Запуск тестов
Тесты проверяют все существующие методы для объектов из users, documents, health_entries. Для проверки необходимо запускать из директории MedArchive_app/
     python database/work_with_db/tests.py

# Зависимости
Для локальной бд:
     pip install pydantic bcript email-validator
Для серверной бд (дополнительно):
     pip install fastapi cryptography passlib python-jose python-dotenv
