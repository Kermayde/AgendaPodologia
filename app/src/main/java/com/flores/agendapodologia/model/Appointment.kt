package com.flores.agendapodologia.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class Appointment(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val patientPhone: String = "",
    val podiatristName: String = "",

    // SERVICIO (Estandarizado)
    val serviceType: String = "Quiropodia",

    // FECHAS
    val date: Date = Date(), // Fecha programada
    val completedAt: Date? = null, // Fecha real cuando se terminó la cita

    // ESTADO Y PAGO
    val status: AppointmentStatus = AppointmentStatus.PENDIENTE,
    // Esto le dice a Firebase: "Cuando leas/escribas este campo, en la BD llámalo 'paid'"
    @get:PropertyName("paid") @set:PropertyName("paid")
    var isPaid: Boolean = false,
    val paymentMethod: PaymentMethod = PaymentMethod.NONE,

    val notes: String = ""
)

// Enums para evitar errores de dedo ("Efectivo" vs "efectivo")
enum class AppointmentStatus {
    PENDIENTE,
    FINALIZADA,
    CANCELADA,
    NO_ASISTIO
}

enum class PaymentMethod {
    NONE,           // No pagado / Garantía
    EFECTIVO,
    TARJETA,
    TRANSFERENCIA,
    OTRO
}