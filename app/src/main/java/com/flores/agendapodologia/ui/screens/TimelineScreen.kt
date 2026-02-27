package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.ClinicSettings
import com.flores.agendapodologia.ui.components.TimeSlot
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// ─────────────────────────────────────────────────────────────────
//  Constantes
// ─────────────────────────────────────────────────────────────────

private const val BASE_START_HOUR = 6
private const val BASE_END_HOUR = 21

/** Altura aproximada de FloatingNavBar + padding externo. */
val NAV_BAR_OFFSET = 90.dp

// ─────────────────────────────────────────────────────────────────
//  TimelineScreen
// ─────────────────────────────────────────────────────────────────

@Composable
fun TimelineScreen(
    selectedDate: Long,
    appointments: List<Appointment>,
    clinicSettings: ClinicSettings,
    onAppointmentClick: (Appointment) -> Unit,
    onAddAtHourClick: (Int) -> Unit
) {
    // ── Datos derivados (se recalculan solo cuando cambian las citas) ──

    val zoneId = remember { ZoneId.systemDefault() }

    val appointmentsByHour = remember(appointments) {
        appointments.groupBy { appt ->
            Instant.ofEpochMilli(appt.date.time)
                .atZone(zoneId)
                .hour
        }
    }

    // Rango visual dinámico: base 6–21, se expande si hay citas fuera de ese rango.
    val (startVisualHour, endVisualHour) = remember(appointmentsByHour) {
        if (appointmentsByHour.isEmpty()) {
            BASE_START_HOUR to BASE_END_HOUR
        } else {
            minOf(BASE_START_HOUR, appointmentsByHour.keys.min()) to
                    maxOf(BASE_END_HOUR, appointmentsByHour.keys.max())
        }
    }

    val hours = remember(startVisualHour, endVisualHour) {
        (startVisualHour..endVisualHour).toList()
    }

    // Hora actual reactiva: se recalcula al cambiar de fecha y se actualiza
    // periódicamente solo mientras la pantalla es visible (STARTED).
    var currentSystemHour by remember { mutableIntStateOf(LocalDateTime.now().hour) }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(selectedDate, lifecycleOwner) {
        // Recalcular inmediatamente al cambiar de fecha.
        currentSystemHour = LocalDateTime.now().hour
        // El bloque se ejecuta solo en STARTED y se cancela al pasar a STOPPED,
        // evitando trabajo innecesario cuando la pantalla no es visible.
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                delay(30_000L)
                currentSystemHour = LocalDateTime.now().hour
            }
        }
    }

    // Pre-calcular qué horas son laborales para evitar hacerlo dentro del LazyColumn.
    val workingHourFlags = remember(selectedDate, clinicSettings, hours) {
        hours.associateWith { hour -> clinicSettings.isWorkingHour(selectedDate, hour) }
    }

    // ── Scroll automático a la hora actual ──

    val listState = rememberLazyListState()

    LaunchedEffect(selectedDate) {
        val selectedHour = Instant.ofEpochMilli(selectedDate)
            .atZone(zoneId)
            .hour

        if (selectedHour in startVisualHour..endVisualHour) {
            val index = (selectedHour - startVisualHour).coerceAtLeast(0)
            listState.scrollToItem(index)
        }
    }

    // ── Padding inferior para no quedar tapado por la FloatingNavBar ──

    val bottomPadding = with(LocalDensity.current) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    } + NAV_BAR_OFFSET

    // ── Lista ──

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = bottomPadding)
    ) {
        items(items = hours, key = { it }) { hour ->
            TimeSlot(
                hour = hour,
                isWorkingHour = workingHourFlags[hour] ?: false,
                appointments = appointmentsByHour[hour] ?: emptyList(),
                isCurrentHour = hour == currentSystemHour,
                onAppointmentClick = onAppointmentClick,
                onSlotClick = { onAddAtHourClick(hour) }
            )
        }
    }
}