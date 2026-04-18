"""
Модуль постобработки текста для извлечения медицинских данных.
Извлекает: дату, тип документа, медицинскую специальность, заключение, рекомендации.
Поддерживает: лабораторные анализы, инструментальные исследования (УЗИ, КТ, МРТ, рентген, холтер)
"""

import re
import logging
from typing import Dict, Optional, Tuple, List
from datetime import datetime
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize

logger = logging.getLogger(__name__)

# Словари для исправления опечаток
MEDICAL_TERMS_DICT = {
    'абдоминальный', 'абсцесс', 'агглютинация', 'агнозия', 'аденома', 'адреналин',
    'аллергия', 'анемия', 'анестезия', 'ангиография', 'ангиопатия', 'антибиотик',
    'астма', 'бактерия', 'билирубин', 'биопсия', 'брадикардия', 'вакцинация',
    'вирус', 'воспаление', 'врач', 'гемоглобин', 'гепатит', 'гипертония',
    'гипотония', 'глаукома', 'диабет', 'диагностика', 'инфекция', 'инсульт',
    'инфаркт', 'кардиограмма', 'лаборатория', 'лекарство', 'лечение', 'онкология',
    'пневмония', 'реабилитация', 'симптом', 'синдром', 'терапия', 'травма',
    'туберкулез', 'ультразвук', 'урология', 'фармакология', 'физиотерапия',
    'хирургия', 'эндокринология', 'эпидемиология', 'осмотр', 'консультация'
}

# Лабораторные анализы
LAB_TERMS_DICT = {
    'аланинаминотрансфераза', 'алт', 'антитела', 'аст', 'аспартатаминотрансфераза',
    'билирубин', 'витамин д', 'ггт', 'гаммаглутамилтрансфераза', 'гемоглобин',
    'глюкоза', 'индекс протромбин', 'иммуноглобулины', 'кальций', 'креатинин',
    'лейкоциты', 'магний', 'натрий', 'соэ', 'сахар', 'среактивный белок',
    'т3 свободный', 'т4 свободный', 'ттг', 'ферритин', 'фосфор', 'холестерин',
    'триглицериды', 'эритроциты', 'мочевина'
}

# Специальности врачей
MEDICAL_SPECIALTIES = {
    'кардиолог': 'кардиология',
    'сердце': 'кардиология',
    'кардио': 'кардиология',
    'сосуд': 'кардиология',
    'невролог': 'неврология',
    'нерв': 'неврология',
    'мозг': 'неврология',
    'лор': 'оториноларингология',
    'отоларинголог': 'оториноларингология',
    'ухо': 'оториноларингология',
    'горло': 'оториноларингология',
    'нос': 'оториноларингология',
    'терапевт': 'общая практика',
    'общий': 'общая практика',
    'хирург': 'хирургия',
    'операция': 'хирургия',
    'эндокринолог': 'эндокринология',
    'гормон': 'эндокринология',
    'щитовидная': 'эндокринология',
    'гастроэнтеролог': 'гастроэнтерология',
    'желудок': 'гастроэнтерология',
    'кишечник': 'гастроэнтерология',
    'пульмонолог': 'пульмонология',
    'легкие': 'пульмонология',
    'дыхание': 'пульмонология',
    'офтальмолог': 'офтальмология',
    'глаз': 'офтальмология',
    'гинеколог': 'гинекология',
    'женский': 'гинекология',
    'уролог': 'урология',
    'почка': 'урология',
    'травматолог': 'травматология',
    'перелом': 'травматология'
}

# Инструментальные исследования
INSTRUMENTAL_STUDIES = {
    'узи': 'ультразвуковое исследование',
    'ультразвуковое': 'ультразвуковое исследование',
    'эхокг': 'эхокардиография',
    'эхо-кг': 'эхокардиография',
    'эхо кг': 'эхокардиография',
    'экг': 'электрокардиография',
    'электрокардиограмма': 'электрокардиография',
    'холтер': 'холтеровское мониторирование',
    'холтеровское': 'холтеровское мониторирование',
    'суточное мониторирование': 'холтеровское мониторирование',
    'кт': 'компьютерная томография',
    'компьютерная томография': 'компьютерная томография',
    'мрт': 'магнитно-резонансная томография',
    'магнитно-резонансная': 'магнитно-резонансная томография',
    'рентген': 'рентгенография',
    'рентгенография': 'рентгенография',
    'маммография': 'маммография',
    'фгс': 'фиброгастроскопия',
    'гастроскопия': 'фиброгастроскопия',
    'колоноскопия': 'колоноскопия',
    'мскт': 'мультиспиральная компьютерная томография',
    'пэт': 'позитронно-эмиссионная томография',
    'пэт-кт': 'позитронно-эмиссионная томография',
    'спиральная кт': 'спиральная компьютерная томография',
    'доплер': 'допплерография',
    'допплерография': 'допплерография',
    'ангиография': 'ангиография',
    'мазок': 'мазок',
    'биопсия': 'биопсия'
}

