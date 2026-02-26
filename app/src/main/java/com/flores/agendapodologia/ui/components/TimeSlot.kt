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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flores.agendapodologia.model.Appointment
import java.util.*

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimeSlot(
    hour: Int,
    isWorkingHour: Boolean,
    appointments: List<Appointment>,
    isCurrentHour: Boolean = false,  // MEJORA 3: Parámetro nuevo para indicar hora actual
    onAppointmentClick: (Appointment) -> Unit,
    onSlotClick: (Int) -> Unit // Para agendar rápido en esta hora (Futuro)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Altura dinámica según contenido
            // MEJORA 3: Resaltar la hora actual con fondo azul claro
//            .background(
//                if (isCurrentHour) Color(0xFFE3F2FD).copy(alpha = 0.5f)
//                else Color.Transparent
//            )
    ) {
        // COLUMNA 1: La Hora (ej: 10:00)
        Column(
            modifier = Modifier
                .width(60.dp)
                .padding(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = remember(LocalConfiguration.current) { String.format(Locale.getDefault(), "%02d:00", hour) },
                style = MaterialTheme.typography.labelMedium,
                color = if (isCurrentHour) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
                fontSize = 12.sp,
                fontWeight = if (isCurrentHour) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
            )
            // MEJORA 3: Mostrar texto "AHORA" debajo de la hora si es la hora actual
            if (isCurrentHour) {
                Text(
                    text = "AHORA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 9.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }

        // COLUMNA 2: El Contenido (Citas o Hueco)
        // Detectar si hay bloqueos personales
        val hasBlockout = appointments.any { it.isBlockout }

        // Definir el color de fondo según el estado de la hora
        val backgroundColor = when {
            !isWorkingHour -> MaterialTheme.colorScheme.surfaceVariant  // Gris: fuera de horario laboral
            hasBlockout -> Color(0xFFFFC107).copy(alpha = 0.2f)  // Amber claro: hora bloqueada
            else -> MaterialTheme.colorScheme.surface  // Blanco/surface: hora libre/disponible o con citas sin bloqueo
        }

        val isClickable = isWorkingHour && !hasBlockout  // No clickeable si hay bloqueo

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(enabled = isClickable) { if (isClickable) onSlotClick(hour) }
        ) {
            Column {
                // Línea separadora superior
                if (isCurrentHour) {
                    LinearWavyProgressIndicator(
                        progress = { 0.949f },
                        amplitude = { 0.3f },
                        wavelength = 15.dp,
                        waveSpeed = 15.dp,
                        stopSize = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                } else {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                }

                // SIEMPRE mostrar citas si existen
                if (appointments.isNotEmpty()) {
                    val count = appointments.size
                    val outerRadius = 16.dp  // Bordes exteriores más redondeados
                    val innerRadius = 4.dp   // Bordes interiores entre citas menos redondeados

                    // Si hay citas, las mostramos apiladas con formas agrupadas
                    appointments.forEachIndexed { index, appt ->
                        val shape = when {
                            // Una sola cita: todos los bordes redondeados
                            count == 1 -> RoundedCornerShape(outerRadius)
                            // Primera cita del grupo: bordes superiores redondeados, inferiores suaves
                            index == 0 -> RoundedCornerShape(
                                topStart = outerRadius, topEnd = outerRadius,
                                bottomStart = innerRadius, bottomEnd = innerRadius
                            )
                            // Última cita del grupo: bordes inferiores redondeados, superiores suaves
                            index == count - 1 -> RoundedCornerShape(
                                topStart = innerRadius, topEnd = innerRadius,
                                bottomStart = outerRadius, bottomEnd = outerRadius
                            )
                            // Citas intermedias: todos los bordes suaves
                            else -> RoundedCornerShape(innerRadius)
                        }

                        AppointmentCard(
                            appointment = appt,
                            onClick = { onAppointmentClick(appt) },
                            shape = shape,
                            isOutsideWorkingHours = !isWorkingHour
                        )

                        // Espaciado pequeño entre citas de la misma hora (2dp),
                        // excepto después de la última
                        if (index < count - 1) {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                    // Espaciado final después del grupo de citas
                    //Spacer(modifier = Modifier.height(4.dp))
                } else {
                    // Si NO hay citas, mostrar un espacio vacío con el mensaje correspondiente
                    if (!isWorkingHour) {
                        // Fuera de horario laboral: mostrar "No Disponible"
                        Box(
                            modifier = Modifier
                                .clip(shape = MaterialTheme.shapes.medium )
                                .height(45.dp)
                                .background(backgroundColor)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No Disponible",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray.copy(alpha = 0.5f)
                            )
                        }
                    } else if (hasBlockout) {
                        // Hora bloqueada: mostrar "Horario Bloqueado"
                        Box(
                            modifier = Modifier
                                .clip(shape = MaterialTheme.shapes.medium )
                                .height(60.dp)
                                .background(backgroundColor)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "🚫 Horario Bloqueado",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFA500)
                            )
                        }
                    } else {
                        // Hora libre/disponible: mostrar espacio en blanco
                        Box(
                            modifier = Modifier
                                .clip(shape = MaterialTheme.shapes.medium )
                                .height(60.dp)
                                .background(backgroundColor)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}