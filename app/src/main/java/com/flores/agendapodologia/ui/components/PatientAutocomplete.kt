package com.flores.agendapodologia.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.model.Patient

@Composable
fun PatientAutocomplete(
    patientsFound: List<Patient>,
    onQueryChanged: (String) -> Unit,
    onPatientSelected: (Patient) -> Unit,
    onClearSelection: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                onQueryChanged(newText)
                isExpanded = newText.isNotEmpty()
                // Si borra el texto, limpiamos la selección previa
                if (newText.isEmpty()) onClearSelection()
            },
            label = { Text("Nombre del Paciente") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )

        // Lista desplegable de sugerencias
        AnimatedVisibility(visible = isExpanded && patientsFound.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp) // Altura máxima para no tapar todo
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn {
                    items(patientsFound) { patient ->
                        ListItem(
                            headlineContent = { Text(patient.name) },
                            supportingContent = { Text(patient.phone) },
                            modifier = Modifier.clickable {
                                text = patient.name // Autocompletar texto
                                onPatientSelected(patient) // Avisar al padre
                                isExpanded = false // Cerrar lista
                            }
                        )
                    }
                }
            }
        }
    }
}