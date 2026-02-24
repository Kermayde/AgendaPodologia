package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

    // Flag para suprimir onWeekChanged durante scrolls programáticos
    // (ej: cuando el carrusel de meses cambió selectedDate y eso movió la tira semanal)
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // Observar cambios en el pager para detectar si cambia el mes
    // Solo reportamos cuando el usuario desliza manualmente la tira semanal
    LaunchedEffect(pagerState.settledPage) {
        if (isProgrammaticScroll) {
            // El scroll fue programático (vino del carrusel/grid), no sobrescribimos el mes
            isProgrammaticScroll = false
        } else {
            // El usuario deslizó manualmente — reportamos el mes del jueves de la semana
            // (jueves es más representativo: ISO 8601 usa jueves para determinar la semana del mes)
            val currentWeekDate = weeks[pagerState.settledPage]
            val calendar = Calendar.getInstance().apply {
                timeInMillis = currentWeekDate
                add(Calendar.DAY_OF_YEAR, 3) // Lunes + 3 = Jueves
            }
            onWeekChanged(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
        }
    }

    // Sincronizar el pager cuando cambia selectedDate (solo si la semana es diferente)
    LaunchedEffect(currentWeekStartDate) {
        val targetIndex = weeks.indexOfFirst { week ->
            getWeekStartDate(week) == currentWeekStartDate
        }.let { index ->
            if (index >= 0) index else pagerState.currentPage
        }

        if (pagerState.currentPage != targetIndex && targetIndex >= 0) {
            isProgrammaticScroll = true
            coroutineScope.launch {
                pagerState.animateScrollToPage(targetIndex)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
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

                    .clickable { onDateSelected(date.time) }
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayNameFormat.format(date).uppercase().take(3),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(1.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .size(36.dp)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                    contentAlignment = Alignment.Center,

                ) {
                    Text(
                        text = dayNumberFormat.format(date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                    )
                }
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