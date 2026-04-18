"""
Основной модуль библиотеки для извлечения медицинских данных из изображений
"""

import sys
import os
from pathlib import Path

# Добавляем путь к папке loader_saving
current_dir = Path(__file__).parent
loader_saving_dir = current_dir / 'loader_saving'
sys.path.insert(0, str(loader_saving_dir))

import io
import logging
from typing import Dict, Union, Optional
from PIL import Image
import numpy as np

# Импортируем модули из папки loader_saving
from ocr_extractor import extract_text_from_image
from text_postprocessor import postprocess_text

logger = logging.getLogger(__name__)


def process_medical_image(image_data: Union[bytes, np.ndarray, Image.Image],
                          apply_spell_check: bool = True,
                          filter_stopwords: bool = False) -> Dict[str, Optional[str]]:
    """
    Основная функция для обработки медицинского изображения.
    """
    
    # 1. ПРОВЕРКА И КОНВЕРТАЦИЯ ВХОДНЫХ ДАННЫХ
    try:
        image = _convert_to_pil_image(image_data)
        logger.debug(f"Изображение загружено: размер {image.size}, режим {image.mode}")
    except Exception as e:
        logger.error(f"Ошибка загрузки изображения: {e}")
        raise ValueError(f"Ошибка загрузки изображения: {str(e)}")
    
    # 2. РАСПОЗНАВАНИЕ ТЕКСТА ЧЕРЕЗ TESSERACT
    try:
        raw_text = extract_text_from_image(image)
        logger.info(f"Текст распознан. Длина: {len(raw_text)} символов")
        if not raw_text or len(raw_text.strip()) < 10:
            logger.warning("Распознанный текст слишком короткий или пустой")
            raise RuntimeError("Распознанный текст слишком короткий или пустой")
            
    except Exception as e:
        logger.error(f"Ошибка распознавания текста: {e}")
        raise RuntimeError(f"Ошибка распознавания текста: {str(e)}")
    
    # 3. ПОСТОБРАБОТКА И ПАРСИНГ ТЕКСТА
    try:
        parsed_result = postprocess_text(
            raw_text,
            apply_spell_check=apply_spell_check,
            filter_stopwords=filter_stopwords
        )
        logger.info(f"Постобработка завершена. Тип документа: {parsed_result.get('document_type')}")
        
    except Exception as e:
        logger.error(f"Ошибка постобработки текста: {e}")
        raise RuntimeError(f"Ошибка постобработки текста: {str(e)}")
    
    # 4. ФОРМИРОВАНИЕ РЕЗУЛЬТАТА (только нужные поля)
    result = {
        'date': parsed_result.get('date'),
        'document_type': parsed_result.get('document_type'),
        'study_type': parsed_result.get('study_type'),
        'medical_specialty': parsed_result.get('medical_specialty'),
        'conclusion': parsed_result.get('conclusion', ''),
        'recommendations': parsed_result.get('recommendations', '')
    }
    
    logger.info("Обработка изображения успешно завершена")
    return result


def _convert_to_pil_image(image_data: Union[bytes, np.ndarray, Image.Image]) -> Image.Image:
    """
    Конвертирует входные данные в объект PIL Image.

    """
    
    # Если уже PIL Image
    if isinstance(image_data, Image.Image):
        if image_data.mode != 'RGB':
            image_data = image_data.convert('RGB')
        return image_data
    
    # Если numpy array
    if isinstance(image_data, np.ndarray):
        if len(image_data.shape) == 2:
            image = Image.fromarray(image_data, mode='L')
        elif len(image_data.shape) == 3:
            if image_data.shape[2] == 3:
                image = Image.fromarray(image_data, mode='RGB')
            elif image_data.shape[2] == 4:
                image = Image.fromarray(image_data, mode='RGBA')
            else:
                raise ValueError(f"Неподдерживаемый формат numpy array: {image_data.shape}")
        else:
            raise ValueError(f"Неподдерживаемое количество измерений: {len(image_data.shape)}")
        
        if image.mode != 'RGB':
            image = image.convert('RGB')
        return image
    
    # Если bytes
    if isinstance(image_data, bytes):
        try:
            image = Image.open(io.BytesIO(image_data))
            if image.mode != 'RGB':
                image = image.convert('RGB')
            return image
        except Exception as e:
            raise ValueError(f"Не удалось декодировать bytes как изображение: {e}")
    
    # Если строка (путь к файлу) - для удобства
    if isinstance(image_data, str):
        try:
            image = Image.open(image_data)
            if image.mode != 'RGB':
                image = image.convert('RGB')
            return image
        except Exception as e:
            raise ValueError(f"Не удалось открыть файл по пути '{image_data}': {e}")
    
    raise ValueError(f"Неподдерживаемый тип входных данных: {type(image_data)}. "
                     f"Ожидаются: bytes, np.ndarray, PIL.Image или str (путь к файлу)")



# Пример использования
# Пример использования
if __name__ == "__main__":
    import sys
    import io
    import logging
    from PIL import Image, ImageDraw, ImageFont
    
    logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
    
    print("=" * 60)
    print("ПРИМЕР: Обработка изображения")
    print("=" * 60)
    
    # Создаем белое изображение большего размера
    test_img = Image.new('RGB', (1200, 600), color='white')
    draw = ImageDraw.Draw(test_img)
    
    # Пробуем загрузить шрифт с поддержкой кириллицы
    try:
        # Для Windows
        font = ImageFont.truetype("arial.ttf", 20)
    except:
        try:
            # Альтернативный путь для Windows
            font = ImageFont.truetype("C:/Windows/Fonts/Arial.ttf", 20)
        except:
            try:
                # Для Linux/Mac
                font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 20)
            except:
                # Если шрифт не загрузился, используем默认
                font = ImageFont.load_default()
                print("Предупреждение: Шрифт по умолчанию может не поддерживать кириллицу")
    
    test_text = """15.03.2024

ОСМОТР ВРАЧА-КАРДИОЛОГА

Жалобы на боли в области сердца.

ДИАГНОЗ: Ишемическая болезнь сердца.

РЕКОМЕНДАЦИИ: Пройти ЭКГ."""
    
    # Рисуем текст построчно
    y_offset = 50
    for line in test_text.split('\n'):
        draw.text((50, y_offset), line, fill='black', font=font)
        y_offset += 30
    
    # Конвертируем в байты
    img_bytes = io.BytesIO()
    test_img.save(img_bytes, format='PNG')
    img_bytes = img_bytes.getvalue()
    
    # Обрабатываем
    try:
        result = process_medical_image(img_bytes)
        
        print(f"\nРезультат обработки:")
        print(f"Дата: {result['date']}")
        print(f"Тип документа: {result['document_type']}")
        print(f"Тип исследования: {result['study_type']}")
        print(f"Специальность: {result['medical_specialty']}")
        print(f"Заключение: {result['conclusion']}")
        print(f"Рекомендации: {result['recommendations']}")
        
    except Exception as e: 
        print(f"Ошибка: {e}")