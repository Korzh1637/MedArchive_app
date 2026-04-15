"""
Модуль постобработки текста для извлечения медицинских данных.
Извлекает: дату, тип документа, медицинскую специальность, заключение, рекомендации.
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
    'хирургия', 'эндокринология', 'эпидемиология'
}

LAB_TERMS_DICT = {
    'аланинаминотрансфераза', 'алт', 'антитела', 'аст', 'аспартатаминотрансфераза',
    'билирубин', 'витамин д', 'ггт', 'гаммаглутамилтрансфераза', 'гемоглобин',
    'глюкоза', 'индекс протромбин', 'иммуноглобулины', 'кальций', 'креатинин',
    'лейкоциты', 'магний', 'натрий', 'соэ', 'сахар', 'среактивный белок',
    'т3 свободный', 'т4 свободный', 'ттг', 'ферритин', 'фосфор', 'холестерин',
    'триглицериды', 'эритроциты', 'мочевина', 'креатинин'
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


def determine_document_type(text: str) -> str:
    """Определяет тип документа: заключение врача или анализы."""
    text_lower = text.lower()

    conclusion_keywords = [
        'заключени', 'диагноз', 'осмотр', 'рекомендац', 'назначен',
        'жалоб', 'анамнез', 'пациент', 'врач', 'осмотрен'
    ]

    lab_keywords = [
        'анализ', 'результат', 'норма', 'показатель', 'исследован',
        'кровь', 'моча', 'биохими', 'общий анализ', 'единиц'
    ] + list(LAB_TERMS_DICT)

    conclusion_score = sum(1 for kw in conclusion_keywords if kw in text_lower)
    lab_score = sum(1 for kw in lab_keywords if kw in text_lower)

    if re.search(r'\d+\s*[|-]\s*\d+', text) and lab_score > 0:
        return 'lab_analysis'

    return 'doctor_conclusion' if conclusion_score >= lab_score else 'lab_analysis'


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


def extract_conclusion_and_recommendations(text: str) -> tuple[str, str]:
    """
    Извлекает заключение и рекомендации из текста.
    """
    text_lower = text.lower()

    conclusion_start = None
    recommendations_start = None

    # Маркеры заключения
    conclusion_markers = ['заключение', 'диагноз', 'клинический диагноз', 'основной диагноз']

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
        # Определяем конец заключения
        end_pos = recommendations_start if recommendations_start is not None else len(text)
        conclusion_text = text[conclusion_start:end_pos]

        # Находим позицию маркера
        marker_found = None
        marker_len = 0
        for marker in conclusion_markers:
            if marker in conclusion_text.lower():
                marker_found = marker
                marker_len = len(marker)
                break

        if marker_found:
            # Находим позицию маркера в тексте
            marker_pos = conclusion_text.lower().find(marker_found)
            # Берем текст ПОСЛЕ маркера
            after_marker = conclusion_text[marker_pos + marker_len:]
            # Удаляем только двоеточия, пробелы и знаки препинания в начале
            after_marker = re.sub(r'^[\s:;,\-]+', '', after_marker)
            conclusion = after_marker.strip()
        else:
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
        # Определяем конец рекомендаций
        end_markers = ['подпись', 'врач', 'дата', 'печать', 'с уважением']
        end_pos = len(text)

        for marker in end_markers:
            pos = text_lower.find(marker, recommendations_start + 1)
            if pos != -1 and pos < end_pos:
                end_pos = pos

        recommendations_text = text[recommendations_start:end_pos]

        # Находим позицию маркера
        marker_found = None
        marker_len = 0
        for marker in recommendations_markers:
            if marker in recommendations_text.lower():
                marker_found = marker
                marker_len = len(marker)
                break

        if marker_found:
            # Находим позицию маркера в тексте
            marker_pos = recommendations_text.lower().find(marker_found)
            # Берем текст ПОСЛЕ маркера
            after_marker = recommendations_text[marker_pos + marker_len:]
            # Удаляем только двоеточия, пробелы и знаки препинания в начале
            after_marker = re.sub(r'^[\s:;,\-]+', '', after_marker)
            recommendations = after_marker.strip()
        else:
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

    # Восстанавливаем потерянные буквы (если строка начинается не с заглавной)
    # Это костыль для случаев, когда первая буква была съедена
    if conclusion and len(conclusion) > 1:
        # Проверяем, не начинается ли предложение с маленькой буквы
        if conclusion[0].islower() and conclusion[0] != 'у':
            # Пробуем восстановить первую букву из контекста
            possible_starts = ['У', 'В', 'П', 'Н', 'Д', 'С']
            for start in possible_starts:
                if start.lower() + conclusion[1:] in conclusion:
                    conclusion = start + conclusion[1:]
                    break

    if recommendations and len(recommendations) > 1:
        if recommendations[0].islower() and recommendations[0] != 'п':
            possible_starts = ['П', 'Н', 'Р', 'В']
            for start in possible_starts:
                if start.lower() + recommendations[1:] in recommendations:
                    recommendations = start + recommendations[1:]
                    break

    # Нормализация пробелов (но не удаляем пробелы после точек)
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

    Args:
        text: исходный текст для обработки
        apply_spell_check: применять ли исправление опечаток
        filter_stopwords: удалять ли стоп-слова

    Returns:
        Dict: {
            'date': дата документа,
            'document_type': тип документа,
            'medical_specialty': медицинская специальность,
            'conclusion': заключение,
            'recommendations': рекомендации,
            'cleaned_text': очищенный текст (опционально)
        }
    """
    logger.info("Начало постобработки текста")

    if not text or not isinstance(text, str):
        logger.warning("Пустой или некорректный текст")
        return {
            'date': None,
            'document_type': None,
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
    medical_specialty = extract_medical_specialty(original_text)
    conclusion, recommendations = extract_conclusion_and_recommendations(original_text)

    # Очистка текста (опционально)
    cleaned_text = filter_text(text, remove_stopwords=filter_stopwords)

    result = {
        'date': date,
        'document_type': document_type,
        'medical_specialty': medical_specialty,
        'conclusion': conclusion,
        'recommendations': recommendations,
        'cleaned_text': cleaned_text if filter_stopwords else text
    }

    logger.info(f"Постобработка завершена. Тип: {document_type}, Специальность: {medical_specialty}")
    logger.info(f"Заключение: {conclusion[:100] if conclusion else 'Не найдено'}...")
    logger.info(f"Рекомендации: {recommendations[:100] if recommendations else 'Не найдены'}...")

    return result


# Пример использования
if __name__ == "__main__":
    test_text = """
    15.03.2024
    
    ОСМОТР ВРАЧА-КАРДИОЛОГА
    
    Жалобы на боли в области сердца, одышку при нагрузке.
    
    ДИАГНОЗ: Ишемическая болезнь сердца. Стенокардия напряжения II ФК.
    
    ЗАКЛЮЧЕНИЕ: У пациента выявлена ишемическая болезнь сердца.
    
    РЕКОМЕНДАЦИИ: Пройти ЭКГ и ЭхоКГ. Консультация кардиолога через месяц.Принимать назначенные препараты.
    
    Врач: Иванов И.И.
    """

    result = postprocess_text(test_text, apply_spell_check=True, filter_stopwords=False)

    print("=" * 50)
    print("РЕЗУЛЬТАТЫ ОБРАБОТКИ:")
    print("=" * 50)
    print(f"Дата: {result['date']}")
    print(f"Тип документа: {result['document_type']}")
    print(f"Специальность: {result['medical_specialty']}")
    print(f"\nЗАКЛЮЧЕНИЕ:\n{result['conclusion']}")
    print(f"\nРЕКОМЕНДАЦИИ:\n{result['recommendations']}")