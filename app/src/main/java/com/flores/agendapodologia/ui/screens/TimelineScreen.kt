package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    // Definimos el rango visual fijo de la agenda (ej. de 6 AM a 9 PM)
    // Esto asegura que la pantalla siempre se vea igual de larga, independientemente
    // de si abren a las 8 o a las 10. Las horas cerradas se pintarán grises.
    val startVisualHour = 6
    val endVisualHour = 21
    val hours = (startVisualHour..endVisualHour).toList()

    // Estado del scroll
    val listState = rememberLazyListState()

    // AUTO-SCROLL A LA HORA ACTUAL
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
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

            // Filtramos las citas que ocurren en ESTA hora (ej: 10:00 - 10:59)
            val appointmentsInThisHour = appointments.filter { appt ->
                val cal = Calendar.getInstance().apply { time = appt.date }
                cal.get(Calendar.HOUR_OF_DAY) == hour
            }

            TimeSlot(
                hour = hour,
                // AHORA USAMOS LA CONFIGURACIÓN DINÁMICA DE FIREBASE
                isWorkingHour = clinicSettings.isWorkingHour(selectedDate, hour),
                appointments = appointmentsInThisHour,
                onAppointmentClick = onAppointmentClick,
                onSlotClick = { clickedHour -> onAddAtHourClick(clickedHour) }
            )
        }
    }
}