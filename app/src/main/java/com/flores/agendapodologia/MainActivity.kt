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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inyección de dependencias simple
        val db = Firebase.firestore
        val repository = AgendaRepositoryImpl(db)
        val viewModel = HomeViewModel(repository)

        setContent {
            // El controlador de navegación: el cerebro que sabe dónde estamos
            val navController = rememberNavController()

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    // DEFINICIÓN DEL GRAFO DE NAVEGACIÓN
                    NavHost(
                        navController = navController,
                        startDestination = AppScreens.Home.route
                    ) {

                        // 1. PANTALLA HOME
                        composable(route = AppScreens.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onAddClick = {
                                    navController.navigate(AppScreens.AddAppointment.route)
                                },
                                onAppointmentClick = { appointment ->
                                    // Navegamos pasando el ID en la ruta
                                    navController.navigate(
                                        AppScreens.AppointmentDetail.createRoute(appointment.id)
                                    )
                                }
                            )
                        }

                        // 2. PANTALLA AGREGAR CITA
                        composable(route = AppScreens.AddAppointment.route) {
                            AddAppointmentScreen(
                                viewModel = viewModel,
                                onBack = {
                                    navController.popBackStack() // Volver atrás
                                }
                            )
                        }

                        // 3. PANTALLA DETALLE CITA (Recibe ID)
                        composable(
                            route = AppScreens.AppointmentDetail.route,
                            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            // Extraemos el ID de la ruta
                            val appointmentId = backStackEntry.arguments?.getString("appointmentId")

                            // Le decimos al ViewModel que cargue los datos
                            LaunchedEffect(appointmentId) {
                                appointmentId?.let { viewModel.loadAppointmentDetails(it) }
                            }

                            AppointmentDetailScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Aquí agregaremos el Directorio de Pacientes en el futuro...
                    }
                }
            }
        }
    }
}