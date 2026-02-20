package com.flores.agendapodologia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.model.DaySchedule
import com.flores.agendapodologia.viewmodel.HomeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentSettings by viewModel.clinicSettings.collectAsState()

    // Estado local para editar antes de guardar
    var editedSettings by remember { mutableStateOf(currentSettings) }
    // Bandera para saber si el usuario ha hecho cambios
    var hasChanges by remember { mutableStateOf(false) }

    // Solo sincronizar cuando los cambios llegan de Firebase pero el usuario no ha editado nada
    LaunchedEffect(currentSettings) {
        if (!hasChanges) {
            editedSettings = currentSettings
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Horarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.updateSettings(editedSettings) {
                            Toast.makeText(context, "Horarios actualizados", Toast.LENGTH_SHORT).show()
                            hasChanges = false
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.Check, "Guardar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Configura los días y turnos de atención. Los cambios se reflejarán inmediatamente en la agenda.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // LUNES
            DayScheduleEditor(dayName = "Lunes", schedule = editedSettings.monday) { newSched ->
                editedSettings = editedSettings.copy(monday = newSched)
                hasChanges = true
            }
            // MARTES
            DayScheduleEditor(dayName = "Martes", schedule = editedSettings.tuesday) { newSched ->
                editedSettings = editedSettings.copy(tuesday = newSched)
                hasChanges = true
            }
            // MIÉRCOLES
            DayScheduleEditor(dayName = "Miércoles", schedule = editedSettings.wednesday) { newSched ->
                editedSettings = editedSettings.copy(wednesday = newSched)
                hasChanges = true
            }
            // JUEVES
            DayScheduleEditor(dayName = "Jueves", schedule = editedSettings.thursday) { newSched ->
                editedSettings = editedSettings.copy(thursday = newSched)
                hasChanges = true
            }
            // VIERNES
            DayScheduleEditor(dayName = "Viernes", schedule = editedSettings.friday) { newSched ->
                editedSettings = editedSettings.copy(friday = newSched)
                hasChanges = true
            }
            // SÁBADO
            DayScheduleEditor(dayName = "Sábado", schedule = editedSettings.saturday) { newSched ->
                editedSettings = editedSettings.copy(saturday = newSched)
                hasChanges = true
            }
            // DOMINGO
            DayScheduleEditor(dayName = "Domingo", schedule = editedSettings.sunday) { newSched ->
                editedSettings = editedSettings.copy(sunday = newSched)
                hasChanges = true
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- SUBCOMPONENTES DE LA INTERFAZ ---

@Composable
fun DayScheduleEditor(
    dayName: String,
    schedule: DaySchedule,
    onScheduleChange: (DaySchedule) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.isOpen) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFEEEEEE)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Encabezado del Día (Lunes, Martes...) y Switch de Abierto/Cerrado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (schedule.isOpen) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
                Switch(
                    checked = schedule.isOpen,
                    onCheckedChange = { onScheduleChange(schedule.copy(isOpen = it)) }
                )
            }

            // Si está abierto, mostramos los controles de horas
            if (schedule.isOpen) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // TURNO 1 (Mañana)
                Text("Turno 1 (Mañana)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                HourStepperRow("Apertura", schedule.shift1Start) { onScheduleChange(schedule.copy(shift1Start = it)) }
                HourStepperRow("Cierre", schedule.shift1End) { onScheduleChange(schedule.copy(shift1End = it)) }

                Spacer(modifier = Modifier.height(12.dp))

                // TURNO 2 (Tarde)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Turno 2 (Tarde)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Checkbox(
                        checked = schedule.hasShift2,
                        onCheckedChange = { onScheduleChange(schedule.copy(hasShift2 = it)) }
                    )
                }

                if (schedule.hasShift2) {
                    HourStepperRow("Reapertura", schedule.shift2Start) { onScheduleChange(schedule.copy(shift2Start = it)) }
                    HourStepperRow("Cierre Final", schedule.shift2End) { onScheduleChange(schedule.copy(shift2End = it)) }
                }
            } else {
                Text("Día de descanso", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

// Componente de botones [ - ] 10:00 [ + ]
@Composable
fun HourStepperRow(label: String, currentHour: Int, onHourChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (currentHour > 0) onHourChange(currentHour - 1) },
                modifier = Modifier.size(32.dp)
            ) { Icon(Icons.Default.Delete, "Menos") }

            Text(
                text = String.format(Locale.getDefault(), "%02d:00", currentHour),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = { if (currentHour < 23) onHourChange(currentHour + 1) },
                modifier = Modifier.size(32.dp)
            ) { Icon(Icons.Default.Add, "Más") }
        }
    }
}