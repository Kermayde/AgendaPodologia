package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.AppointmentStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit
) {
    // Formateador de hora (ej: 10:30)
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = timeFormatter.format(appointment.date)

    // Estilo condicional
    val isBlockout = appointment.isBlockout  // ← NUEVO
    val isFinished = appointment.status == AppointmentStatus.FINALIZADA && !isBlockout  // No aplica para bloqueos
    val cardAlpha = if (isFinished) 0.6f else 1f // Más transparente si terminó
    val textDecoration = if (isFinished) TextDecoration.LineThrough else null

    // Color distintivo según si es bloqueo o normal
    val (containerColor, borderColor) = if (isBlockout) {
        Pair(Color(0xFFFFB74D).copy(alpha = 0.2f), BorderStroke(2.dp, Color(0xFFFFA500)))  // Naranja para bloqueos
    } else {
        val bgColor = if (isFinished) Color.LightGray.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        Pair(bgColor, null)  // Color normal para citas
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFinished) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp).alpha(cardAlpha), // Aplicamos transparencia
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
                    modifier = Modifier.padding(end = 16.dp)
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
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

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
                        text = appointment.podiatristName.first().toString(), // Inicial "C" o "K"
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