package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flores.agendapodologia.model.Patient
import com.flores.agendapodologia.model.PatientStatus
import com.flores.agendapodologia.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDirectoryScreen(
    viewModel: HomeViewModel,
    onPatientClick: (Patient) -> Unit,
    onBack: () -> Unit
) {
    val patients by viewModel.directoryList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                title = { Text("Directorio de Pacientes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // BARRA DE BÚSQUEDA
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar por nombre o teléfono...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // LISTA DE PACIENTES
            LazyColumn {
                items(patients) { patient ->
                    PatientDirectoryItem(patient = patient, onClick = { onPatientClick(patient) })
                }
            }
        }
    }
}

@Composable
fun PatientDirectoryItem(patient: Patient, onClick: () -> Unit) {
    // Si está bloqueado, usamos un color rojizo de fondo para alertar
    val backgroundColor = if (patient.status == PatientStatus.BLOCKED)
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    else
        MaterialTheme.colorScheme.surface

    ListItem(
        modifier = Modifier
            .clickable { onClick() }
            .background(backgroundColor),
        headlineContent = {
            Text(
                text = patient.name,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = if (patient.status == PatientStatus.BLOCKED) MaterialTheme.colorScheme.error else Color.Unspecified
            )
        },
        supportingContent = { Text(patient.phone) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (patient.status == PatientStatus.BLOCKED) {
                    Icon(Icons.Default.Warning, null, tint = Color.White)
                } else {
                    Text(
                        text = patient.name.firstOrNull()?.toString() ?: "?",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    )
    Divider()
}