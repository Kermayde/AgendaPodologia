package com.flores.agendapodologia.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// M3 Expressive abusa de los bordes extra redondeados para dar un toque orgánico
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp), // Tarjetas normales
    large = RoundedCornerShape(24.dp),  // Tarjetas destacadas (ej. tu DailySummaryCard)
    extraLarge = RoundedCornerShape(32.dp) // BottomSheets o Diálogos grandes
)