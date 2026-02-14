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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.model.PatientStatus
import com.flores.agendapodologia.viewmodel.HomeViewModel
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val patient by viewModel.currentPatient.collectAsState()
    val history by viewModel.lastAppointments.collectAsState() // Asegúrate de tener esto en el VM
    val context = LocalContext.current

    // Estado para el diálogo de confirmación de borrado
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Paciente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    // Botón Eliminar (Peligroso)
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        if (patient == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // 1. ENCABEZADO (Datos Personales)
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

                        if (patient!!.status == PatientStatus.BLOCKED) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text(" LISTA NEGRA ", modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                        try { context.startActivity(intent) } catch (e: Exception) {
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
                            try { context.startActivity(intent) } catch (e: Exception) {
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
                    onClick = { viewModel.togglePatientStatus(patient!!) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (patient!!.status == PatientStatus.BLOCKED) Color.Gray else MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Warning, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (patient!!.status == PatientStatus.BLOCKED) "Desbloquear Paciente" else "Reportar (Lista Negra)")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. HISTORIAL DE CITAS
                Text("Historial Completo", style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(history) { appointment ->
                        ListItem(
                            headlineContent = { Text(java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(appointment.date)) },
                            supportingContent = { Text(appointment.serviceType) },
                            trailingContent = { Text(appointment.status, style = MaterialTheme.typography.labelSmall) },
                            leadingContent = { Icon(Icons.Default.List, null) }
                        )
                    }
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