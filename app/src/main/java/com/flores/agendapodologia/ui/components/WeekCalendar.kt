package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeekCalendar(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    // Calculamos los días de la semana actual basada en selectedDate
    val daysOfWeek = remember(selectedDate) {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }

        // Vamos al lunes de esta semana
        // (En USA el primer día es Domingo, en MX suele ser Lunes, ajustamos según Locale o forzamos)
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY
        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)

        val days = mutableListOf<Date>()
        repeat(7) {
            days.add(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        days
    }

    val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault()) // "Lun", "Mar"
    val dayNumberFormat = SimpleDateFormat("d", Locale.getDefault())   // "12", "13"

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(daysOfWeek) { date ->
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
                    text = dayNameFormat.format(date).uppercase().take(3), // LUN
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dayNumberFormat.format(date), // 23
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