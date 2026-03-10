package com.example.medarchive.splash

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class WaveShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, size.height * 0.2f)

            // Волна 1
            cubicTo(
                x1 = size.width * 0.25f,
                y1 = size.height * 0.1f,
                x2 = size.width * 0.4f,
                y2 = size.height * 0.25f,
                x3 = size.width * 0.5f,
                y3 = size.height * 0.2f
            )

            // Волна 2
            cubicTo(
                x1 = size.width * 0.6f,
                y1 = size.height * 0.15f,
                x2 = size.width * 0.75f,
                y2 = size.height * 0.25f,
                x3 = size.width,
                y3 = size.height * 0.2f
            )

            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        return Outline.Generic(path)
    }
}