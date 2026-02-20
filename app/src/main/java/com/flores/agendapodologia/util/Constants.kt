package com.flores.agendapodologia.util

object ServiceConstants {
    // Nombres canónicos de servicios
    const val QUIROPODIA = "Quiropodia"
    const val CORRECTIVOS = "Correcciones"
    const val MATRICTOMIA = "Matricectomía"
    const val REFLEXOLOGIA = "Reflexología"
    const val CURACION = "Curación"
    const val REVISION = "Revisión"
    // Constante sin caracteres especiales para evitar warnings
    const val UNA_ENCARNADA = "Uña encarnada"
    const val BLOQUEO_PERSONAL = "Bloqueo Personal"
    const val OTRO = "Otro"

    val SERVICES_LIST = listOf(
        QUIROPODIA,
        CORRECTIVOS,
        MATRICTOMIA,
        REFLEXOLOGIA,
        CURACION,
        REVISION,
        UNA_ENCARNADA,
        BLOQUEO_PERSONAL,
        OTRO
    )

    // Servicios que activan la garantía (pagados)
    val WARRANTY_TRIGGER_SERVICES = listOf(QUIROPODIA, CORRECTIVOS)

    // Servicio específico que se considera "aplicable" para descuento/0$ al finalizar
    // Normalmente son las correcciones (reemplazo de correctivos)
    const val WARRANTY_APPLICABLE_SERVICE = CORRECTIVOS

    val SERVICE_PRICES = mapOf(
        QUIROPODIA to 550.0,
        UNA_ENCARNADA to 550.0,
        MATRICTOMIA to 3000.0,
        CORRECTIVOS to 250.0,
        REFLEXOLOGIA to 400.0,
        CURACION to 0.0,
        REVISION to 0.0,
        BLOQUEO_PERSONAL to 0.0,
        OTRO to 0.0
    )

    fun getSuggestedPrice(serviceType: String): Double {
        return SERVICE_PRICES[serviceType] ?: 0.0
    }
}