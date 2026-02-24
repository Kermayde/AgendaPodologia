package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.ui.components.ExpandableCalendarContainer
import com.flores.agendapodologia.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddClick: () -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
    // Observamos los estados del ViewModel
    val appointments by viewModel.appointments.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val clinicSettings by viewModel.clinicSettings.collectAsState()
    val displayedMonth by viewModel.displayedMonth.collectAsState()
    val isCalendarExpanded by viewModel.isCalendarExpanded.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                }
            )
        }

    ) { paddingValues ->
        // Contenedor principal con padding
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // MOSTRAR LA LÍNEA DE TIEMPO
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