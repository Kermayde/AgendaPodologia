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
    // Formateador de hora (ej: 10:30)
    val configuration = LocalConfiguration.current
    val timeFormatter = remember(configuration) {
        SimpleDateFormat("HH:mm", configuration.locales[0])
    }
    val timeString = timeFormatter.format(appointment.date)

    // Estilo condicional
    val isBlockout = appointment.isBlockout  // ← NUEVO
    val isFinished = appointment.status == AppointmentStatus.FINALIZADA && !isBlockout  // No aplica para bloqueos
    val cardAlpha = if (isFinished) 0.6f else 1f // Más transparente si terminó
    val textDecoration = if (isFinished) TextDecoration.LineThrough else null

    // Color distintivo según si es bloqueo o normal
    val (containerColor, borderColor) = when {
        isBlockout -> {
            Pair(Color(0xFFFFB74D).copy(alpha = 0.2f), BorderStroke(2.dp, Color(0xFFFFA500)))  // Naranja para bloqueos
        }
        isOutsideWorkingHours -> {
            // Fuera de horario: fondo con tinte gris-azulado y borde punteado azul-gris
            val bgColor = if (isFinished) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                          else Color(0xFFECEFF1) // Gris azulado claro
            Pair(bgColor, BorderStroke(1.5.dp, Color(0xFF78909C)))  // Borde gris-azul
        }
        else -> {
            val bgColor = if (isFinished) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
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
                // BLOQUEO: Mostrar nombre e icono
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🚫 ${appointment.patientName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFA500)
                    )
                    Text(
                        text = appointment.serviceType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFA500)
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
                        .height(40.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
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
                    Text(
                        text = appointment.serviceType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Indicador si esta cita usó garantía (badge verde)
                    if (appointment.usedWarranty) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text = "POR GARANTÍA",
                                color = Color(0xFF2E7D32),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Indicador si la cita está fuera de horario laboral
                    if (isOutsideWorkingHours) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFFECEFF1),
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text = "⏰ FUERA DE HORARIO",
                                color = Color(0xFF546E7A),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    PendingBadge()
                }

                // COLUMNA 3: PODÓLOGO (Badge)
                val podiatristColor = if (appointment.podiatristName == "Carlos")
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.tertiaryContainer

                Surface(
                    color = podiatristColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = appointment.podiatristName.firstOrNull()?.toString() ?: "?", // Inicial "C" o "K" o "?"
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Icono de Check si terminó
                if (isFinished) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.CheckCircle, null, tint = Color.Gray)
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
private fun PendingBadge() {
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
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            // Punto pulsante
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .alpha(pulseAlpha)
                    .clip(CircleShape)
                    .background(colors.warning)
            )
//            Spacer(modifier = Modifier.width(5.dp))
//            Text(
//                text = "SIN CONFIRMAR",
//                color = colors.onWarningContainer,
//                style = MaterialTheme.typography.labelSmall,
//                fontWeight = FontWeight.Medium
//            )
        }
    }
}
