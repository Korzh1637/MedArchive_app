from typing import Optional
from datetime import datetime

from .base import BaseSchema, SoftDeleteSchema
from .enums import DocumentType


class DocumentCreate(BaseSchema):
    local_id: str
    user_id: int
    title: Optional[str] = None
    document_type: Optional[DocumentType] = None
    content: Optional[str] = None
    # image_url: Optional[str] = None   # для сервера
    image_path: Optional[str] = None  # для локального SQLite

class DocumentUpdate(BaseSchema):
    title: Optional[str] = None
    document_type: Optional[DocumentType] = None
    content: Optional[str] = None

class Document(DocumentCreate, SoftDeleteSchema):
    id: Optional[int] = None
    is_synced: bool = False
    last_sync_at: Optional[datetime] = None