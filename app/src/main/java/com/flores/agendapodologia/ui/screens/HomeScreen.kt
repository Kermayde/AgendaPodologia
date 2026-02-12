package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.flores.agendapodologia.ui.components.PatientItem
import com.flores.agendapodologia.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, onAddClick: () -> Unit) {
    // 1. Observamos el estado del ViewModel (La lista de pacientes)
    // "collectAsState" convierte el flujo de datos de Firebase en un estado de Compose
    val patients by viewModel.patients.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda Podológica") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Cita")
            }
        }
    ) { paddingValues ->

        // Contenido de la pantalla
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            if (patients.isEmpty()) {
                // Mensaje si no hay datos
                Text(
                    text = "No hay pacientes aún. ¡Agrega uno!",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                // Lista eficiente (Recycler View en Compose)
                LazyColumn {
                    items(patients) { patient ->
                        PatientItem(patient = patient)
                    }
                }
            }
            
        }
    }
}
