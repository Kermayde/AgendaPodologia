package com.flores.agendapodologia.model

data class Appointment(
    val id: String = "",
    val patientId: String = "",      // Referencia al ID del documento del paciente
    val patientName: String = "",    // Copia del nombre para lectura rápida
    val podiatristName: String = "", // "Carlos" o "Karla"
    val serviceType: String = "",    // "Corte", "Uña encarnada", etc.
    val date: Long = 0L,             // Fecha y hora en milisegundos (Timestamp)
    val status: String = "PENDIENTE", // PENDIENTE, COMPLETADA, CANCELADA
    val notes: String = ""
)