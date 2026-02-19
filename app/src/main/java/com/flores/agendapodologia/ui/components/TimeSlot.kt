package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flores.agendapodologia.model.Appointment

@Composable
fun TimeSlot(
    hour: Int,
    isWorkingHour: Boolean,
    appointments: List<Appointment>,
    onAppointmentClick: (Appointment) -> Unit,
    onSlotClick: (Int) -> Unit // Para agendar rápido en esta hora (Futuro)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Altura dinámica según contenido
    ) {
        // COLUMNA 1: La Hora (ej: 10:00)
        Column(
            modifier = Modifier
                .width(60.dp)
                .padding(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = String.format("%02d:00", hour),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // COLUMNA 2: El Contenido (Citas o Hueco)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    if (isWorkingHour) Color.Transparent
                    else Color(0xFFEEEEEE) // Gris si no es horario laboral
                )
                .clickable { if (isWorkingHour) onSlotClick(hour) }
        ) {
            Column {
                // Línea separadora superior
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                if (appointments.isNotEmpty()) {
                    // Si hay citas, las mostramos apiladas
                    appointments.forEach { appt ->
                        AppointmentCard(
                            appointment = appt,
                            onClick = { onAppointmentClick(appt) }
                        )
                    }
                } else {
                    // Si está vacío
                    if (isWorkingHour) {
                        // Espacio vacío "Disponible"
                        Box(modifier = Modifier.height(60.dp).fillMaxWidth())
                    } else {
                        // Espacio "Cerrado"
                        Box(
                            modifier = Modifier.height(40.dp).fillMaxWidth(),
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