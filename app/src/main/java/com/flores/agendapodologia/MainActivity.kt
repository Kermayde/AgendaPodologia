package com.flores.agendapodologia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.flores.agendapodologia.data.repository.AgendaRepositoryImpl
import com.flores.agendapodologia.ui.screens.AddAppointmentScreen
import com.flores.agendapodologia.ui.screens.AppointmentDetailScreen
import com.flores.agendapodologia.ui.screens.HomeScreen
import com.flores.agendapodologia.viewmodel.HomeViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.flores.agendapodologia.ui.navigation.AppScreens
import com.flores.agendapodologia.ui.screens.PatientDetailScreen
import com.flores.agendapodologia.ui.screens.PatientDirectoryScreen
import com.flores.agendapodologia.ui.screens.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inyección de dependencias simple
        val db = Firebase.firestore
        val repository = AgendaRepositoryImpl(db)
        val viewModel = HomeViewModel(repository)

        setContent {
            val navController = rememberNavController()
            // Estado para saber en qué pantalla estamos (opcional, para colorear iconos del BottomBar si tuvieras)

            MaterialTheme {
                NavHost(navController = navController, startDestination = AppScreens.Home.route) {

                    // 1. HOME
                    composable(AppScreens.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            onAddClick = { navController.navigate(AppScreens.AddAppointment.route) },
                            onAppointmentClick = { appt -> navController.navigate(AppScreens.AppointmentDetail.createRoute(appt.id)) },
                            onOpenDirectory = { navController.navigate(AppScreens.PatientDirectory.route) },
                            navController = navController
                        )
                    }

                    // 2. DIRECTORIO (Lista de Pacientes)
                    composable(AppScreens.PatientDirectory.route) {
                        PatientDirectoryScreen(
                            viewModel = viewModel,
                            onPatientClick = { patient ->
                                navController.navigate(AppScreens.PatientDetail.createRoute(patient.id))
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 3. DETALLE PACIENTE (WhatsApp, Bloqueo, etc.)
                    composable(
                        route = AppScreens.PatientDetail.route,
                        arguments = listOf(navArgument("patientId") { type = NavType.StringType })
                    ) { entry ->
                        val id = entry.arguments?.getString("patientId")
                        LaunchedEffect(id) { id?.let { viewModel.loadPatientDetail(it) } }

                        PatientDetailScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 4. AGREGAR CITA
                    composable(AppScreens.AddAppointment.route) {
                        AddAppointmentScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 5. DETALLE CITA
                    composable(
                        route = AppScreens.AppointmentDetail.route,
                        arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
                    ) { entry ->
                        val id = entry.arguments?.getString("appointmentId")
                        LaunchedEffect(id) { id?.let { viewModel.loadAppointmentDetails(it) } }

                        AppointmentDetailScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 6. CONFIGURACIÓN (NUEVO)
                    composable(AppScreens.Settings.route) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}