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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun MonthGridCalendar(
    displayedYear: Int,
    displayedMonth: Int, // 0-11
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onMonthChanged: (year: Int, month: Int) -> Unit,
    onMonthChangedByPager: (year: Int, month: Int) -> Unit = { _, _ -> } // Callback cuando cambia por pager
) {
    val totalMonths = 1200 // Rango amplio: ~100 años de meses
    val middleIndex = totalMonths / 2

    // Calcular el índice inicial para el mes/año actual
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2000) // Año base
        set(Calendar.MONTH, 0)
    }
    val baseYear = cal.get(Calendar.YEAR)
    val baseMonth = cal.get(Calendar.MONTH)

    val initialIndex = (displayedYear - baseYear) * 12 + (displayedMonth - baseMonth) + middleIndex

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { totalMonths }
    )

    val coroutineScope = rememberCoroutineScope()

    // Flag para suprimir onMonthChangedByPager durante scrolls programáticos
    // (ej: cuando el carrusel cambió displayedMonth y eso movió el pager)
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // Observar cambios en el pagerState para actualizar el mes mostrado (solo actualizar displayedMonth, no selectedDate)
    // Usamos settledPage para evitar que páginas intermedias durante una animación disparen actualizaciones falsas
    // Solo reportamos cuando el usuario desliza manualmente el grid
    LaunchedEffect(pagerState.settledPage) {
        if (isProgrammaticScroll) {
            isProgrammaticScroll = false
        } else {
            val monthOffset = pagerState.settledPage - middleIndex
            val newYear = baseYear + (monthOffset / 12)
            val newMonth = ((monthOffset % 12) + 12) % 12
            onMonthChangedByPager(newYear, newMonth)
        }
    }

    // Sincronizar el pager cuando cambia displayedMonth desde el carrusel
    LaunchedEffect(displayedYear, displayedMonth) {
        val targetIndex = (displayedYear - baseYear) * 12 + (displayedMonth - baseMonth) + middleIndex
        // Solo animar si es diferente al actual
        if (pagerState.currentPage != targetIndex) {
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
            .padding(vertical = 8.dp)
    ) { pageIndex ->
        val monthOffset = pageIndex - middleIndex
        val year = baseYear + (monthOffset / 12)
        val month = ((monthOffset % 12) + 12) % 12

        MonthGridPage(
            year = year,
            month = month,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected
        )
    }
}

@Composable
private fun MonthGridPage(
    year: Int,
    month: Int, // 0-11
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, etc.
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Ajustar para comenzar en lunes (DAY_OF_WEEK = 2)
    val startOffset = if (firstDayOfWeek == 1) 6 else firstDayOfWeek - 2

    val dayNames = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Fila de nombres de días
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayNames.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid de días
        val weeks = mutableListOf<List<Int?>>()
        val currentWeek = mutableListOf<Int?>()

        // Agregar espacios vacíos al inicio si el mes no comienza en lunes
        repeat(startOffset) {
            currentWeek.add(null)
        }

        // Agregar todos los días del mes
        for (day in 1..daysInMonth) {
            currentWeek.add(day)
            if (currentWeek.size == 7) {
                weeks.add(currentWeek.toList())
                currentWeek.clear()
            }
        }

        // Agregar la última semana incompleta
        if (currentWeek.isNotEmpty()) {
            while (currentWeek.size < 7) {
                currentWeek.add(null)
            }
            weeks.add(currentWeek)
        }

        // Renderizar las semanas
        weeks.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    if (day != null) {
                        val dayDate = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        val isSelected = isSameDay(dayDate, selectedDate)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.4f)
                                .clickable {
                                    onDateSelected(dayDate)
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .size(27.dp)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = day.toString(),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}


