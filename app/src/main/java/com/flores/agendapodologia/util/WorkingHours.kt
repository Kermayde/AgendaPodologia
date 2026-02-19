package com.flores.agendapodologia.util

import java.util.Calendar

object WorkingHours {
    // Definimos el rango visual del calendario (de qué hora a qué hora se dibuja la lista)
    const val START_HOUR = 6  // 6:00 AM
    const val END_HOUR = 21   // 9:00 PM

    fun isWorkingHour(dateMillis: Long, hourOfDay: Int): Boolean {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return when (dayOfWeek) {
            Calendar.SUNDAY -> false // Domingos cerrado
            Calendar.SATURDAY -> {
                // Sábados: 9am - 4pm (16:00)
                hourOfDay in 9 until 16
            }
            else -> {
                // Lunes a Viernes: 10am-2pm y 4pm-8pm
                (hourOfDay in 10 until 14) || (hourOfDay in 16 until 20)
            }
        }
    }
}