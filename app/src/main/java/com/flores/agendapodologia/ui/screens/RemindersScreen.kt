package com.flores.agendapodologia.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.model.ReminderPreference
import com.flores.agendapodologia.viewmodel.HomeViewModel
import com.flores.agendapodologia.viewmodel.PendingReminder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val reminders by viewModel.pendingReminders.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Recordatorios") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay recordatorios pendientes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Los recordatorios aparecerán aquí un día\nantes de cada cita agendada con anticipación",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reminders, key = { it.appointment.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onSendReminder = {
                            when (reminder.reminderPreference) {
                                ReminderPreference.WHATSAPP -> {
                                    val phone = formatPhoneForWhatsapp(reminder.patientPhone)
                                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                    val timeStr = timeFormat.format(reminder.appointment.date)
                                    val message = URLEncoder.encode(
                                        "Hola ${reminder.patientName}, nos comunicamos de Salud Integral para Tus Pies, " +
                                                "para recordarte de tu cita de ${reminder.appointment.serviceType} " +
                                                "mañana a las $timeStr. " +
                                                "¿Nos confirmas tu asistencia?",
                                        "UTF-8"
                                    )
                                    val url = "https://api.whatsapp.com/send?phone=$phone&text=$message"
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(url)
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                ReminderPreference.LLAMADA -> {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${reminder.patientPhone}")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "No se puede realizar la llamada", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                else -> {}
                            }
                        },
                        onMarkSent = {
                            viewModel.markReminderSent(reminder.appointment.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: PendingReminder,
    onSendReminder: () -> Unit,
    onMarkSent: () -> Unit
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeStr = timeFormat.format(reminder.appointment.date)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nombre y hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${reminder.appointment.serviceType} — $timeStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Podólogo: ${reminder.appointment.podiatristName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de acción principal (WhatsApp o Llamada)
                when (reminder.reminderPreference) {
                    ReminderPreference.WHATSAPP -> {
                        Button(
                            onClick = onSendReminder,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366)
                            )
                        ) {
                            Text("Enviar WhatsApp", color = Color.White)
                        }
                    }
                    ReminderPreference.LLAMADA -> {
                        Button(
                            onClick = onSendReminder,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Llamar")
                        }
                    }
                    else -> {}
                }

                // Botón marcar como enviado
                FilledTonalButton(
                    onClick = onMarkSent
                ) {
                    Icon(Icons.Default.Check, "Marcar como avisado", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Avisado")
                }
            }
        }
    }
}


