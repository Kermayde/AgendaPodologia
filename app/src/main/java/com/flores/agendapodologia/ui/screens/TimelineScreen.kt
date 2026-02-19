package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.ui.components.TimeSlot
import com.flores.agendapodologia.util.WorkingHours
import java.util.Calendar
import java.util.Date

@Composable
fun TimelineScreen(
    selectedDate: Long,
    appointments: List<Appointment>,
    onAppointmentClick: (Appointment) -> Unit,
    onAddAtHourClick: (Int) -> Unit
) {
    // Rango de horas a mostrar (6 am a 9 pm)
    val hours = (WorkingHours.START_HOUR..WorkingHours.END_HOUR).toList()

    // Estado del scroll
    val listState = rememberLazyListState()

    // AUTO-SCROLL A LA HORA ACTUAL
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // Si la hora actual está dentro de nuestro rango visual
        if (currentHour in WorkingHours.START_HOUR..WorkingHours.END_HOUR) {
            // Calculamos el índice (hora actual - hora inicio)
            val index = currentHour - WorkingHours.START_HOUR
            // Hacemos scroll (con un pequeño offset para que no quede pegado al borde)
            listState.scrollToItem(index)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(hours.size) { index ->
            val hour = hours[index]

            // Filtramos las citas que ocurren en ESTA hora (ej: 10:00 - 10:59)
            val appointmentsInThisHour = appointments.filter { appt ->
                val cal = Calendar.getInstance().apply { time = appt.date }
                cal.get(Calendar.HOUR_OF_DAY) == hour
            }

            TimeSlot(
                hour = hour,
                isWorkingHour = WorkingHours.isWorkingHour(selectedDate, hour),
                appointments = appointmentsInThisHour,
                onAppointmentClick = onAppointmentClick,
                onSlotClick = { clickedHour -> onAddAtHourClick(clickedHour) }
            )
        }
    }
}