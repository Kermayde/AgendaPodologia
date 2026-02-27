package com.flores.agendapodologia.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.net.toUri
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.AppointmentStatus
import com.flores.agendapodologia.model.PatientStatus
import com.flores.agendapodologia.model.ReminderPreference
import com.flores.agendapodologia.ui.components.ReminderPreferenceSelector
import com.flores.agendapodologia.viewmodel.HomeViewModel
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit,
    onAppointmentClick: (Appointment) -> Unit = {}
) {
    val patient by viewModel.currentPatient.collectAsState()
    val history by viewModel.lastAppointments.collectAsState()
    val upcomingAppointments by viewModel.upcomingAppointments.collectAsState()
    val context = LocalContext.current

    // Formateador de fecha
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    // Estado para el diálogo de confirmación de borrado
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Estado para el diálogo de motivo de bloqueo
    var showBlockReasonDialog by remember { mutableStateOf(false) }
    var blockReasonText by remember { mutableStateOf("") }

    // ESTADO DE EDICIÓN
    var isEditing by remember { mutableStateOf(false) }

    // Estados temporales para los campos de texto (buffer de edición)
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editReminderPreference by remember { mutableStateOf(ReminderPreference.WHATSAPP) }

    // Sincronizar buffer cuando entramos al modo edición
    LaunchedEffect(isEditing) {
        if (isEditing && patient != null) {
            editName = patient!!.name
            editPhone = patient!!.phone
            editReminderPreference = patient!!.reminderPreference
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editando Paciente" else "Detalle del Paciente") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    // Si estamos editando, el botón atrás funciona como "Cancelar"
                    IconButton(onClick = {
                        if (isEditing) isEditing = false else onBack()
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (!isEditing) {
                        // MODO VISUALIZACIÓN: Botón Editar y Eliminar
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, "Editar")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        // MODO EDICIÓN: Botón Guardar
                        IconButton(onClick = {
                            if (patient != null) {
                                val updatedPatient = patient!!.copy(
                                    name = editName.trim(),
                                    phone = editPhone.trim(),
                                    reminderPreference = editReminderPreference
                                )
                                viewModel.updatePatient(updatedPatient) {
                                    isEditing = false // Salir del modo edición al terminar
                                }
                            }
                        }) {
                            Icon(Icons.Default.Check, "Guardar Cambios", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (patient == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {

                // 1. TARJETA DE DATOS (Ahora mutante)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (patient!!.status == PatientStatus.BLOCKED)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isEditing) {
                            // --- CAMPOS EDITABLES ---
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Nombre Completo") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                label = { Text("Teléfono") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ReminderPreferenceSelector(
                                selected = editReminderPreference,
                                onSelected = { editReminderPreference = it }
                            )
                        } else {
                            // --- VISTA SOLO LECTURA (Lo que ya tenías) ---
                            Text(
                                text = patient!!.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = patient!!.phone,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aviso: ${when (patient!!.reminderPreference) {
                                    ReminderPreference.WHATSAPP -> "WhatsApp"
                                    ReminderPreference.LLAMADA -> "Llamada"
                                    ReminderPreference.NINGUNO -> "Ninguno"
                                }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Badge de Lista Negra (Siempre visible si aplica)
                        if (patient!!.status == PatientStatus.BLOCKED) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text(" LISTA NEGRA ", modifier = Modifier.padding(4.dp))
                            }
                            if (patient!!.blockReason.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Motivo: ${patient!!.blockReason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ocultamos botones de acción rápida mientras se edita para no distraer
                if (!isEditing) {
                    // 2. ACCIONES RÁPIDAS (Llamar / WhatsApp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Botón LLAMAR
                        Button(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${patient!!.phone}")
                            }
                            try { context.startActivity(intent) } catch (_: Exception) {
                                Toast.makeText(context, "No se puede llamar", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Call, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Llamar")
                        }

                        // Botón WHATSAPP
                        Button(
                            onClick = {
                                val phone = formatPhoneForWhatsapp(patient!!.phone)
                                val message = URLEncoder.encode("Hola ${patient!!.name}, le escribimos de la Clínica Podológica.", "UTF-8")
                                val url = "https://api.whatsapp.com/send?phone=$phone&text=$message"

                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(url)
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {
                                    Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Default.Email, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WhatsApp")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. BLOQUEO / LISTA NEGRA
                    OutlinedButton(
                        onClick = {
                            if (patient!!.status == PatientStatus.BLOCKED) {
                                // Desbloquear directamente
                                viewModel.togglePatientStatus(patient!!)
                            } else {
                                // Mostrar diálogo para pedir motivo
                                blockReasonText = ""
                                showBlockReasonDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (patient!!.status == PatientStatus.BLOCKED) Color.Gray else MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Warning, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (patient!!.status == PatientStatus.BLOCKED) "Desbloquear Paciente" else "Reportar (Lista Negra)")
                    }
                } else {
                    Text(
                        text = "⚠️ Al cambiar el nombre, se actualizará en todas las citas históricas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. PRÓXIMAS CITAS
                if (upcomingAppointments.isNotEmpty()) {
                    Text("Próximas Citas", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    upcomingAppointments.forEach { appointment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable { onAppointmentClick(appointment) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dateFormat.format(appointment.date),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = appointment.serviceType,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Podólogo: ${appointment.podiatristName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Ver detalle",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 5. HISTORIAL DE CITAS PASADAS
                Text("Historial Completo", style = MaterialTheme.typography.titleMedium)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Filtro: Mostrar solo citas que usaron garantía
                var showOnlyWarranty by remember { mutableStateOf(false) }
                val warrantyCount = history.count { it.usedWarranty }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Mostrar solo citas por garantía", style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$warrantyCount", style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(checked = showOnlyWarranty, onCheckedChange = { showOnlyWarranty = it })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val displayList = if (showOnlyWarranty) history.filter { it.usedWarranty } else history

                if (displayList.isNotEmpty()) {
                    displayList.forEach { appointment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable { onAppointmentClick(appointment) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = dateFormat.format(appointment.date),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (appointment.usedWarranty) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "[GARANTÍA]",
                                                color = Color(0xFF2E7D32),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = appointment.serviceType,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val statusText = when (appointment.status) {
                                        AppointmentStatus.FINALIZADA -> "Finalizada"
                                        AppointmentStatus.CANCELADA -> "Cancelada"
                                        AppointmentStatus.NO_ASISTIO -> "No Asistió"
                                        AppointmentStatus.PENDIENTE -> "Pendiente"
                                    }
                                    val statusColor = when (appointment.status) {
                                        AppointmentStatus.FINALIZADA -> Color(0xFF2E7D32)
                                        AppointmentStatus.CANCELADA, AppointmentStatus.NO_ASISTIO -> MaterialTheme.colorScheme.error
                                        AppointmentStatus.PENDIENTE -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Ver detalle",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No hay citas registradas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN DE BORRADO
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar Paciente?") },
            text = { Text("Esta acción borrará al paciente '${patient?.name}' y TODAS sus citas agendadas. No se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCurrentPatient {
                            showDeleteDialog = false
                            onBack() // Regresa al directorio tras borrar
                            Toast.makeText(context, "Paciente eliminado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar Todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // DIÁLOGO PARA MOTIVO DE BLOQUEO
    if (showBlockReasonDialog) {
        AlertDialog(
            onDismissRequest = { showBlockReasonDialog = false },
            title = { Text("Reportar Paciente") },
            text = {
                Column {
                    Text("¿Por qué deseas agregar a este paciente a la lista negra?")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = blockReasonText,
                        onValueChange = { blockReasonText = it },
                        label = { Text("Motivo") },
                        placeholder = { Text("Ej: No se presentó 3 veces seguidas...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.togglePatientStatus(patient!!, blockReasonText.trim())
                        showBlockReasonDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Bloquear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockReasonDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// Función auxiliar para formatear número mexicano
fun formatPhoneForWhatsapp(phone: String): String {
    // 1. Quitamos espacios, guiones y paréntesis
    var cleanPhone = phone.replace(Regex("[^0-9]"), "")

    // 2. Si no tiene código de país y parece número de México (10 dígitos), agregamos +52
    if (cleanPhone.length == 10) {
        cleanPhone = "52$cleanPhone"
    }

    return cleanPhone
}