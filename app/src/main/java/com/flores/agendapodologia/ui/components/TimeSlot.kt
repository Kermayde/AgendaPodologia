package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
            .background(
                if (isCurrentHour) Color(0xFFE3F2FD).copy(alpha = 0.5f)
                else Color.Transparent
            )
    ) {
        // COLUMNA 1: La Hora (ej: 10:00)
        Column(
            modifier = Modifier
                .width(60.dp)
                .padding(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = String.format(Locale.getDefault(), "%02d:00", hour),
                style = MaterialTheme.typography.labelMedium,
                // MEJORA 3: Color azul y negrita si es hora actual
                color = if (isCurrentHour) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                fontSize = 12.sp,
                fontWeight = if (isCurrentHour) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
            )
            // MEJORA 3: Mostrar texto "AHORA" debajo de la hora si es la hora actual
            if (isCurrentHour) {
                Text(
                    text = "AHORA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 9.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }

        // ... resto del código sin cambios ...
        // COLUMNA 2: El Contenido (Citas o Hueco)
        // Detectar si hay bloqueos personales
        val hasBlockout = appointments.any { it.isBlockout }
        val backgroundColor = when {
            !isWorkingHour -> MaterialTheme.colorScheme.surfaceVariant  // Gris claro: cerrado
            hasBlockout -> MaterialTheme.colorScheme.errorContainer  // Rojo claro: bloqueado
            else -> MaterialTheme.colorScheme.surface  // Blanco: disponible
        }
        val isClickable = isWorkingHour && !hasBlockout  // No clickeable si hay bloqueo

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                //.background(backgroundColor)
                .clickable(enabled = isClickable) { if (isClickable) onSlotClick(hour) }
        ) {
            Column {
                // Línea separadora superior
                if (isCurrentHour) {
                    LinearWavyProgressIndicator(
                        progress = { 0.949f },
                        amplitude = { 0.3f }, // Intensidad del ondulado
                        wavelength = 15.dp,
                        waveSpeed = 15.dp,
                        stopSize = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                else {
                    //HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                }

                // Si hay citas y no hay citas bloqueadas, las mostramos apiladas
                if (appointments.isNotEmpty()) {
                    // Si hay citas, las mostramos apiladas
                    appointments.forEach { appt ->
                        Box(

                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .fillMaxHeight()
                                .background(backgroundColor)
                        ) {
                            AppointmentCard(
                                appointment = appt,
                                onClick = { onAppointmentClick(appt) }
                            )
                        }
                    }
                } else {
                    // Si está vacío
                    if (isWorkingHour && !hasBlockout) {
                        // Espacio vacío "Disponible"
                        Box(modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .height(60.dp)
                            .fillMaxWidth()
                            .background(backgroundColor),)
                    } else if (hasBlockout) {
                        // Espacio bloqueado
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Horario Bloqueado",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFA500)
                            )
                        }
                    } else {
                        // Espacio "Cerrado"
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No Disponible",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}