package com.flores.agendapodologia.model

import java.util.Calendar

// Representa el horario de UN solo día
data class DaySchedule(
    val isOpen: Boolean = true,

    // Primer turno (Mañana)
    val shift1Start: Int = 10, // Formato 24h (10 AM)
    val shift1End: Int = 14,   // (2 PM)

    // Segundo turno (Tarde)
    val hasShift2: Boolean = true,
    val shift2Start: Int = 16, // (4 PM)
    val shift2End: Int = 20    // (8 PM)
)

// Representa la configuración completa de la clínica
data class ClinicSettings(
    val monday: DaySchedule = DaySchedule(),
    val tuesday: DaySchedule = DaySchedule(),
    val wednesday: DaySchedule = DaySchedule(),
    val thursday: DaySchedule = DaySchedule(),
    val friday: DaySchedule = DaySchedule(),
    val saturday: DaySchedule = DaySchedule(hasShift2 = false, shift1Start = 9, shift1End = 16),
    val sunday: DaySchedule = DaySchedule(isOpen = false)
) {
    fun getScheduleForDay(calendarDayOfWeek: Int): DaySchedule {
        return when (calendarDayOfWeek) {
            Calendar.MONDAY -> monday
            Calendar.TUESDAY -> tuesday
            Calendar.WEDNESDAY -> wednesday
            Calendar.THURSDAY -> thursday
            Calendar.FRIDAY -> friday
            Calendar.SATURDAY -> saturday
            Calendar.SUNDAY -> sunday
            else -> DaySchedule(isOpen = false)
        }
    }

    // --- NUEVA FUNCIÓN: Calcula si una hora es laboral ---
    fun isWorkingHour(dateMillis: Long, hourOfDay: Int): Boolean {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val daySchedule = getScheduleForDay(calendar.get(Calendar.DAY_OF_WEEK))

        // Si el día entero está cerrado (ej. Domingo)
        if (!daySchedule.isOpen) return false

        // Verificamos si la hora cae en el primer turno (Mañana)
        val inShift1 = hourOfDay in daySchedule.shift1Start until daySchedule.shift1End

        // Verificamos si la hora cae en el segundo turno (Tarde), si es que tiene
        val inShift2 = daySchedule.hasShift2 && (hourOfDay in daySchedule.shift2Start until daySchedule.shift2End)

        return inShift1 || inShift2
    }
}