# Типы анализов
ANALYSIS_TYPES = {
    'кровь': 'анализ крови',
    'анализ крови': 'анализ крови',
    'общий анализ крови': 'анализ крови',
    'биохимический анализ крови': 'анализ крови',
    'моча': 'анализ мочи',
    'анализ мочи': 'анализ мочи',
    'общий анализ мочи': 'общий анализ мочи',
    'кал': 'анализ кала',
    'анализ кала': 'анализ кала',
    'гормоны': 'анализ гормонов',
    'биохимия': 'биохимический анализ',
    'пцр': 'ПЦР-диагностика',
    'иммунограмма': 'иммунологическое исследование'
}


def levenshtein_distance(a: str, b: str) -> int:
    """Вычисляет расстояние Левенштейна между двумя строками."""
    n, m = len(a), len(b)
    if n == 0:
        return m
    if m == 0:
        return n

    if n > m:
        a, b = b, a
        n, m = m, n

    current_row = list(range(n + 1))
    for i in range(1, m + 1):
        previous_row, current_row = current_row, [i] + [0] * n
        for j in range(1, n + 1):
            add = previous_row[j] + 1
            delete = current_row[j - 1] + 1
            change = previous_row[j - 1]
            if a[j - 1] != b[i - 1]:
                change += 1
            current_row[j] = min(add, delete, change)

    return current_row[n]


def correct_text(text: str, dictionary: set, max_distance: int = 2) -> str:
    """Исправляет опечатки в тексте."""
    words = text.lower().split()
    corrected_words = []

    for word in words:
        if len(word) <= 2 or word.isdigit():
            corrected_words.append(word)
            continue

        closest_word = None
        min_dist = float('inf')

        for dict_word in dictionary:
            dist = levenshtein_distance(word, dict_word)
            if dist < min_dist:
                min_dist = dist
                closest_word = dict_word

        if closest_word and min_dist <= max_distance:
            corrected_words.append(closest_word)
        else:
            corrected_words.append(word)

    return ' '.join(corrected_words)


def extract_date(text: str) -> Optional[str]:
    """Извлекает дату из текста."""
    date_patterns = [
        (r'(\d{2})\.(\d{2})\.(\d{4})', '%d.%m.%Y'),
        (r'(\d{2})/(\d{2})/(\d{4})', '%d/%m/%Y'),
        (r'(\d{2})-(\d{2})-(\d{4})', '%d-%m-%Y'),
        (r'(\d{4})-(\d{2})-(\d{2})', '%Y-%m-%d'),
    ]

    months = {
        'января': '01', 'февраля': '02', 'марта': '03', 'апреля': '04',
        'мая': '05', 'июня': '06', 'июля': '07', 'августа': '08',
        'сентября': '09', 'октября': '10', 'ноября': '11', 'декабря': '12'
    }

    # Поиск даты в формате с месяцем словами
    month_pattern = r'(\d{2})\s+(января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря)\s+(\d{4})'
    match = re.search(month_pattern, text, re.IGNORECASE)
    if match:
        day = match.group(1)
        month_ru = match.group(2).lower()
        year = match.group(3)
        month = months.get(month_ru)
        if month:
            return f"{year}-{month}-{day}"

    # Поиск даты в стандартных форматах
    for pattern, fmt in date_patterns:
        match = re.search(pattern, text)
        if match:
            try:
                date_obj = datetime.strptime(match.group(0), fmt)
                return date_obj.strftime('%Y-%m-%d')
            except ValueError:
                continue

    return None


