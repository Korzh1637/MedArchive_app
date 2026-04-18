import secrets
import string

# Генерируем 64-символьную случайную строку
secret = secrets.token_urlsafe(64)
print(f"JWT_SECRET_KEY={secret}")

from cryptography.fernet import Fernet

key = Fernet.generate_key()
print(f"ENCRYPTION_KEY={key.decode()}")