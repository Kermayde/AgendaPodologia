package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun MonthCarousel(
    displayedYear: Int,
    displayedMonth: Int, // 0-11
    onMonthSelected: (year: Int, month: Int) -> Unit
) {
    // Crear una lista de meses con separadores de año
    val monthsWithYear = mutableListOf<MonthCarouselItem>()

    // Generar 24 meses alrededor del mes actual (12 antes, 12 después)
    val baseYear = displayedYear - 1
    for (y in baseYear..displayedYear + 2) {
        for (m in 0..11) {
            // Insertar separador de año antes de enero
            if (m == 0 && y > baseYear) {
                monthsWithYear.add(MonthCarouselItem.YearSeparator(y))
            }
            monthsWithYear.add(MonthCarouselItem.Month(y, m))
        }
    }

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Encontrar el índice del mes actual y scrollear solo si no es visible
    LaunchedEffect(displayedYear, displayedMonth) {
        val targetIndex = monthsWithYear.indexOfFirst {
            it is MonthCarouselItem.Month && it.year == displayedYear && it.month == displayedMonth
        }
        if (targetIndex >= 0) {
            coroutineScope.launch {
                val layoutInfo = lazyListState.layoutInfo
                val visibleItemsInfo = layoutInfo.visibleItemsInfo

                // Verificar si el elemento está dentro de los visibles
                val isVisible = visibleItemsInfo.any { it.index == targetIndex }

                if (!isVisible) {
                    // Solo animar si no es visible - hacer scroll al centro
                    lazyListState.animateScrollToItem(
                        index = targetIndex,
                        scrollOffset = -150 // Centrar aproximadamente
                    )
                }
            }
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 16.dp),
        state = lazyListState,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(
            items = monthsWithYear,
            key = { item ->
                when (item) {
                    is MonthCarouselItem.Month -> "${item.year}-${item.month}"
                    is MonthCarouselItem.YearSeparator -> "sep-${item.year}"
                }
            }
        ) { item ->
            when (item) {
                is MonthCarouselItem.Month -> {
                    val isSelected = item.year == displayedYear && item.month == displayedMonth
                    val monthName = run {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.MONTH, item.month)
                        }
                        java.text.SimpleDateFormat("MMM", Locale.getDefault())
                            .format(cal.time)
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape( if (isSelected) 30.dp else 8.dp))
                            .background(
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                onMonthSelected(item.year, item.month)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = monthName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    }
                }

                is MonthCarouselItem.YearSeparator -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))

                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.year.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            //fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

sealed class MonthCarouselItem {
    data class Month(val year: Int, val month: Int) : MonthCarouselItem()
    data class YearSeparator(val year: Int) : MonthCarouselItem()
}



