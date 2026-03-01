package com.flores.agendapodologia.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilidades centralizadas para formatear horas según la preferencia 12h/24h.
 */
object TimeFormatUtils {

    // ── Formatos ─────────────────────────────────────────────────

    /** Devuelve "14:30" (24h) o "2:30" (12h, sin AM/PM). */
    fun formatTime(date: Date, use12Hour: Boolean, locale: Locale = Locale.getDefault()): String {
        val pattern = if (use12Hour) "h:mm" else "HH:mm"
        return SimpleDateFormat(pattern, locale).format(date)
    }

    /** Devuelve "AM" o "PM" para la fecha dada. */
    fun getAmPm(date: Date, locale: Locale = Locale.getDefault()): String {
        return SimpleDateFormat("a", locale).format(date).uppercase(locale)
    }

    /** Devuelve "14:30" (24h) o "2:30 PM" (12h, con AM/PM). */
    fun formatTimeFull(date: Date, use12Hour: Boolean, locale: Locale = Locale.getDefault()): String {
        val pattern = if (use12Hour) "h:mm a" else "HH:mm"
        return SimpleDateFormat(pattern, locale).format(date)
    }

    // ── Formatos basados en hora entera (Int) ────────────────────

    /** Convierte una hora 0-23 a su representación: "14:00" (24h) o "2:00" (12h, sin AM/PM). */
    fun formatHour(hour: Int, use12Hour: Boolean): String {
        return if (use12Hour) {
            val h = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            String.format(Locale.getDefault(), "%d:00", h)
        } else {
            String.format(Locale.getDefault(), "%02d:00", hour)
        }
    }

    /** Devuelve "AM" o "PM" para una hora 0-23. */
    fun getAmPm(hour: Int): String {
        return if (hour < 12) "AM" else "PM"
    }

    // ── Formato fecha + hora para pantallas de detalle ───────────

    /** Devuelve "dd/MM/yyyy HH:mm" (24h) o "dd/MM/yyyy h:mm a" (12h). */
    fun formatDateTime(date: Date, use12Hour: Boolean, locale: Locale = Locale.getDefault()): String {
        val pattern = if (use12Hour) "dd/MM/yyyy h:mm a" else "dd/MM/yyyy HH:mm"
        return SimpleDateFormat(pattern, locale).format(date)
    }

    /** Devuelve solo la parte de hora: "HH:mm" (24h) o "h:mm a" (12h). */
    fun formatTimeOnly(date: Date, use12Hour: Boolean, locale: Locale = Locale.getDefault()): String {
        return formatTimeFull(date, use12Hour, locale)
    }

    /** Formatea hora y minuto (ints) como texto: "14:30" (24h) o "2:30 PM" (12h). */
    fun formatHourMinute(hour: Int, minute: Int, use12Hour: Boolean): String {
        return if (use12Hour) {
            val h = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val amPm = if (hour < 12) "AM" else "PM"
            String.format(Locale.getDefault(), "%d:%02d %s", h, minute, amPm)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        }
    }
}

