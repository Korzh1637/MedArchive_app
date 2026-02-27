from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict

class BaseSchema(BaseModel):
    """Базовая схема с настройками для ORM."""
    model_config = ConfigDict(from_attributes=True)

class TimestampSchema(BaseSchema):
    """Схема с временными метками."""
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None

class SoftDeleteSchema(TimestampSchema):
    """Схема с поддержкой мягкого удаления."""
    deleted_at: Optional[datetime] = None