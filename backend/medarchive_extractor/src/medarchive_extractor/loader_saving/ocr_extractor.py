"""
Модуль для извлечения текста из медицинских документов с помощью Tesseract
"""

import pytesseract
from PIL import Image
import numpy as np
import cv2
import io
import logging
from pathlib import Path
from typing import Union, Any, Optional

logger = logging.getLogger(__name__)

def get_image_parameters(img: Image.Image) -> tuple:
    """
    Получает среднее значение и стандартное отклонение яркости изображения.
    """
    img_array = np.array(img)
    img_mean = np.mean(img_array)
    img_std = np.std(img_array)
    
    return img_mean, img_std


def _calculate_blur_temp(contrast: float) -> float:
    """
    Рассчитывает коэффициент для адаптивного размытия.
    """
    return 1 + contrast * 5 / 130


def _adaptive_bilateral_filter(image: np.ndarray) -> np.ndarray:
    """
    Применяет адаптивный билатеральный фильтр для шумоподавления.
    """
    height, width = image.shape[:2]
    std_color = np.std(image, axis=(0, 1))
    avg_std_color = np.mean(std_color)
    contrast = np.std(image)
    temp = _calculate_blur_temp(contrast)
    
    d = int(max(5, min(width, height) // 10) / temp)
    sigmaColor = int(max(10, int(avg_std_color * 2)) / temp)
    sigmaSpace = int(max(5, min(width, height) // 20) / temp)
    
    filtered_image = cv2.bilateralFilter(image, d=d, sigmaColor=sigmaColor, sigmaSpace=sigmaSpace)
    return filtered_image


def _analyze_image_quality(mean_brightness: float, std_brightness: float) -> tuple:
    """
    Анализирует качество изображения и определяет необходимые корректировки.
    """
    flag_brightness = 0
    flag_contrast = 0
    
    # Корректировка яркости
    if mean_brightness < 100:
        brightness_adjustment = 1 + (50 - mean_brightness) / 100
        flag_brightness = 1
    elif mean_brightness > 250:
        brightness_adjustment = 1 - (mean_brightness - 250) / 100
        flag_brightness = 1
    else:
        brightness_adjustment = 1
        
    # Корректировка контрастности
    if std_brightness < 50:
        contrast_adjustment = 1 + (50 - std_brightness) / 100
        flag_contrast = 1
    elif std_brightness > 150:
        contrast_adjustment = 1 - (std_brightness - 150) / 100
        flag_contrast = 1
    else:
        contrast_adjustment = 1

    if flag_contrast and flag_brightness:
        adjust_type = 3
    elif flag_brightness:
        adjust_type = 1
    elif flag_contrast:
        adjust_type = 2
    else:
        adjust_type = 0

    return contrast_adjustment, brightness_adjustment, adjust_type


def _preprocess_image(image: Image.Image, 
                      brightness: float = 1.0, 
                      contrast: float = 1.0, 
                      adjust_type: int = 0) -> np.ndarray:
    """
    Предобрабатывает изображение: корректирует яркость/контраст и применяет фильтрацию.
    """
    from PIL import ImageEnhance
    
    brightness = max(0.5, min(brightness, 1.5))
    contrast = max(0.5, min(contrast, 1.5))

    if adjust_type == 1:
        image = ImageEnhance.Brightness(image).enhance(brightness)
    elif adjust_type == 2:
        image = ImageEnhance.Contrast(image).enhance(contrast)
    elif adjust_type == 3:
        image_brightness = ImageEnhance.Brightness(image).enhance(brightness)
        image = ImageEnhance.Contrast(image_brightness).enhance(contrast)

    
    img_array = np.array(image)
    
    # Применяем фильтрацию только для цветных изображений
    if len(img_array.shape) == 3:
        img_array = _adaptive_bilateral_filter(img_array)
    
    return img_array


def extract_text_from_image(image_input: Union[Image.Image, np.ndarray, bytes]) -> str:
    """
    Извлекает текст из изображения с помощью Tesseract OCR.
    """
    # Конвертируем входные данные в PIL Image
    if isinstance(image_input, bytes):
        image = Image.open(io.BytesIO(image_input))
    elif isinstance(image_input, np.ndarray):
        image = Image.fromarray(image_input)
    elif isinstance(image_input, Image.Image):
        image = image_input
    else:
        raise ValueError(f"Неподдерживаемый тип изображения: {type(image_input)}")
    
    # Конвертируем в grayscale для лучшего распознавания
    if image.mode != 'L':
        image = image.convert('L')
    
    # Анализируем качество изображения
    mean_bright, std_bright = get_image_parameters(image)
    contrast_adj, brightness_adj, adjust_type = _analyze_image_quality(mean_bright, std_bright)
    
    # Предобрабатываем изображение
    processed_image = _preprocess_image(image, brightness_adj, contrast_adj, adjust_type)
    
    # Распознаем текст
    try:
        text = pytesseract.image_to_string(
            processed_image,
            lang='rus+eng',
            config='--psm 6'
        )
        return text.strip()
    except Exception as e:
        raise RuntimeError(f"Не удалось распознать текст: {e}")


def extract_text_from_pdf(pdf_path: Union[str, Path]) -> str:
    """
    Извлекает текст из PDF документа путем конвертации страниц в изображения и OCR.
    """
    from pdf2image import convert_from_path
    import pytesseract
    from PIL import Image
    import tempfile
    import os
    
    try:
        images = convert_from_path(
            pdf_path, 
            dpi=300,  # Высокое разрешение для лучшего OCR
            fmt='png'
        )
        
        all_text = []
        for page_num, image in enumerate(images, 1):
            logger.debug(f"Обработка страницы {page_num}/{len(images)}")
            if image.mode != 'L':
                image = image.convert('L')
            
            # Распознаем текст с текущей страницы
            page_text = pytesseract.image_to_string(
                image,
                lang='rus+eng',
                config='--psm 6'  # Блок текста
            )
            
            if page_text.strip():
                all_text.append(f"--- Страница {page_num} ---\n{page_text.strip()}")
            else:
                logger.warning(f"Страница {page_num}: текст не распознан")
        
        # Очищаем память
        images.clear()
        
        result = '\n\n'.join(all_text)
     
        return result.strip()
        
    except Exception as e:
        raise RuntimeError(f"Не удалось извлечь текст из PDF {pdf_path}: {e}")

