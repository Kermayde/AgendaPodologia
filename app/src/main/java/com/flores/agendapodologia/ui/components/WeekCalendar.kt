package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeekCalendar(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onWeekChanged: (year: Int, month: Int) -> Unit = { _, _ -> } // Callback cuando la semana cambia de mes
) {
    // Calcular el lunes de la semana que contiene selectedDate
    fun getWeekStartDate(dateMillis: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY
        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    val currentWeekStartDate = remember(selectedDate) {
        getWeekStartDate(selectedDate)
    }

    // Crear lista de semanas (52 semanas antes y después = ~2 años)
    val weeks = remember {
        val weeksList = mutableListOf<Long>()
        val baseCalendar = Calendar.getInstance().apply {
            // Empezar desde el lunes de la semana actual
            timeInMillis = getWeekStartDate(System.currentTimeMillis())
            // Retroceder 52 semanas para empezar la lista desde ahí
            add(Calendar.WEEK_OF_YEAR, -52)
        }

        // Agregar 105 semanas en total (52 atrás + 1 actual + 52 adelante)
        repeat(105) {
            weeksList.add(baseCalendar.timeInMillis)
            baseCalendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        weeksList
    }

    // Encontrar el índice de la semana actual
    val weekIndex = remember(currentWeekStartDate) {
        val index = weeks.indexOfFirst { week ->
            getWeekStartDate(week) == currentWeekStartDate
        }
        if (index >= 0) index else 52 // 52 es el centro de la lista (semana actual)
    }

    val pagerState = rememberPagerState(
        initialPage = weekIndex,
        pageCount = { weeks.size }
    )

    val coroutineScope = rememberCoroutineScope()

    // Observar cambios en el pager para detectar si cambia el mes
    LaunchedEffect(pagerState.currentPage) {
        val currentWeekDate = weeks[pagerState.currentPage]
        val calendar = Calendar.getInstance().apply { timeInMillis = currentWeekDate }
        onWeekChanged(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
    }

    // Sincronizar el pager cuando cambia selectedDate (solo si la semana es diferente)
    LaunchedEffect(currentWeekStartDate) {
        val targetIndex = weeks.indexOfFirst { week ->
            getWeekStartDate(week) == currentWeekStartDate
        }.let { index ->
            if (index >= 0) index else pagerState.currentPage
        }

        if (pagerState.currentPage != targetIndex && targetIndex >= 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(targetIndex)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) { pageIndex ->
        val weekStartDate = weeks[pageIndex]
        WeekRow(
            weekStartDate = weekStartDate,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected
        )
    }
}

@Composable
private fun WeekRow(
    weekStartDate: Long,
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    // Obtener los 7 días de la semana
    val daysOfWeek = remember(weekStartDate) {
        val calendar = Calendar.getInstance().apply { timeInMillis = weekStartDate }
        val days = mutableListOf<Date>()
        repeat(7) {
            days.add(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        days
    }

    val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dayNumberFormat = SimpleDateFormat("d", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        daysOfWeek.forEach { date ->
            val isSelected = isSameDay(date.time, selectedDate)

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onDateSelected(date.time) }
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayNameFormat.format(date).uppercase().take(3),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dayNumberFormat.format(date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Función auxiliar para comparar si dos timestamps son el mismo día
fun isSameDay(date1: Long, date2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = date1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}