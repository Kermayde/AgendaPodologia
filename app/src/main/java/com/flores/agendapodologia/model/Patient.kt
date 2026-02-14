package com.flores.agendapodologia.model

enum class PatientStatus {
    ACTIVE,     // Paciente normal (Verde)
    BLOCKED,    // Paciente problemático (Rojo)
    ARCHIVED    // (Opcional) Para no borrar pero ocultar
}

data class Patient(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val status: PatientStatus = PatientStatus.ACTIVE, // <--- NUEVO CAMPO
    val lastVisit: Long = System.currentTimeMillis()
)