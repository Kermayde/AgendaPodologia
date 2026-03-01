package com.flores.agendapodologia.ui.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal que proporciona la preferencia de formato de hora a toda la UI.
 * `false` = 24 horas (por defecto), `true` = 12 horas.
 */
val LocalUse12HourFormat = compositionLocalOf { false }

