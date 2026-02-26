package com.flores.agendapodologia.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────
//  COLORES FIJOS — independientes del color dinámico del sistema
//  Diseñados para verse orgánicos en tema oscuro Y claro.
// ─────────────────────────────────────────────────────────────────

// ── Estado: Cita confirmada / Éxito ──────────────────────────────
//  Verde salvia apagado (no compite con el primary dinámico)
val SuccessLight   = Color(0xFF4A7C59)   // texto / ícono sobre fondo claro
val SuccessDark    = Color(0xFF85C49A)   // texto / ícono sobre fondo oscuro
val SuccessContainerLight = Color(0xFFD6EDDC)  // fondo de chip/badge claro
val SuccessContainerDark  = Color(0xFF1C3D26)  // fondo de chip/badge oscuro

// ── Estado: Pendiente / Advertencia ──────────────────────────────
//  Ámbar terroso, evoca calidez orgánica
val WarningLight   = Color(0xFF7D5A1E)
val WarningDark    = Color(0xFFE8BB6A)
val WarningContainerLight = Color(0xFFF5E4BB)
val WarningContainerDark  = Color(0xFF3D2C08)

// ── Estado: Cancelada / Error suave ──────────────────────────────
//  Rojo arcilla, no agresivo
val ErrorSoftLight   = Color(0xFF8C3A2E)
val ErrorSoftDark    = Color(0xFFE8998D)
val ErrorSoftContainerLight = Color(0xFFF5D9D5)
val ErrorSoftContainerDark  = Color(0xFF3D1710)

// ── Acento informativo / Nota ─────────────────────────────────────
//  Azul pizarra calmado
val InfoLight   = Color(0xFF2C5B7A)
val InfoDark    = Color(0xFF8BBCD4)
val InfoContainerLight = Color(0xFFCFE4F0)
val InfoContainerDark  = Color(0xFF0E2D3D)

// ── Neutros orgánicos — superficies y separadores ─────────────────
//  Greige cálido (gris con matiz beige/verde muy sutil)
val SurfaceVariantWarmLight = Color(0xFFF0F2EE)  // fondo alterno de tarjeta (claro)
val SurfaceVariantWarmDark  = Color(0xFF252927)  // fondo alterno de tarjeta (oscuro)

val OutlineSubtleLight = Color(0xFFCDD0CB)  // separador / borde sutil (claro)
val OutlineSubtleDark  = Color(0xFF3A3D3A)  // separador / borde sutil (oscuro)

// ── Acento tierra — detalles decorativos ──────────────────────────
//  Terracota apagada, complementa el verde primary
val EarthAccentLight = Color(0xFF8B5E52)
val EarthAccentDark  = Color(0xFFD4A89A)
val EarthAccentContainerLight = Color(0xFFF2DDD8)
val EarthAccentContainerDark  = Color(0xFF3D221D)

// ── Texto secundario fijo ─────────────────────────────────────────
val OnSurfaceSubtleLight = Color(0xFF5A5F5C)   // subtítulos / metadatos (claro)
val OnSurfaceSubtleDark  = Color(0xFFA0A8A2)   // subtítulos / metadatos (oscuro)

