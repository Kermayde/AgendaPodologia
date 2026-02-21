package com.flores.agendapodologia.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableCalendarContainer(
    selectedDate: Long,
    displayedMonth: Pair<Int, Int>, // (year, month)
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onMonthChanged: (year: Int, month: Int) -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 1. Header con ícono de expansión/colapso
        ExpandableCalendarHeader(
            selectedDate = selectedDate,
            isExpanded = isExpanded,
            onToggleExpanded = onToggleExpanded,
            onOpenDirectory = onOpenDirectory,
            onOpenSettings = onOpenSettings
        )



        // 3. Grid mensual + Carrusel (solo cuando expandido)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                expandFrom = Alignment.Top
            ) + slideInVertically(
                initialOffsetY = { -it / 2 }
            ) + fadeIn(),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top
            ) + slideOutVertically(
                targetOffsetY = { -it / 2 }
            ) + fadeOut()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Grid mensual con paginación
                    MonthGridCalendar(
                        displayedYear = displayedMonth.first,
                        displayedMonth = displayedMonth.second,
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected,
                        onMonthChanged = onMonthChanged
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Carrusel de meses
                    MonthCarousel(
                        displayedYear = displayedMonth.first,
                        displayedMonth = displayedMonth.second,
                        onMonthSelected = onMonthChanged
                    )
                }
            }
        }
        // 2. Tira semanal (siempre visible)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            WeekCalendar(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
        }
    }
}


