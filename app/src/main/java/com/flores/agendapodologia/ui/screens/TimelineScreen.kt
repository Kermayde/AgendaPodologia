package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.ClinicSettings
import com.flores.agendapodologia.ui.components.TimeSlot
import java.util.Calendar

@Composable
fun TimelineScreen(
    selectedDate: Long,
    appointments: List<Appointment>,
    clinicSettings: ClinicSettings,
    onAppointmentClick: (Appointment) -> Unit,
    onAddAtHourClick: (Int) -> Unit
) {
    // MEJORA 2: Agrupar citas por hora UNA SOLA VEZ (optimización)
    val appointmentsByHour = remember(appointments) {
        appointments.groupBy { appt ->
            Calendar.getInstance().apply { time = appt.date }
                .get(Calendar.HOUR_OF_DAY)
        }
    }

    // Rango visual DINÁMICO: base 6–21, pero se expande si hay citas fuera de ese rango.
    // Así nunca se pierden citas agendadas por accidente en horarios extremos.
    val baseStartHour = 6
    val baseEndHour = 21

    val startVisualHour = remember(appointmentsByHour) {
        if (appointmentsByHour.isEmpty()) baseStartHour
        else minOf(baseStartHour, appointmentsByHour.keys.min())
    }
    val endVisualHour = remember(appointmentsByHour) {
        if (appointmentsByHour.isEmpty()) baseEndHour
        else maxOf(baseEndHour, appointmentsByHour.keys.max())
    }

    val hours = (startVisualHour..endVisualHour).toList()

    // Estado del scroll
    val listState = rememberLazyListState()

    // MEJORA 1: AUTO-SCROLL A LA HORA ACTUAL (reactivo a cambios de fecha)
    LaunchedEffect(selectedDate) {  // Se ejecuta cuando cambia selectedDate
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        if (currentHour in startVisualHour..endVisualHour) {
            val index = currentHour - startVisualHour
            // Le restamos 1 al index (si se puede) para que la hora actual
            // no quede pegada al techo de la pantalla y se vea un poquito de la hora anterior.
            val targetIndex = if (index > 0) index - 1 else 0
            listState.scrollToItem(targetIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(hours.size) { index ->
            val hour = hours[index]

            // MEJORA 3: Detectar si es la hora actual para mostrar indicador visual
            val isCurrentHour = hour == Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY)

            // Usar las citas pre-agrupadas para mejor performance
            val appointmentsInThisHour = appointmentsByHour[hour] ?: emptyList()

            TimeSlot(
                hour = hour,
                // AHORA USAMOS LA CONFIGURACIÓN DINÁMICA DE FIREBASE
                isWorkingHour = clinicSettings.isWorkingHour(selectedDate, hour),
                appointments = appointmentsInThisHour,
                isCurrentHour = isCurrentHour,  // MEJORA 3: Pasar indicador
                onAppointmentClick = onAppointmentClick,
                onSlotClick = { clickedHour -> onAddAtHourClick(clickedHour) }
            )
        }
    }
}