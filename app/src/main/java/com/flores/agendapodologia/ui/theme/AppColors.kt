package com.flores.agendapodologia.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────
//  AppColors — contenedor de colores fijos (no dependen de Material You)
//  Accede desde cualquier Composable con: AppTheme.colors.success
// ─────────────────────────────────────────────────────────────────

@Immutable
data class AppColors(
    // ── Éxito / Cita confirmada ───────────────────────────────────
    val success: Color,
    val onSuccess: Color,           // texto/ícono DENTRO del contenedor success
    val successContainer: Color,
    val onSuccessContainer: Color,  // texto/ícono sobre el contenedor

    // ── Advertencia / Pendiente ───────────────────────────────────
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,

    // ── Error suave / Cancelada ───────────────────────────────────
    val errorSoft: Color,
    val onErrorSoft: Color,
    val errorSoftContainer: Color,
    val onErrorSoftContainer: Color,

    // ── Info / Nota ───────────────────────────────────────────────
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,

    // ── Neutros orgánicos ─────────────────────────────────────────
    val surfaceVariantWarm: Color,   // fondo alternativo de tarjeta
    val outlineSubtle: Color,        // borde / separador sutil

    // ── Acento tierra ─────────────────────────────────────────────
    val earthAccent: Color,
    val onEarthAccent: Color,
    val earthAccentContainer: Color,
    val onEarthAccentContainer: Color,

    // ── Texto secundario ──────────────────────────────────────────
    val onSurfaceSubtle: Color,      // subtítulos, metadatos, fechas
)

// ─── Instancias para cada tema ────────────────────────────────────

val LightAppColors = AppColors(
    success              = SuccessLight,
    onSuccess            = Color(0xFFFFFFFF),
    successContainer     = SuccessContainerLight,
    onSuccessContainer   = Color(0xFF0D2B18),

    warning              = WarningLight,
    onWarning            = Color(0xFFFFFFFF),
    warningContainer     = WarningContainerLight,
    onWarningContainer   = Color(0xFF2E1D00),

    errorSoft            = ErrorSoftLight,
    onErrorSoft          = Color(0xFFFFFFFF),
    errorSoftContainer   = ErrorSoftContainerLight,
    onErrorSoftContainer = Color(0xFF360B03),

    info                 = InfoLight,
    onInfo               = Color(0xFFFFFFFF),
    infoContainer        = InfoContainerLight,
    onInfoContainer      = Color(0xFF001E2E),

    surfaceVariantWarm   = SurfaceVariantWarmLight,
    outlineSubtle        = OutlineSubtleLight,

    earthAccent          = EarthAccentLight,
    onEarthAccent        = Color(0xFFFFFFFF),
    earthAccentContainer = EarthAccentContainerLight,
    onEarthAccentContainer = Color(0xFF2B110C),

    onSurfaceSubtle      = OnSurfaceSubtleLight,
)

val DarkAppColors = AppColors(
    success              = SuccessDark,
    onSuccess            = Color(0xFF0D2B18),
    successContainer     = SuccessContainerDark,
    onSuccessContainer   = Color(0xFF85C49A),

    warning              = WarningDark,
    onWarning            = Color(0xFF2E1D00),
    warningContainer     = WarningContainerDark,
    onWarningContainer   = Color(0xFFE8BB6A),

    errorSoft            = ErrorSoftDark,
    onErrorSoft          = Color(0xFF360B03),
    errorSoftContainer   = ErrorSoftContainerDark,
    onErrorSoftContainer = Color(0xFFE8998D),

    info                 = InfoDark,
    onInfo               = Color(0xFF001E2E),
    infoContainer        = InfoContainerDark,
    onInfoContainer      = Color(0xFF8BBCD4),

    surfaceVariantWarm   = SurfaceVariantWarmDark,
    outlineSubtle        = OutlineSubtleDark,

    earthAccent          = EarthAccentDark,
    onEarthAccent        = Color(0xFF2B110C),
    earthAccentContainer = EarthAccentContainerDark,
    onEarthAccentContainer = Color(0xFFD4A89A),

    onSurfaceSubtle      = OnSurfaceSubtleDark,
)

// ─── CompositionLocal ─────────────────────────────────────────────

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

// ─── Punto de acceso global (equivalente a MaterialTheme.colorScheme) ──
object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}