def determine_study_type(text: str) -> Tuple[str, str]:
    """
    Определяет тип исследования: инструментальное или лабораторное.

    Returns:
        Tuple[str, str]: (категория, тип исследования)
        категория: 'lab_analysis' или 'instrumental_study'
        тип: название исследования (УЗИ, анализ крови, КТ и т.д.)
    """
    text_lower = text.lower()

    # Проверяем инструментальные исследования
    for keyword, study_name in INSTRUMENTAL_STUDIES.items():
        if keyword in text_lower:
            return 'instrumental_study', study_name

    # Проверяем лабораторные анализы
    for keyword, analysis_name in ANALYSIS_TYPES.items():
        if keyword in text_lower:
            return 'lab_analysis', analysis_name

    # Проверяем по терминам лабораторных анализов
    for term in LAB_TERMS_DICT:
        if term in text_lower:
            return 'lab_analysis', 'лабораторное исследование'

    # Если ничего не найдено, пробуем определить по общим признакам
    if any(word in text_lower for word in ['норма', 'показатель', 'единиц', 'ммоль', 'г/л']):
        return 'lab_analysis', 'лабораторное исследование'

    return 'doctor_conclusion', 'консультация врача'


def determine_document_type(text: str) -> str:
    """
    Определяет тип документа с учетом инструментальных и лабораторных исследований.
    """
    text_lower = text.lower()

    # Ключевые слова для заключения врача
    conclusion_keywords = [
        'заключени', 'диагноз', 'осмотр', 'рекомендац', 'назначен',
        'жалоб', 'анамнез', 'пациент', 'врач', 'осмотрен'
    ]

    # Ключевые слова для лабораторных анализов
    lab_keywords = [
        'анализ', 'результат', 'норма', 'показатель', 'исследован',
        'кровь', 'моча', 'биохими', 'общий анализ'
    ] + list(LAB_TERMS_DICT)

    # Ключевые слова для инструментальных исследований
    instrumental_keywords = list(INSTRUMENTAL_STUDIES.keys())

    conclusion_score = sum(1 for kw in conclusion_keywords if kw in text_lower)
    lab_score = sum(1 for kw in lab_keywords if kw in text_lower)
    instrumental_score = sum(1 for kw in instrumental_keywords if kw in text_lower)

    # Приоритет: инструментальные > лабораторные > консультация
    if instrumental_score > 0:
        return 'instrumental_study'
    elif lab_score > conclusion_score:
        return 'lab_analysis'
    else:
        return 'doctor_conclusion'


def extract_medical_specialty(text: str) -> str:
    """Определяет медицинскую специальность по тексту."""
    text_lower = text.lower()

    specialty_scores = {}

    for keyword, specialty in MEDICAL_SPECIALTIES.items():
        if keyword in text_lower:
            specialty_scores[specialty] = specialty_scores.get(specialty, 0) + 1

    if specialty_scores:
        return max(specialty_scores, key=specialty_scores.get)

    # Определение по ключевым словам
    if any(word in text_lower for word in ['сердц', 'сосуд', 'кардио', 'аритм', 'стенокард']):
        return 'кардиология'
    elif any(word in text_lower for word in ['нерв', 'мозг', 'инсульт', 'головн']):
        return 'неврология'
    elif any(word in text_lower for word in ['дыхан', 'легк', 'пневмон', 'бронх']):
        return 'пульмонология'
    elif any(word in text_lower for word in ['желуд', 'кишечн', 'гастр', 'печен']):
        return 'гастроэнтерология'
    elif any(word in text_lower for word in ['гормон', 'щитовид', 'эндокрин', 'сахар']):
        return 'эндокринология'
    elif any(word in text_lower for word in ['хирург', 'операц', 'резекц']):
        return 'хирургия'
    else:
        return 'общая практика'


