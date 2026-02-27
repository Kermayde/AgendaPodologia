package com.flores.agendapodologia.model

enum class PatientStatus {
    ACTIVE,     // Paciente normal (Verde)
    BLOCKED,    // Paciente problemático (Rojo)
    ARCHIVED    // (Opcional) Para no borrar pero ocultar
}

enum class ReminderPreference {
    WHATSAPP,
    LLAMADA,
    NINGUNO
}

data class Patient(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val status: PatientStatus = PatientStatus.ACTIVE,
    val reminderPreference: ReminderPreference = ReminderPreference.WHATSAPP,
    val lastVisit: Long = System.currentTimeMillis(),
    val blockReason: String = "" // Motivo por el cual el paciente está en lista negra
)