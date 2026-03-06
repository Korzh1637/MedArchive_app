from typing import Optional
from datetime import datetime

from pydantic import EmailStr, field_validator

from .base import BaseSchema, TimestampSchema


class UserCreate(BaseSchema):
    email: EmailStr
    password: str
    full_name: str

    @field_validator('password')
    def validate_password(cls, v):
        """Минимальные требования к паролю."""
        if len(v) < 6:
            raise ValueError('Пароль должен быть не менее 6 символов')
        return v

class UserUpdate(BaseSchema):
    email: Optional[EmailStr] = None
    password: Optional[str] = None
    full_name: Optional[str] = None

    @field_validator('password')
    def validate_password(cls, v):
        """При обновлении пароль может быть опциональным"""
        if v is not None and len(v) < 6:
            raise ValueError('Пароль должен быть не менее 6 символов')
        return v

class User(TimestampSchema):
    id: int
    email: EmailStr
    password_hash: str
    full_name: str