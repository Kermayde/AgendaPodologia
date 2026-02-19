package com.flores.agendapodologia.util

object ServiceConstants {
    val SERVICES_LIST = listOf(
        "Quiropodia",
        "Correcciones",
        "Matricectomía",
        "Reflexología",
        "Curación",
        "Revisión",
        "Uña encarnada",
        "Bloqueo Personal",
        "Otro"
    )

    val WARRANTY_TRIGGER_SERVICES = listOf("Quiropodia", "Correcciones")

    val SERVICE_PRICES = mapOf(
        "Quiropodia" to 550.0,
        "Uña encarnada" to 550.0,
        "Matricectomía" to 3000.0,
        "Correcciones" to 250.0,
        "Reflexología" to 400.0,
        "Curación" to 0.0,
        "Revisión" to 0.0,
        "Bloqueo Personal" to 0.0,
        "Otro" to 0.0
    )

    fun getSuggestedPrice(serviceType: String): Double {
        return SERVICE_PRICES[serviceType] ?: 0.0
    }
}