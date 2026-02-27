package com.flores.agendapodologia.ui.components

import android.icu.text.IDNA
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
import androidx.compose.material.icons.filled.CheckCircle
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
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.AppointmentStatus
import com.flores.agendapodologia.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(12.dp),
    isOutsideWorkingHours: Boolean = false
) {
    val colors = AppTheme.colors
    // Formateador de hora (ej: 10:30)
    val configuration = LocalConfiguration.current
    val timeFormatter = remember(configuration) {
        SimpleDateFormat("HH:mm", configuration.locales[0])
    }
    val timeString = timeFormatter.format(appointment.date)

    // Estilo condicional
    val isBlockout = appointment.isBlockout  // ← NUEVO
    val isFinished = appointment.status == AppointmentStatus.FINALIZADA && !isBlockout  // No aplica para bloqueos
    val isCancelled = appointment.status == AppointmentStatus.CANCELADA && !isBlockout
    val isNoShow = appointment.status == AppointmentStatus.NO_ASISTIO && !isBlockout
    val isDimmed = isFinished || isCancelled || isNoShow  // Cualquiera de estos se atenúa
    val cardAlpha = if (isDimmed) 0.6f else 1f // Más transparente si terminó/canceló/no asistió
    val textDecoration = if (isDimmed) TextDecoration.LineThrough else null

    // Color distintivo según si es bloqueo o normal
    val (containerColor, borderColor) = when {
        isBlockout -> {
            Pair(colors.warningContainer, BorderStroke(3.dp, colors.warning.copy(alpha = 0.5f)))  // Naranja para bloqueos
        }
        isOutsideWorkingHours -> {
            // Fuera de horario: fondo con tinte gris-azulado y borde punteado azul-gris
            val bgColor = if (isDimmed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                          else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            Pair(bgColor, BorderStroke(3.dp, MaterialTheme.colorScheme.surfaceVariant))
        }
        else -> {
            val bgColor = if (isDimmed) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface
            Pair(bgColor, null)  // Color normal para citas
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor),
        border = borderColor
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .alpha(cardAlpha), // Aplicamos transparencia
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Si es bloqueo, mostrar diferente (sin hora, con icono)
            if (isBlockout) {
                // BLOQUEO
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 9.dp, end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Bloqueo",
                        tint = colors.onWarningContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }
                // Separador vertical
                VerticalDivider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                // COLUMNA 2: DATOS DEL PACIENTE Y SERVICIO
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HORA BLOQUEADA",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onWarningContainer,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                // CITA NORMAL: Mostrar hora + datos
                // COLUMNA 1: HORA
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if(Integer.parseInt(timeString.split(":")[0]) < 12) "AM" else "PM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                // Separador vertical
                VerticalDivider(
                    modifier = Modifier
                        .height(40.dp),
                    color = if (!isDimmed) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // COLUMNA 2: DATOS DEL PACIENTE Y SERVICIO
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = textDecoration // Tachado si terminó
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                isCancelled -> "Cancelada"
                                isNoShow -> "No Asistió"
                                else -> appointment.serviceType
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                isCancelled || isNoShow -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (!isDimmed) {
                            if (isOutsideWorkingHours) {
                                AlertBadge()
                            } else if (!appointment.isReminderSent) {
                                UnconfirmBadge()
                            }
                        }
                        else if (appointment.usedWarranty) {
                            WarrantyBadge()
                        }
                    }
                }

                // COLUMNA 3: PODÓLOGO (Badge)
                val podiatristColor = if (appointment.podiatristName == "Carlos")
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.tertiaryContainer

                Surface(
                    color = podiatristColor,
                    shape = CircleShape,
                ) {
                    Text(
                        text = appointment.podiatristName.firstOrNull()?.toString() ?: "?", // Inicial "C" o "K" o "?"
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Badge animado para citas no confirmadas (PENDIENTE).
 * Punto ámbar pulsante + texto.
 */
@Composable
private fun UnconfirmBadge() {
    val colors = AppTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "pending_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        color = colors.warningContainer,
        shape = CircleShape,
        tonalElevation = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            // Punto pulsante
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .alpha(pulseAlpha)
                    .clip(CircleShape)
                    .background(colors.warning)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "SIN CONFIRMAR",
                color = colors.onWarningContainer,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AlertBadge() {
    val colors = AppTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "pending_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        color = colors.errorSoftContainer,
        shape = CircleShape,
        tonalElevation = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            // Punto pulsante
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .alpha(pulseAlpha)
                    .clip(CircleShape)
                    .background(colors.errorSoft)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "INTEMPESTIVO",
                color = colors.onErrorSoftContainer,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
@Composable
private fun WarrantyBadge() {
    val colors = AppTheme.colors

    Surface(
        color = colors.successContainer,
        shape = CircleShape,
        tonalElevation = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            // Punto pulsante
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(colors.success)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "GARANTÍA",
                color = colors.onSuccessContainer,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}