package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.ui.components.ExpandableCalendarContainer
import com.flores.agendapodologia.viewmodel.HomeViewModel

// ─────────────────────────────────────────────────────────────────
//  HomeScreen — pantalla principal con calendario y línea de tiempo
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddClick: () -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
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
            ExpandableCalendarContainer(
                selectedDate = selectedDate,
                displayedMonth = displayedMonth,
                isExpanded = isCalendarExpanded,
                onToggleExpanded = viewModel::toggleCalendarExpanded,
                onDateSelected = viewModel::changeDate,
                onMonthChanged = viewModel::changeDisplayedMonth,
                onWeekMonthChanged = viewModel::updateDisplayedMonthFromWeek,
                onGoToToday = viewModel::goToToday
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
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