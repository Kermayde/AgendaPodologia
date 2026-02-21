package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.ui.components.ExpandableCalendarContainer
import com.flores.agendapodologia.ui.components.DailySummaryCard
import com.flores.agendapodologia.ui.navigation.AppScreens
import com.flores.agendapodologia.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddClick: () -> Unit,
    onAppointmentClick: (Appointment) -> Unit,
    onOpenDirectory: () -> Unit,
    navController: NavController
) {
    // Observamos los estados del ViewModel
    val appointments by viewModel.appointments.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val clinicSettings by viewModel.clinicSettings.collectAsState()
    val displayedMonth by viewModel.displayedMonth.collectAsState()
    val isCalendarExpanded by viewModel.isCalendarExpanded.collectAsState()

    Scaffold(
        topBar = {
            // Usar el nuevo componente ExpandableCalendarContainer
            ExpandableCalendarContainer(
                selectedDate = selectedDate,
                displayedMonth = displayedMonth,
                isExpanded = isCalendarExpanded,
                onToggleExpanded = { viewModel.toggleCalendarExpanded() },
                onDateSelected = { newDate ->
                    viewModel.changeDate(newDate)
                },
                onMonthChanged = { year, month ->
                    viewModel.changeDisplayedMonth(year, month)
                },
                onWeekMonthChanged = { year, month ->
                    viewModel.updateDisplayedMonthFromWeek(year, month)
                },
                onGoToToday = {
                    viewModel.goToToday()
                },
                onOpenDirectory = onOpenDirectory,
                onOpenSettings = { navController.navigate(AppScreens.Settings.route) }
            )
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

}