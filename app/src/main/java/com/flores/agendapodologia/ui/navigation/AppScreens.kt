package com.flores.agendapodologia.ui.navigation

sealed class AppScreens(val route: String) {
    // Pantallas principales
    object Home : AppScreens("home")
    object PatientDirectory : AppScreens("patient_directory")
    object AddAppointment : AppScreens("add_appointment")

    // Pantallas con argumentos (Detalle de Cita y Detalle de Paciente)
    object AppointmentDetail : AppScreens("appointment_detail/{appointmentId}") {
        fun createRoute(appointmentId: String) = "appointment_detail/$appointmentId"
    }

    object PatientDetail : AppScreens("patient_detail/{patientId}") {
        fun createRoute(patientId: String) = "patient_detail/$patientId"
    }

    object Settings : AppScreens("settings")
    object Schedule : AppScreens("schedule")
    object Reminders : AppScreens("reminders")
    object CashRegister : AppScreens("cash_register")
}