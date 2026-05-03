import os
from datetime import datetime, timedelta, timezone
from typing import Optional

from fastapi import FastAPI, Depends, HTTPException, UploadFile, File, Form
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel
from jose import JWTError, jwt
from passlib.context import CryptContext
from cryptography.fernet import Fernet
from dotenv import load_dotenv

from database.work_with_db.postgresql_database import PostgreSQLDatabase
from backend.medarchive_extractor.src.medarchive_extractor.core import process_medical_image


# ---------- Конфигурация ----------
load_dotenv()
SECRET_KEY = os.getenv("JWT_SECRET_KEY")
if not SECRET_KEY:
    raise ValueError("JWT_SECRET_KEY is not set")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24

ENCRYPTION_KEY = os.getenv("ENCRYPTION_KEY")
cipher = Fernet(ENCRYPTION_KEY.encode())

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")

# Подключение к БД (параметры из переменных окружения)
db = PostgreSQLDatabase(
    host=os.getenv("PG_HOST", "localhost"),
    port=int(os.getenv("PG_PORT", "5432")),
    database=os.getenv("PG_DB", "medarchive"),
    user=os.getenv("PG_USER", "postgres"),
    password=os.getenv("PG_PASSWORD", "")
)

app = FastAPI(title="MedArchive Sync API")


# ---------- Модели Pydantic ----------
class Token(BaseModel):
    access_token: str
    token_type: str


# ---------- Аутентификация ----------
def hash_password(password: str) -> str:
    return pwd_context.hash(password)

def verify_password(plain: str, hashed: str) -> bool:
    return pwd_context.verify(plain, hashed)

def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

async def get_current_user(token: str = Depends(oauth2_scheme)):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id = payload.get("sub")
        if user_id is None:
            raise HTTPException(status_code=401, detail="Invalid token")
        return int(user_id)
    except JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")

# ---------- Эндпоинты ----------
@app.post("/auth/register", response_model=dict)
async def register(
    email: str = Form(...),
    password: str = Form(...),
    full_name: str = Form(...),
    created_at: datetime = Form(...),
    updated_at: datetime = Form(...),
    last_login_at: Optional[datetime] = Form(None),
    is_active: bool = Form(True)
):
    """Регистрация пользователя (вызывает sync_user)"""
    hashed_password = hash_password(password)
    user_id = db.sync_user(
        email=email,
        password_hash=hashed_password,
        full_name=full_name,
        created_at=created_at,
        updated_at=updated_at,
        last_login_at=last_login_at,
        is_active=is_active
    )
    if not user_id:
        raise HTTPException(status_code=400, detail="User sync failed")
    return {"user_id": user_id, "message": "User synchronized"}

@app.post("/auth/login", response_model=Token)
async def login(form_data: OAuth2PasswordRequestForm = Depends()):
    """Логин – возвращает JWT токен"""
    user = db.get_user_by_email(form_data.username)
    if not user or not verify_password(form_data.password, user["password_hash"]):
        raise HTTPException(status_code=400, detail="Incorrect email or password")
    access_token = create_access_token(data={"sub": str(user["id"])})
    return {"access_token": access_token, "token_type": "bearer"}

@app.post("/recogn_doc")
async def recognize_document(
    image: UploadFile = File(...)
):
    raw = await image.read()
    info_dict = process_medical_image(raw)
    title = info_dict['document_type']
    document_type = info_dict['medical_specialty'] if info_dict['document_type'] == "doctor_conclusion" else info_dict['study_type']
    text = info_dict['conclusion'] + info_dict['recommendations']

    return {"title": title, "document_type": document_type, "content": text}
    

# -------------------- СИНХРОНИЗАЦИЯ (основные методы класса) --------------------
@app.post("/sync/user")
async def sync_user_endpoint(
    user_id: int = Form(...),
    email: str = Form(...),
    password_hash: str = Form(...),
    full_name: str = Form(...),
    created_at: datetime = Form(...),
    updated_at: datetime = Form(...),
    last_login_at: Optional[datetime] = Form(None),
    is_active: bool = Form(True),
    current_user_id: int = Depends(get_current_user)
):
    """
    Синхронизация пользователя.
    """

    if user_id != current_user_id:
        raise HTTPException(status_code=403, detail="Forbidden: cannot sync another user")
    
    user_id = db.sync_user(
        email=email,
        password_hash=password_hash,
        full_name=full_name,
        created_at=created_at,
        updated_at=updated_at,
        last_login_at=last_login_at,
        is_active=is_active
    )
    if not user_id:
        raise HTTPException(status_code=500, detail="User sync failed")
    
    return { "user_id": user_id, "status": "synced" }


@app.post("/sync/document")
async def sync_document_endpoint(
    local_id: str = Form(...),
    user_id: int = Form(...),
    title: str = Form(...),
    type_of_doc: str = Form(...),
    text: str = Form(...),
    created_at: datetime = Form(...),
    updated_at: datetime = Form(...),
    deleted_at: Optional[datetime] = Form(None),
    image: UploadFile = File(...),
    current_user_id: int = Depends(get_current_user)
):
    """
    Синхронизация документа с возможностью загрузить изображение.
    Изображение шифруется и сохраняется в БД.

    Пример для фронденда:
    // Retrofit
    @Multipart
    @POST("/sync/document")
    suspend fun syncDocument(
        @Part("local_id") localId: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part("created_at") createdAt: RequestBody,
        @Part("updated_at") updatedAt: RequestBody,
        @Part("deleted_at") deletedAt: RequestBody? = null,
        @Part image: MultipartBody.Part
    )
    """
    if user_id != current_user_id:
        raise HTTPException(status_code=403, detail="Forbidden")

    raw = await image.read()
    encrypted_image = cipher.encrypt(raw)

    doc_id = db.sync_document(
        local_id=local_id,
        user_id=user_id,
        title=title,
        document_type=type_of_doc,
        content=text,
        image_data=encrypted_image,
        created_at=created_at,
        updated_at=updated_at,
        deleted_at=deleted_at
    )

    if not doc_id:
        raise HTTPException(status_code=500, detail="Document sync failed")
    
    return {"document_id": doc_id, "local_id": local_id, "user_id": user_id,
            "title": title, "document_type": type_of_doc,
            "content": text, "created_at": created_at,
            "updated_at": updated_at, "deleted_at": deleted_at}


@app.post("/sync/health_entry")
async def sync_health_entry_endpoint(
    local_id: str = Form(...),
    user_id: int = Form(...),
    entry_type: str = Form(...),
    value1: float = Form(...),
    value2: Optional[float] = Form(None),
    value3: Optional[float] = Form(None),
    unit: str = Form(...),
    notes: str = Form(...),
    entry_date: datetime = Form(...),
    created_at: datetime = Form(...),
    updated_at: datetime = Form(...),   
    deleted_at: Optional[datetime] = Form(None),
    current_user_id: int = Depends(get_current_user)
):
    """
    Синхронизация записи здоровья.
    """
    if user_id != current_user_id:
        raise HTTPException(status_code=403, detail="Forbidden")
    
    success = db.sync_health_entry(
        local_id=local_id,
        user_id=user_id,
        entry_type=entry_type,
        value1=value1,
        unit=unit,
        entry_date=entry_date,
        created_at=created_at,
        updated_at=updated_at,
        deleted_at=deleted_at,
        value2=value2,
        value3=value3,
        notes=notes
    )
    if not success:
        raise HTTPException(status_code=500, detail="Health entry sync failed")
    
    return { "user_id": user_id, "status": "synced" }