package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.ui.theme.AppTheme
import java.util.Locale

// ─────────────────────────────────────────────────────────────────
//  TimeSlot — franja horaria dentro de la línea de tiempo
// ─────────────────────────────────────────────────────────────────

private val OUTER_RADIUS = 16.dp
private val INNER_RADIUS = 4.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimeSlot(
    hour: Int,
    isWorkingHour: Boolean,
    appointments: List<Appointment>,
    isCurrentHour: Boolean = false,
    onAppointmentClick: (Appointment) -> Unit,
    onSlotClick: (Int) -> Unit
) {
    val hasBlockout = appointments.any { it.isBlockout }
    val isClickable = isWorkingHour && !hasBlockout

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        HourLabel(
            hour = hour,
            isCurrentHour = isCurrentHour,
            onSlotClick = { onSlotClick(hour) }
        )

        SlotContent(
            appointments = appointments,
            isCurrentHour = isCurrentHour,
            isWorkingHour = isWorkingHour,
            isClickable = isClickable,
            hasBlockout = hasBlockout,
            onAppointmentClick = onAppointmentClick,
            onSlotClick = { onSlotClick(hour) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  Subcomponentes
// ─────────────────────────────────────────────────────────────────

/** Columna izquierda con la hora (ej: 10:00) y opcionalmente "AHORA". */
@Composable
private fun HourLabel(
    hour: Int,
    isCurrentHour: Boolean,
    onSlotClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .width(60.dp)
            .fillMaxHeight()
            .clickable(onClick = onSlotClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = remember(hour) { String.format(Locale.getDefault(), "%02d:00", hour) },
            style = MaterialTheme.typography.labelMedium,
            fontSize = 12.sp,
            color = if (isCurrentHour) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.outline,
            fontWeight = if (isCurrentHour) FontWeight.Bold else FontWeight.Normal
        )
        if (isCurrentHour) {
            Text(
                text = "AHORA",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Columna derecha: separador + citas o placeholder vacío. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RowScope.SlotContent(
    appointments: List<Appointment>,
    isCurrentHour: Boolean,
    isWorkingHour: Boolean,
    isClickable: Boolean,
    hasBlockout: Boolean,
    onAppointmentClick: (Appointment) -> Unit,
    onSlotClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .weight(1f)
            .fillMaxHeight()
            .clickable(enabled = isClickable, onClick = onSlotClick)
    ) {
        Column(modifier = Modifier.padding(end = 8.dp)) {
            SlotDivider(isCurrentHour)

            if (appointments.isNotEmpty()) {
                AppointmentGroup(
                    appointments = appointments,
                    isWorkingHour = isWorkingHour,
                    onAppointmentClick = onAppointmentClick
                )
            } else {
                EmptySlotPlaceholder(
                    isWorkingHour = isWorkingHour,
                    hasBlockout = hasBlockout
                )
            }
        }
    }
}

/**
 * Separador superior de la franja: línea ondulada animada si es la hora actual,
 * o un simple espaciador en caso contrario.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SlotDivider(isCurrentHour: Boolean) {
    if (isCurrentHour) {
        Spacer(modifier = Modifier.height(2.dp))
        LinearWavyProgressIndicator(
            progress = { 0.949f },
            amplitude = { 0.3f },
            wavelength = 15.dp,
            waveSpeed = 15.dp,
            stopSize = 0.dp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(2.dp))
    } else {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )
    }
}

/** Grupo de tarjetas apiladas con bordes agrupados (primera, intermedia, última). */
@Composable
private fun AppointmentGroup(
    appointments: List<Appointment>,
    isWorkingHour: Boolean,
    onAppointmentClick: (Appointment) -> Unit
) {
    val count = appointments.size

    appointments.forEachIndexed { index, appt ->
        AppointmentCard(
            appointment = appt,
            onClick = { onAppointmentClick(appt) },
            shape = resolveGroupedShape(index, count),
            isOutsideWorkingHours = !isWorkingHour
        )
        if (index < count - 1) {
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

/**
 * Placeholder para franjas horarias vacías.
 * Muestra un mensaje distinto según si es fuera de horario, hora bloqueada o libre.
 */
@Composable
private fun EmptySlotPlaceholder(
    isWorkingHour: Boolean,
    hasBlockout: Boolean
) {
    val colors = AppTheme.colors

    val (bgColor, text, textColor, height) = when {
        !isWorkingHour -> SlotPlaceholderStyle(
            background = MaterialTheme.colorScheme.surfaceVariant,
            text = "No Disponible",
            textColor = Color.Gray.copy(alpha = 0.5f),
            height = 45.dp
        )
        hasBlockout -> SlotPlaceholderStyle(
            background = colors.warningContainer,
            text = "Horario Bloqueado",
            textColor = colors.onWarningContainer.copy(alpha = 0.5f),
            height = 60.dp
        )
        else -> SlotPlaceholderStyle(
            background = MaterialTheme.colorScheme.surface,
            text = null,
            textColor = Color.Transparent,
            height = 60.dp
        )
    }

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .height(height)
            .background(bgColor)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Utilidades
// ─────────────────────────────────────────────────────────────────

/** Calcula el shape agrupado para tarjetas apiladas dentro de la misma hora. */
private fun resolveGroupedShape(index: Int, count: Int) = when {
    count == 1      -> RoundedCornerShape(OUTER_RADIUS)
    index == 0      -> RoundedCornerShape(
        topStart = OUTER_RADIUS, topEnd = OUTER_RADIUS,
        bottomStart = INNER_RADIUS, bottomEnd = INNER_RADIUS
    )
    index == count - 1 -> RoundedCornerShape(
        topStart = INNER_RADIUS, topEnd = INNER_RADIUS,
        bottomStart = OUTER_RADIUS, bottomEnd = OUTER_RADIUS
    )
    else            -> RoundedCornerShape(INNER_RADIUS)
}

/** Data class interna para parametrizar el placeholder vacío. */
private data class SlotPlaceholderStyle(
    val background: Color,
    val text: String?,
    val textColor: Color,
    val height: Dp
)
