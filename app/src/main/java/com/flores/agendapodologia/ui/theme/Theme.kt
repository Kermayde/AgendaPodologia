package com.flores.agendapodologia.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. PALETAS DE RESPALDO (Para Android 11 o menor)
// Puedes poner los tonos originales que tenías para la clínica
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C4C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF89F8C7),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF4C6357),
    tertiary = Color(0xFF3D6373),
    background = Color(0xFFFBFDF9),
    surface = Color(0xFFFBFDF9),
    // Agrega más según necesites...
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6CDBAC),
    onPrimary = Color(0xFF003826),
    primaryContainer = Color(0xFF005138),
    onPrimaryContainer = Color(0xFF89F8C7),
    secondary = Color(0xB2D0DF),
    tertiary = Color(0xA1CEDE),
    background = Color(0xFF191C1A),
    surface = Color(0xFF191C1A),
)

@Composable
fun AgendaPodologiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 2. FLAG PARA HABILITAR COLOR DINÁMICO (True por defecto)
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // 3. LÓGICA DE SELECCIÓN DE COLOR
    val colorScheme = when {
        // Si dynamicColor está encendido Y el celular es Android 12+ (API 31+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Fallbacks para celulares más antiguos
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 4. CAMBIAR EL COLOR DE LA BARRA DE ESTADO (Status Bar)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // 5. APLICAR EL TEMA
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Tu archivo Type.kt
        shapes = Shapes,         // Tu archivo Shape.kt (Aquí metemos lo "Expressive")
        content = content
    )
}