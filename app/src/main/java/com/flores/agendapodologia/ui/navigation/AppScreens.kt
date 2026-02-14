package com.flores.agendapodologia.ui.navigation

sealed class AppScreens(val route: String) {
    // Pantallas principales
    object Home : AppScreens("home")
    object PatientDirectory : AppScreens("patient_directory") // <--- NUEVA
    object AddAppointment : AppScreens("add_appointment")

    // Pantallas con argumentos (Detalle de Cita y Detalle de Paciente)
    object AppointmentDetail : AppScreens("appointment_detail/{appointmentId}") {
        fun createRoute(appointmentId: String) = "appointment_detail/$appointmentId"
    }

    object PatientDetail : AppScreens("patient_detail/{patientId}") { // <--- NUEVA
        fun createRoute(patientId: String) = "patient_detail/$patientId"
    }
}