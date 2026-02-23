package com.flores.agendapodologia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flores.agendapodologia.data.repository.AgendaRepositoryImpl
import com.flores.agendapodologia.ui.components.FloatingNavBar
import com.flores.agendapodologia.ui.navigation.AppScreens
import com.flores.agendapodologia.ui.screens.AddAppointmentScreen
import com.flores.agendapodologia.ui.screens.AppointmentDetailScreen
import com.flores.agendapodologia.ui.screens.HomeScreen
import com.flores.agendapodologia.ui.screens.PatientDetailScreen
import com.flores.agendapodologia.ui.screens.PatientDirectoryScreen
import com.flores.agendapodologia.ui.screens.SettingsScreen
import com.flores.agendapodologia.ui.theme.AgendaPodologiaTheme
import com.flores.agendapodologia.viewmodel.HomeViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // Inyección de dependencias simple
        val db = Firebase.firestore
        val repository = AgendaRepositoryImpl(db)
        val viewModel = HomeViewModel(repository)

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Pantallas donde se muestra la barra flotante
            val showNavBar = currentRoute in listOf(
                AppScreens.Home.route,
                AppScreens.PatientDirectory.route,
                AppScreens.Settings.route
            )

            AgendaPodologiaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(navController = navController, startDestination = AppScreens.Home.route) {

                            // 1. HOME
                            composable(AppScreens.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onAddClick = { navController.navigate(AppScreens.AddAppointment.route) },
                                onAppointmentClick = { appt -> navController.navigate(AppScreens.AppointmentDetail.createRoute(appt.id)) }
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

                            // 6. CONFIGURACIÓN
                            composable(AppScreens.Settings.route) {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }

                        // Barra de navegación flotante
                        if (showNavBar) {
                            FloatingNavBar(
                                currentRoute = currentRoute,
                                onNavigateToHome = {
                                    navController.navigate(AppScreens.Home.route) {
                                        popUpTo(AppScreens.Home.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToPatients = {
                                    navController.navigate(AppScreens.PatientDirectory.route) {
                                        popUpTo(AppScreens.Home.route)
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToSettings = {
                                    navController.navigate(AppScreens.Settings.route) {
                                        popUpTo(AppScreens.Home.route)
                                        launchSingleTop = true
                                    }
                                },
                                onAddAppointment = {
                                    navController.navigate(AppScreens.AddAppointment.route)
                                },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}