package com.flores.agendapodologia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inyección de dependencias simple
        val db = Firebase.firestore
        val repository = AgendaRepositoryImpl(db)
        val viewModel = HomeViewModel(repository)

        setContent {

            // Asegúrate de tener un Theme creado, si no, quita "AgendaPodologiaTheme"
            MaterialTheme {
                // Estado simple para controlar navegación
                var currentScreen by remember { mutableStateOf("home") }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when (currentScreen) {
                        "home" -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onAddClick = { currentScreen = "add_appointment" },
                                onAppointmentClick = { appointment ->  // <--- NUEVO CALLBACK
                                    viewModel.selectAppointment(appointment)
                                    currentScreen = "detail_appointment"
                                }
                            )
                        }
                        "add_appointment" -> {
                            AddAppointmentScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = "home" }
                            )
                        }
                        "detail_appointment" -> { // <--- NUEVA PANTALLA
                            AppointmentDetailScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = "home" }
                            )
                        }
                    }
                }
            }
        }
    }
}