def extract_conclusion_and_recommendations(text: str) -> Tuple[str, str]:
    """
    Извлекает заключение и рекомендации из текста.
    """
    text_lower = text.lower()

    conclusion_start = None
    recommendations_start = None

    # Маркеры заключения
    conclusion_markers = ['заключение', 'диагноз', 'клинический диагноз', 'основной диагноз', 'заключение:']

    for marker in conclusion_markers:
        pos = text_lower.find(marker)
        if pos != -1:
            conclusion_start = pos
            break

    # Маркеры рекомендаций
    recommendations_markers = ['рекомендации', 'назначено', 'лечение', 'терапия', 'рекомендовано']

    for marker in recommendations_markers:
        pos = text_lower.find(marker)
        if pos != -1:
            recommendations_start = pos
            break

    # Извлечение заключения
    conclusion = ""
    if conclusion_start is not None:
        end_pos = recommendations_start if recommendations_start is not None else len(text)
        conclusion_text = text[conclusion_start:end_pos]

        for marker in conclusion_markers:
            if marker in conclusion_text.lower():
                marker_pos = conclusion_text.lower().find(marker)
                after_marker = conclusion_text[marker_pos + len(marker):]
                after_marker = re.sub(r'^[\s:;,\-]+', '', after_marker)
                conclusion = after_marker.strip()
                break

        if not conclusion:
            conclusion = conclusion_text.strip()

    # Если заключение пустое, ищем альтернативно
    if not conclusion:
        patterns = [
            r'(?:Диагноз|Заключение)\s*[:;]?\s*([^.!?]+[.!?])',
            r'(?:Диагноз|Заключение)\s*[:;]?\s*([^\n]+)'
        ]
        for pattern in patterns:
            match = re.search(pattern, text, re.IGNORECASE)
            if match:
                conclusion = match.group(1).strip()
                break

    # Извлечение рекомендаций
    recommendations = ""
    if recommendations_start is not None:
        end_markers = ['подпись', 'врач', 'дата', 'печать', 'с уважением']
        end_pos = len(text)

        for marker in end_markers:
            pos = text_lower.find(marker, recommendations_start + 1)
            if pos != -1 and pos < end_pos:
                end_pos = pos

        recommendations_text = text[recommendations_start:end_pos]

        for marker in recommendations_markers:
            if marker in recommendations_text.lower():
                marker_pos = recommendations_text.lower().find(marker)
                after_marker = recommendations_text[marker_pos + len(marker):]
                after_marker = re.sub(r'^[\s:;,\-]+', '', after_marker)
                recommendations = after_marker.strip()
                break

        if not recommendations:
            recommendations = recommendations_text.strip()

    # Если рекомендации пустые, ищем альтернативно
    if not recommendations:
        patterns = [
            r'(?:Рекомендации|Назначено|Лечение)\s*[:;]?\s*([^.!?]+[.!?])',
            r'(?:Рекомендации|Назначено|Лечение)\s*[:;]?\s*([^\n]+)'
        ]
        for pattern in patterns:
            match = re.search(pattern, text, re.IGNORECASE)
            if match:
                recommendations = match.group(1).strip()
                break

    # Нормализация пробелов
    conclusion = re.sub(r'\s+', ' ', conclusion).strip()
    recommendations = re.sub(r'\s+', ' ', recommendations).strip()

    # Добавляем пробел после точки, если его нет
    recommendations = re.sub(r'\.([А-Яа-я])', r'. \1', recommendations)
    conclusion = re.sub(r'\.([А-Яа-я])', r'. \1', conclusion)

    return conclusion, recommendations


def filter_text(text: str, remove_stopwords: bool = True) -> str:
    """Фильтрует текст: удаляет спецсимволы и опционально стоп-слова."""
    text = re.sub(r'[^а-яА-ЯёЁ0-9\s\.\,\-\:\;\(\)]', ' ', text)
    text = re.sub(r'\s+', ' ', text).strip()

    if remove_stopwords:
        try:
            words = word_tokenize(text, language='russian')
            stop_words = set(stopwords.words('russian'))
            filtered_words = [word for word in words if word.lower() not in stop_words and len(word) > 2]
            return ' '.join(filtered_words)
        except:
            logger.warning("NLTK стоп-слова не загружены, пропускаем фильтрацию")
            return text

    return text


