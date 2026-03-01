package com.flores.agendapodologia.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.AppointmentStatus
import com.flores.agendapodologia.ui.theme.AppTheme
import com.flores.agendapodologia.ui.theme.LocalUse12HourFormat
import com.flores.agendapodologia.util.TimeFormatUtils

// ─────────────────────────────────────────────────────────────────
//  AppointmentCard — tarjeta principal de cita en la agenda
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(12.dp),
    isOutsideWorkingHours: Boolean = false
) {
    val isBlockout = appointment.isBlockout
    val isDimmed = !isBlockout && appointment.status in setOf(
        AppointmentStatus.FINALIZADA,
        AppointmentStatus.CANCELADA,
        AppointmentStatus.NO_ASISTIO
    )

    val (containerColor, borderColor) = resolveCardColors(
        isBlockout = isBlockout,
        isOutsideWorkingHours = isOutsideWorkingHours,
        isDimmed = isDimmed
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor),
        border = borderColor
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .alpha(if (isDimmed) 0.6f else 1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isBlockout) {
                BlockoutCardContent()
            } else {
                AppointmentCardContent(
                    appointment = appointment,
                    isDimmed = isDimmed,
                    isOutsideWorkingHours = isOutsideWorkingHours
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Contenido interno: Bloqueo
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RowScope.BlockoutCardContent() {
    val colors = AppTheme.colors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(end = 8.dp).widthIn( min = 57.dp )
            //.widthIn( min = 65.dp )
            //.padding(start = 9.dp, end = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Block,
            contentDescription = "Bloqueo",
            tint = colors.onWarningContainer,
            modifier = Modifier.size(40.dp)
        )
    }

    CardDivider()
    Spacer(modifier = Modifier.width(8.dp))

    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = "HORA BLOQUEADA",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onWarningContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  Contenido interno: Cita normal
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RowScope.AppointmentCardContent(
    appointment: Appointment,
    isDimmed: Boolean,
    isOutsideWorkingHours: Boolean
) {
    val use12Hour = LocalUse12HourFormat.current
    val timeString = rememberFormattedTime(appointment, use12Hour)
    val amPm = if (use12Hour) TimeFormatUtils.getAmPm(appointment.date) else null
    val isCancelled = appointment.status == AppointmentStatus.CANCELADA
    val isNoShow = appointment.status == AppointmentStatus.NO_ASISTIO

    TimeColumn(timeString, amPm, use12Hour)
    CardDivider(isDimmed)
    Spacer(modifier = Modifier.width(8.dp))

    // Info de la cita
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = appointment.patientName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textDecoration = if (isDimmed) TextDecoration.LineThrough else null
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            ServiceOrStatusText(
                isCancelled = isCancelled,
                isNoShow = isNoShow,
                serviceType = appointment.serviceType
            )
            Spacer(modifier = Modifier.width(6.dp))
            StatusBadge(
                isDimmed = isDimmed,
                isOutsideWorkingHours = isOutsideWorkingHours,
                isReminderSent = appointment.isReminderSent,
                usedWarranty = appointment.usedWarranty
            )
        }
    }

    PodiatristBadge(appointment.podiatristName)
}

// ─────────────────────────────────────────────────────────────────
//  Subcomponentes atómicos
// ─────────────────────────────────────────────────────────────────

/** Columna con la hora y AM/PM (solo en formato 12h). */
@Composable
private fun TimeColumn(timeString: String, amPm: String?, use12Hour: Boolean) {
    val colors = AppTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(end = 8.dp).widthIn( min = 57.dp )
    ) {
        Text(
            text = timeString,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (use12Hour && amPm != null) {
            Text(
                text = amPm,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceSubtle
            )
        }
    }
}

