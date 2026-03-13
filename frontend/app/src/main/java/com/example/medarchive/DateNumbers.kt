package com.example.medarchive

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.filter { it.isDigit() }.take(8)

        val formatted = buildString {
            trimmed.forEachIndexed { index, char ->
                if (index == 2 || index == 4) append(" / ")
                append(char)
            }
        }

        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = DateOffsetMapping // ✅ Было: offsetTranslator
        )
    }
}

// Отдельный объект для маппинга
private object DateOffsetMapping : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        if (offset <= 0) return 0
        if (offset <= 2) return offset
        if (offset <= 4) return offset + 3
        return offset + 6
    }

    override fun transformedToOriginal(offset: Int): Int {
        if (offset <= 2) return offset
        if (offset <= 6) return offset - 3
        return offset - 6
    }
}