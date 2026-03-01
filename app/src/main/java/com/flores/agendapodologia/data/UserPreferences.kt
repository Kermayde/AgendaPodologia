package com.flores.agendapodologia.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencias locales del usuario almacenadas en SharedPreferences.
 * Cada dispositivo/usuario mantiene su propia configuración.
 */
class UserPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "agenda_podologia_prefs"
        private const val KEY_USE_12_HOUR_FORMAT = "use_12_hour_format"

        @Volatile
        private var instance: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return instance ?: synchronized(this) {
                instance ?: UserPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Formato de 12 horas ──────────────────────────────────────
    private val _use12HourFormat = MutableStateFlow(prefs.getBoolean(KEY_USE_12_HOUR_FORMAT, false))
    val use12HourFormat: StateFlow<Boolean> = _use12HourFormat.asStateFlow()

    fun setUse12HourFormat(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_12_HOUR_FORMAT, enabled).apply()
        _use12HourFormat.value = enabled
    }
}