/** Texto que muestra el tipo de servicio o el estado (Cancelada / No Asistió). */
@Composable
private fun ServiceOrStatusText(
    isCancelled: Boolean,
    isNoShow: Boolean,
    serviceType: String
) {
    val (text, color) = when {
        isCancelled -> "Cancelada" to MaterialTheme.colorScheme.error
        isNoShow    -> "No Asistió" to MaterialTheme.colorScheme.error
        else        -> serviceType to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color
    )
}

/** Muestra el badge contextual según el estado de la cita. */
@Composable
private fun StatusBadge(
    isDimmed: Boolean,
    isOutsideWorkingHours: Boolean,
    isReminderSent: Boolean,
    usedWarranty: Boolean
) {
    val colors = AppTheme.colors
    when {
        !isDimmed && isOutsideWorkingHours -> PulsingBadge(
            text = "INTEMPESTIVO",
            dotColor = colors.errorSoft,
            containerColor = colors.errorSoftContainer,
            textColor = colors.onErrorSoftContainer
        )
        !isDimmed && !isReminderSent -> PulsingBadge(
            text = "SIN CONFIRMAR",
            dotColor = colors.warning,
            containerColor = colors.warningContainer,
            textColor = colors.onWarningContainer
        )
        isDimmed && usedWarranty -> PulsingBadge(
            text = "GARANTÍA",
            dotColor = colors.success,
            containerColor = colors.successContainer,
            textColor = colors.onSuccessContainer,
            animated = false
        )
    }
}

/** Inicial del podólogo dentro de un círculo coloreado. */
@Composable
private fun PodiatristBadge(podiatristName: String) {
    val color = if (podiatristName == "Carlos")
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.tertiaryContainer

    Surface(color = color, shape = CircleShape) {
        Text(
            text = podiatristName.firstOrNull()?.toString() ?: "?",
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Separador vertical reutilizado entre la hora y la info. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CardDivider(isDimmed: Boolean = false) {
    VerticalDivider(
        modifier = Modifier.height(40.dp),
        color = MaterialTheme.colorScheme.outlineVariant.let {
            if (isDimmed) it.copy(alpha = 0.6f) else it
        }
    )
}

// ─────────────────────────────────────────────────────────────────
//  Badge genérico con punto pulsante (o estático)
// ─────────────────────────────────────────────────────────────────

/**
 * Badge reutilizable: punto (opcionalmente pulsante) + texto.
 *
 * @param animated Si `true` el punto pulsa; si `false` es estático.
 */
@Composable
private fun PulsingBadge(
    text: String,
    dotColor: Color,
    containerColor: Color,
    textColor: Color,
    animated: Boolean = true
) {
    val dotAlpha = if (animated) {
        val transition = rememberInfiniteTransition(label = "badge_pulse")
        val alpha by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
        alpha
    } else 1f

    Surface(
        color = containerColor,
        shape = CircleShape,
        tonalElevation = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .alpha(dotAlpha)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Utilidades
// ─────────────────────────────────────────────────────────────────

/** Calcula los colores del Card según el contexto. */
@Composable
private fun resolveCardColors(
    isBlockout: Boolean,
    isOutsideWorkingHours: Boolean,
    isDimmed: Boolean
): Pair<Color, BorderStroke?> {
    val colors = AppTheme.colors
    return when {
        isBlockout -> colors.warningContainer to BorderStroke(3.dp, colors.warning.copy(alpha = 0.5f))

        isOutsideWorkingHours -> {
            val bg = if (isDimmed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                     else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            bg to BorderStroke(3.dp, MaterialTheme.colorScheme.surfaceVariant)
        }

        else -> {
            val bg = if (isDimmed) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                     else MaterialTheme.colorScheme.surface
            bg to null
        }
    }
}

/** Formatea la hora de la cita según la preferencia de formato 12h/24h. */
@Composable
private fun rememberFormattedTime(appointment: Appointment, use12Hour: Boolean): String {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    return remember(appointment.date, use12Hour, locale) {
        TimeFormatUtils.formatTime(appointment.date, use12Hour, locale)
    }
}