def postprocess_text(text: str,
                     apply_spell_check: bool = True,
                     filter_stopwords: bool = False) -> Dict[str, str]:
    """
    Основная функция постобработки текста.

    Returns:
        Dict: {
            'date': дата документа,
            'document_type': тип документа (doctor_conclusion, lab_analysis, instrumental_study),
            'study_type': тип исследования (для анализов и инструментальных),
            'medical_specialty': медицинская специальность,
            'conclusion': заключение,
            'recommendations': рекомендации,
            'cleaned_text': очищенный текст
        }
    """
    logger.info("Начало постобработки текста")

    if not text or not isinstance(text, str):
        logger.warning("Пустой или некорректный текст")
        return {
            'date': None,
            'document_type': None,
            'study_type': None,
            'medical_specialty': None,
            'conclusion': '',
            'recommendations': '',
            'cleaned_text': ''
        }

    original_text = text

    # Исправление опечаток
    if apply_spell_check:
        logger.debug("Применяем исправление опечаток")
        text = correct_text(text, MEDICAL_TERMS_DICT)
        text = correct_text(text, LAB_TERMS_DICT)

    # Извлечение данных
    date = extract_date(original_text)
    document_type = determine_document_type(original_text)
    category, study_type = determine_study_type(original_text)

    # Для обратной совместимости: если определилось как инструментальное или лабораторное
    if category in ['instrumental_study', 'lab_analysis']:
        document_type = category

    medical_specialty = extract_medical_specialty(original_text)
    conclusion, recommendations = extract_conclusion_and_recommendations(original_text)

    # Очистка текста (опционально)
    cleaned_text = filter_text(text, remove_stopwords=filter_stopwords)

    result = {
        'date': date,
        'document_type': document_type,
        'study_type': study_type if document_type != 'doctor_conclusion' else None,
        'medical_specialty': medical_specialty,
        'conclusion': conclusion,
        'recommendations': recommendations,
        'cleaned_text': cleaned_text if filter_stopwords else text
    }

    logger.info(f"Постобработка завершена. Тип: {document_type}, Исследование: {study_type}")

    return result


# Пример использования
if __name__ == "__main__":
    # Тест 1: Консультация врача
    doctor_text = """
    15.03.2024
    
    ОСМОТР ВРАЧА-КАРДИОЛОГА
    
    Жалобы на боли в области сердца, одышку при нагрузке.
    
    ДИАГНОЗ: Ишемическая болезнь сердца. Стенокардия напряжения II ФК.
    
    ЗАКЛЮЧЕНИЕ: У пациента выявлена ишемическая болезнь сердца.
    
    РЕКОМЕНДАЦИИ:
    1. Пройти ЭКГ и ЭхоКГ.
    2. Консультация кардиолога через месяц.
    
    Врач: Иванов И.И.
    """

    print("=" * 50)
    print("ТЕСТ 1: КОНСУЛЬТАЦИЯ ВРАЧА")
    print("=" * 50)
    result1 = postprocess_text(doctor_text)
    print(f"Дата: {result1['date']}")
    print(f"Тип документа: {result1['document_type']}")
    print(f"Специальность: {result1['medical_specialty']}")
    print(f"\nЗАКЛЮЧЕНИЕ:\n{result1['conclusion']}")
    print(f"\nРЕКОМЕНДАЦИИ:\n{result1['recommendations']}")

    # Тест 2: УЗИ исследование
    ultrasound_text = """
    10.02.2024
    
    УЗИ ОРГАНОВ БРЮШНОЙ ПОЛОСТИ
    
    ЗАКЛЮЧЕНИЕ: Признаков патологии не выявлено. Печень, желчный пузырь, поджелудочная железа без особенностей.
    
    Рекомендовано: повторить УЗИ через год.
    """

    print("\n" + "=" * 50)
    print("ТЕСТ 2: УЗИ ИССЛЕДОВАНИЕ")
    print("=" * 50)
    result2 = postprocess_text(ultrasound_text)
    print(f"Дата: {result2['date']}")
    print(f"Тип документа: {result2['document_type']}")
    print(f"Тип исследования: {result2['study_type']}")
    print(f"\nЗАКЛЮЧЕНИЕ:\n{result2['conclusion']}")
    print(f"\nРЕКОМЕНДАЦИИ:\n{result2['recommendations']}")

    # Тест 3: Анализ крови
    blood_text = """
    05.03.2024
    
    АНАЛИЗ КРОВИ ОБЩИЙ
    
    Гемоглобин: 145 г/л (норма 130-160)
    Лейкоциты: 6.5 (норма 4-9)
    СОЭ: 12 мм/ч
    
    Заключение: Показатели в пределах нормы.
    """

    print("\n" + "=" * 50)
    print("ТЕСТ 3: АНАЛИЗ КРОВИ")
    print("=" * 50)
    result3 = postprocess_text(blood_text)
    print(f"Дата: {result3['date']}")
    print(f"Тип документа: {result3['document_type']}")
    print(f"Тип исследования: {result3['study_type']}")
    print(f"\nЗАКЛЮЧЕНИЕ:\n{result3['conclusion']}")