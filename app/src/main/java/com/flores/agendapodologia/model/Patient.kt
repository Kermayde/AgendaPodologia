package com.flores.agendapodologia.model

// Firestore necesita valores por defecto (vacíos) para poder deserializar
data class Patient(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val lastVisit: Long = System.currentTimeMillis() // Timestamp simple
)
