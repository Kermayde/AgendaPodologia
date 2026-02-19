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

    // Servicios que "activan" la garantía para futuras correcciones
    val WARRANTY_TRIGGER_SERVICES = listOf("Quiropodia", "Correcciones")
}