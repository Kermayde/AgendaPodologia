package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.ui.components.WarrantyBanner
import com.flores.agendapodologia.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val appointment by viewModel.currentDetailAppointment.collectAsState()
    val warrantyState by viewModel.warrantyStatus.collectAsState() // <--- Observamos el estado
    // Observamos la lista
    val historyList by viewModel.lastAppointments.collectAsState()

    // Estado local para las notas que estás escribiendo
    var currentNotes by remember { mutableStateOf(appointment?.notes ?: "") }

    // Formateador de fechas
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(appointment?.patientName ?: "Detalle", style = MaterialTheme.typography.titleMedium)
                        Text(appointment?.serviceType ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                },
                actions = {
                    // Botón Guardar en la barra superior
                    IconButton(onClick = {
                        viewModel.saveNotes(currentNotes) { onBack() }
                    }) {
                        Icon(Icons.Default.Check, "Guardar Notas")
                    }
                }
            )
        }
    ) { padding ->
        if (appointment == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Banner de Garantía (Lo ponemos al principio para que sea evidente)
                WarrantyBanner(warrantyState = warrantyState)

                // 2. Si la cita actual es de "Correcciones" y hay garantía, avisamos que es GRATIS
                if (appointment?.serviceType == "Correcciones" && warrantyState.isActive) {
                    Text(
                        text = "✨ Esta cita no debería tener costo por garantía.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // SECCIÓN 1: Historial Reciente
                Text("Historial Reciente (Últimas 3 visitas)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (historyList.isNotEmpty()) {
                    // Iteramos sobre la lista para mostrar cada cita pasada
                    historyList.forEach { pastAppointment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp), // Separación entre tarjetas
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.List, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dateFormat.format(pastAppointment.date),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = pastAppointment.notes.ifEmpty { "Sin notas." },
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3 // Limitamos líneas para que no ocupe toda la pantalla
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Atendió: ${pastAppointment.podiatristName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.align(Alignment.End),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // Caso vacío
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No hay visitas anteriores registradas.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(24.dp))

                // SECCIÓN 2: Sesión Actual (Lo importante)
                Text("Notas de la Sesión Actual", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Escribe aquí el tratamiento, observaciones o recomendaciones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = currentNotes,
                    onValueChange = { currentNotes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), // Campo grande para escribir a gusto
                    label = { Text("Tratamiento / Observaciones") },
                    placeholder = { Text("Ej: Se realizó corte de uña encarnada en pie derecho. Paciente reportó dolor leve...") },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.saveNotes(currentNotes) { onBack() } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Cambios")
                }
            }
        }
    }
}