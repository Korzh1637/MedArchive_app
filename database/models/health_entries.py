from typing import Optional
from datetime import datetime

from pydantic import field_validator

from .base import BaseSchema, SoftDeleteSchema
from .enums import HealthEntryType, Unit


class HealthEntryCreate(BaseSchema):
    local_id: str
    user_id: int
    entry_type: HealthEntryType
    value1: Optional[float] = None
    value2: Optional[float] = None  # для диастолического давления
    value3: Optional[float] = None  # для пульса
    unit: Optional[Unit] = None
    notes: Optional[str] = None
    entry_date: datetime

    @field_validator('entry_type')
    def validate_pressure(cls, v, values):
        """Для давления обязательно два значения"""
        if v == HealthEntryType.PRESSURE:
            if 'value2' not in values or values['value2'] is None:
                raise ValueError('Для давления нужно указать value2 (диастолическое)')
        return v

class HealthEntryUpdate(BaseSchema):
    value1: Optional[float] = None
    value2: Optional[float] = None
    notes: Optional[str] = None
    entry_date: Optional[datetime] = None

class HealthEntry(HealthEntryCreate, SoftDeleteSchema):
    id: int
    is_synced: bool = False