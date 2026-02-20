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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.ui.components.AppointmentCard
import com.flores.agendapodologia.ui.components.DailySummaryCard
import com.flores.agendapodologia.ui.components.DatePickerModal
import com.flores.agendapodologia.ui.components.PatientItem
import com.flores.agendapodologia.ui.components.WeekCalendar
import com.flores.agendapodologia.ui.navigation.AppScreens
import com.flores.agendapodologia.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar // Para sumar/restar días
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddClick: () -> Unit,
    onAppointmentClick: (Appointment) -> Unit,
    onOpenDirectory: () -> Unit,
    navController: NavController
) {
    // 1. Observamos las Citas y la Fecha seleccionada
    val appointments by viewModel.appointments.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val clinicSettings by viewModel.clinicSettings.collectAsState()

    // Estado para el selector de mes (TopBar)
    var showMonthPicker by remember { mutableStateOf(false) }

    // Formateador para "Febrero 2026"
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        // Título clickeable para cambiar mes/año
                        TextButton(
                            onClick = { showMonthPicker = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Text(
                                text = monthYearFormat.format(Date(selectedDate)).replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.DateRange, null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    actions = {
                        IconButton(onClick = onOpenDirectory) {
                            Icon(Icons.Default.Person, "Directorio", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(onClick = { navController.navigate(AppScreens.Settings.route) }) { // <-- Asegúrate de pasar una función lambda 'onOpenSettings: () -> Unit' en los parámetros de HomeScreen, o usa el navController si lo tienes a la mano ahí.
                            Icon(Icons.Default.Settings, "Configuración", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                )

                // --- NUEVO COMPONENTE SEMANAL ---
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    WeekCalendar(
                        selectedDate = selectedDate,
                        onDateSelected = { newDate ->
                            viewModel.changeDate(newDate)
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, "Nueva Cita")
            }
        }

    ) { paddingValues ->
        // Contenedor principal con padding
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 1. MOSTRAR EL DASHBOARD FINANCIERO
            // Solo lo mostramos si ya hay dinero cobrado, para no estorbar cuando está en ceros
            if (dailySummary.total > 0) {
                DailySummaryCard(summary = dailySummary)
            }

            // 2. MOSTRAR LA LÍNEA DE TIEMPO
            Box(modifier = Modifier.weight(1f)) {
                TimelineScreen(
                    selectedDate = selectedDate,
                    appointments = appointments,
                    clinicSettings = clinicSettings,
                    onAppointmentClick = onAppointmentClick,
                    onAddAtHourClick = { clickedHour ->
                        viewModel.setPreselectedTime(selectedDate, clickedHour)
                        onAddClick()
                    }
                )
            }
        }
    }

    // Diálogo para cambiar de mes (reutilizamos tu DatePickerModal)
    if (showMonthPicker) {
        DatePickerModal(
            onDateSelected = { utcMillis ->
                if (utcMillis != null) {
                    // Ajuste de zona horaria (mismo truco que usamos antes)
                    val cal = Calendar.getInstance()
                    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    utcCal.timeInMillis = utcMillis

                    cal.set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                    cal.set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                    cal.set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))

                    viewModel.changeDate(cal.timeInMillis)
                }
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}