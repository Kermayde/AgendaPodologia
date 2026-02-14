package com.flores.agendapodologia.ui.navigation

sealed class AppScreens(val route: String) {
    // Pantallas simples
    object Home : AppScreens("home")
    object AddAppointment : AppScreens("add_appointment")

    // Pantalla con argumentos (ID de la cita)
    object AppointmentDetail : AppScreens("appointment_detail/{appointmentId}") {
        // Función auxiliar para construir la ruta con el ID real
        fun createRoute(appointmentId: String) = "appointment_detail/$appointmentId"
    }

    // Futuras pantallas (Directorio)
    object PatientDirectory : AppScreens("patient_directory")
    // object PatientDetail : AppScreens("patient_detail/{patientId}")
}