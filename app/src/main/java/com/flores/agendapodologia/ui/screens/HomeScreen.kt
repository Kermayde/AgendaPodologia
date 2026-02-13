package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.ui.components.AppointmentCard
import com.flores.agendapodologia.ui.components.PatientItem
import com.flores.agendapodologia.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar // Para sumar/restar días
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddClick: () -> Unit
) {
    // 1. Observamos las Citas y la Fecha seleccionada
    val appointments by viewModel.appointments.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    // Formateador para mostrar "Hoy, 12 Feb"
    val dateFormatter = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Agenda Podológica") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                // --- BARRA DE FECHAS (NUEVO) ---
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Día Anterior
                        IconButton(onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                            cal.add(Calendar.DAY_OF_YEAR, -1)
                            viewModel.changeDate(cal.timeInMillis)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Anterior")
                        }

                        // Texto de la Fecha
                        Text(
                            text = dateFormatter.format(Date(selectedDate)).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Botón Día Siguiente
                        IconButton(onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                            viewModel.changeDate(cal.timeInMillis)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Siguiente")
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, "Nueva Cita")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            if (appointments.isEmpty()) {
                // Estado vacío más bonito
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No hay citas para este día", color = Color.Gray)
                }
            } else {
                // LISTA DE CITAS
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp) // Espacio para el FAB
                ) {
                    items(appointments) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onClick = {
                                // Aquí navegaremos al detalle más adelante
                            }
                        )
                    }
                }
            }
        }
    }
}