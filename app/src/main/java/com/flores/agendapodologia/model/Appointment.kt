package com.flores.agendapodologia.model

import java.util.Date

data class Appointment(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val patientPhone: String = "", // Agregamos teléfono aquí por si acaso
    val podiatristName: String = "",
    val serviceType: String = "",
    val date: Date = Date(), // Usamos java.util.Date
    val status: String = "PENDIENTE",
    val notes: String = ""
)