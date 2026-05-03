import os
import zipfile
import logging
from pathlib import Path
from typing import Union, Tuple, Any
import mimetypes
import io
from PIL import Image
import pdfplumber
from docx import Document
import pydicom

# Настройка логирования
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Попытка импортировать pillow-heif для поддержки HEIC/HEIF
try:
    import pillow_heif
    pillow_heif.register_heif_opener()
    logger.info("pillow-heif зарегистрирован, HEIC/HEIF будут открываться через Pillow")
except ImportError:
    logger.info("pillow-heif не установлен, HEIC/HEIF не поддерживаются")


def load_medical_document(file_path: Union[str, Path]) -> Tuple[str, Any]:
    """
    Загружает медицинский документ с телефона или другого устройства.
    
    Поддерживаемые форматы:
    - Документы: .pdf, .doc, .docx, .rtf, .txt
    - Изображения: .jpeg, .jpg, .png, .tiff, .bmp, .gif, .heic, .heif (iPhone)
    - Медицинские форматы: .dcm, .dicom, .zip (с DICOM внутри)
        
    Raises:
        FileNotFoundError: Файл не существует
        ValueError: Неподдерживаемый формат или ошибка загрузки
        ImportError: Отсутствует необходимая зависимость
        Exception: Другие ошибки загрузки
    """
    
    file_path = Path(file_path)
    if not file_path.exists():
        raise FileNotFoundError(f"Файл не найден: {file_path}")
    
    extension = file_path.suffix.lower()        # расширение файла
    
    # Словарь поддерживаемых форматов
    SUPPORTED_FORMATS = {
        # Документы
        '.pdf': 'pdf',
        '.doc': 'document',
        '.docx': 'document',
        '.rtf': 'document',
        '.txt': 'text',
        
        # Изображения
        '.jpg': 'image',
        '.jpeg': 'image',
        '.png': 'image',
        '.tiff': 'image',
        '.tif': 'image',
        '.bmp': 'image',
        '.gif': 'image',
        '.heic': 'image',
        '.heif': 'image',
        
        # Медицинские форматы
        '.dcm': 'dicom',
        '.dicom': 'dicom',
        '.zip': 'zip',
    }
    
    # проверка поддержки формата
    if extension not in SUPPORTED_FORMATS:
        mime_type, _ = mimetypes.guess_type(str(file_path))
        if mime_type:
            if 'pdf' in mime_type:
                file_type = 'pdf'
            elif 'image' in mime_type:  # исправлено условие
                file_type = 'image'
            elif 'text' in mime_type:
                file_type = 'text'
            elif 'dicom' in mime_type or 'zip' in mime_type:
                file_type = 'dicom'
            else:
                raise ValueError(f"Неподдерживаемый формат файла: {extension} (MIME: {mime_type})")
        else:
            raise ValueError(f"Неподдерживаемый формат файла: {extension}")
    else:
        file_type = SUPPORTED_FORMATS[extension]
    
    logger.info(f"Загрузка {file_type} файла: {file_path.name}")
    
    # загрузка в зависимости от типа
    try:
        # --- PDF ФАЙЛЫ ---
        if file_type == 'pdf' or extension == '.pdf':
            try:
                with pdfplumber.open(file_path) as pdf:
                    # Извлекаем текст со всех страниц
                    text = "\n".join([page.extract_text() or "" for page in pdf.pages])
                    if not text.strip():
                        # Если текст не извлекся, возвращаем объект pdf для продвинутой работы
                        return 'pdf', file_path
                    return 'pdf', text
            except Exception as e:
                raise ValueError(f"Ошибка загрузки PDF: {e}")
        
        # --- ИЗОБРАЖЕНИЯ (включая HEIC) ---
        elif file_type == 'image':
            try:
                # pillow-heif уже зарегистрировал opener, поэтому открываем как обычно
                image = Image.open(file_path)
                image.verify()
                image = Image.open(file_path)  # повторно открываем после verify
                return 'image', image
            except Exception as e:
                raise ValueError(f"Ошибка загрузки изображения: {e}")
        
        # --- DICOM ФАЙЛЫ ---
        elif file_type == 'dicom':
            try:
                dicom_data = pydicom.dcmread(file_path, force=True)
                if not hasattr(dicom_data, 'PatientID') and not hasattr(dicom_data, 'SOPClassUID'):
                    logger.warning("Файл имеет расширение .dcm, но не содержит обязательных DICOM тегов")
                
                # Создаем архив в оперативной памяти
                zip_buffer = io.BytesIO()
                with zipfile.ZipFile(zip_buffer, 'w', zipfile.ZIP_DEFLATED) as zipf:
                    with open(file_path, 'rb') as f:
                        file_data = f.read()
                    zipf.writestr(Path(file_path).name, file_data)
                zip_buffer.seek(0)
                zip_archive = zipfile.ZipFile(zip_buffer, 'r')
                logger.info(f"DICOM файл упакован в архив (в памяти)")
                return 'dicom', zip_archive
            except Exception as e:
                raise ValueError(f"Ошибка загрузки DICOM: {e}")

        # --- ZIP АРХИВЫ (возможно с DICOM) ---
        elif file_type == 'zip':
            try:
                zip_archive = zipfile.ZipFile(file_path, 'r')
                
                # Проверяем содержимое
                dcm_files = [f for f in zip_archive.namelist() 
                            if f.lower().endswith(('.dcm', '.dicom'))]
                
                if dcm_files:
                    logger.info(f"Найдены DICOM файлы в архиве: {dcm_files}")
                    return 'dicom', zip_archive
                else:
                    raise ValueError(f"В ZIP архиве не найдено DICOM файлов")
            except Exception as e:
                raise ValueError(f"Ошибка обработки ZIP: {e}")
        
        # --- ТЕКСТОВЫЕ ФАЙЛЫ ---
        elif file_type == 'text' or extension == '.txt':
            try:
                encodings = ['utf-8', 'cp1251', 'koi8-r', 'latin-1']
                for encoding in encodings:
                    try:
                        with open(file_path, 'r', encoding=encoding) as f:
                            text = f.read()
                        logger.info(f"Текст загружен с кодировкой {encoding}")
                        return 'text', text
                    except UnicodeDecodeError:
                        continue
                raise ValueError("Не удалось определить кодировку файла")
            except Exception as e:
                raise ValueError(f"Ошибка загрузки текстового файла: {e}")
        
        # --- DOC, DOCX, RTF ---
        elif file_type == 'document':
            if extension == '.docx':
                try:
                    doc = Document(file_path)
                    text = "\n".join([paragraph.text for paragraph in doc.paragraphs])
                    return 'document', text
                except Exception as e:
                    raise ValueError(f"Ошибка загрузки DOCX: {e}")
            
            elif extension == '.doc':
                logger.warning(".doc файлы могут быть не полностью поддерживаемы. Рекомендуется конвертация в .docx")
                try:
                    with open(file_path, 'rb') as f:
                        raw_data = f.read()
                    return 'document', raw_data
                except Exception as e:
                    raise ValueError(f"Ошибка загрузки DOC: {e}")
            
            elif extension == '.rtf':
                try:
                    with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                        text = f.read()
                    return 'document', text
                except Exception as e:
                    raise ValueError(f"Ошибка загрузки RTF: {e}")
    
    except (FileNotFoundError, ValueError, ImportError) as e:
        # Пробрасываем специфические ошибки
        raise
    except Exception as e:
        raise Exception(f"Неожиданная ошибка при загрузке {file_path}: {e}")